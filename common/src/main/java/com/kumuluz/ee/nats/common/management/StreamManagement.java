package com.kumuluz.ee.nats.common.management;

import com.kumuluz.ee.nats.common.connection.NatsConnection;
import com.kumuluz.ee.nats.common.connection.config.NatsConfigLoader;
import com.kumuluz.ee.nats.common.connection.config.NatsConnectionConfig;
import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Helper for managing streams
 *
 * @author Matej Bizjak
 */

public class StreamManagement {

    private static final Logger LOG = Logger.getLogger(StreamManagement.class.getName());

    public static void establishAll() {
        HashMap<String, Connection> connections = NatsConnection.getAllConnections();
        HashMap<String, NatsConnectionConfig> connectionConfigs = NatsConfigLoader.getInstance().getConnectionConfigs();

        connections.forEach(
                (name, connection) -> connectionConfigs.get(name).getStreamConfigurations().forEach(
                        streamConfiguration -> {
                            try {
                                createStreamOrUpdateSubjects(connection, streamConfiguration);
                            } catch (IOException | JetStreamApiException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
        );
    }

    public static StreamInfo getStreamInfoOrNullWhenNotExist(JetStreamManagement jetStreamManagement, String streamName)
            throws IOException, JetStreamApiException {
        try {
            return jetStreamManagement.getStreamInfo(streamName);
        } catch (JetStreamApiException jsae) {
            if (jsae.getErrorCode() == 404) {
                return null;
            }
            throw jsae;
        }
    }

    public static StreamInfo createStream(JetStreamManagement jetStreamManagement, StreamConfiguration streamConfiguration)
            throws IOException, JetStreamApiException {
        StreamInfo streamInfo = jetStreamManagement.addStream(streamConfiguration);
        LOG.info(String.format("Created stream '%s' with subject(s) %s\n",
                streamConfiguration.getName(), streamInfo.getConfiguration().getSubjects()));
        return streamInfo;
    }

    public static StreamInfo createStream(Connection nc, StreamConfiguration streamConfiguration) throws IOException, JetStreamApiException {
        return createStream(nc.jetStreamManagement(), streamConfiguration);
    }

    public static StreamInfo createStreamOrUpdateSubjects(JetStreamManagement jetStreamManagement, StreamConfiguration streamConfiguration)
            throws IOException, JetStreamApiException {
        StreamInfo streamInfo = getStreamInfoOrNullWhenNotExist(jetStreamManagement, streamConfiguration.getName());
        if (streamInfo == null) {
            return createStream(jetStreamManagement, streamConfiguration);
        }

        // check to see if the configuration has all the subject we want
        StreamConfiguration streamConfigurationDb = streamInfo.getConfiguration();
        boolean needToUpdate = false;
        for (String subject : streamConfiguration.getSubjects()) {
            if (!streamConfigurationDb.getSubjects().contains(subject)) {
                needToUpdate = true;
                streamConfigurationDb.getSubjects().add(subject);
            }
        }
        if (needToUpdate) {
            streamConfigurationDb = StreamConfiguration.builder(streamConfigurationDb).subjects(streamConfigurationDb.getSubjects()).build();
            streamInfo = jetStreamManagement.updateStream(streamConfigurationDb);
            LOG.info(String.format("Existing stream '%s' was updated, has subject(s) %s\n",
                    streamConfiguration.getName(), streamInfo.getConfiguration().getSubjects()));
        } else {
            LOG.info(String.format("Existing stream '%s' already contained subject(s) %s\n",
                    streamConfiguration.getName(), streamInfo.getConfiguration().getSubjects()));
        }
        return streamInfo;
    }

    public static StreamInfo createStreamOrUpdateSubjects(Connection nc, StreamConfiguration streamConfiguration)
            throws IOException, JetStreamApiException {
        return createStreamOrUpdateSubjects(nc.jetStreamManagement(), streamConfiguration);
    }

    public static void addOrUpdateConsumer(String connectionName, String streamName, ConsumerConfiguration consumerConfiguration) {
        Connection nc = NatsConnection.getConnection(connectionName);
        try {
            nc.jetStreamManagement().addOrUpdateConsumer(streamName, consumerConfiguration);
            LOG.info(String.format("Successfully added/updated consumer configuration for connection %s and stream %s"
                    , connectionName, streamName));
        } catch (IOException | JetStreamApiException e) {
            LOG.severe(String.format("Unable to add/update consumer configuration for connection %s and stream %s"
                    , connectionName, streamName));
        }
    }
}
