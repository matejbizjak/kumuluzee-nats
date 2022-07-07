package com.kumuluz.ee.nats.common.management;

import com.kumuluz.ee.nats.common.connection.NatsConnection;
import com.kumuluz.ee.nats.common.connection.config.ConfigLoader;
import com.kumuluz.ee.nats.common.connection.config.ConnectionConfig;
import com.kumuluz.ee.nats.common.exception.NatsException;
import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.StreamConfiguration;
import io.nats.client.api.StreamInfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Helper for managing streams.
 *
 * @author Matej Bizjak
 */

public class StreamManagement {

    private static final Logger LOG = Logger.getLogger(StreamManagement.class.getName());

    public static void establishAll() {
        HashMap<String, Connection> connections = NatsConnection.getAllConnections();
        HashMap<String, ConnectionConfig> connectionConfigs = ConfigLoader.getInstance().getConnectionConfigs();
        connections.forEach(
                (name, connection) -> connectionConfigs.get(name).getStreamConfigurations().forEach(
                        streamConfiguration -> {
                            try {
                                createStreamOrUpdateSubjects(connection, streamConfiguration);
                            } catch (IOException | JetStreamApiException e) {
                                throw new NatsException(e);
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
        LOG.info(String.format("Created stream %s with subject(s) %s.",
                streamConfiguration.getName(), streamInfo.getConfiguration().getSubjects()));
        return streamInfo;
    }

    public static StreamInfo createStreamOrUpdateSubjects(JetStreamManagement jetStreamManagement, StreamConfiguration streamConfiguration)
            throws IOException, JetStreamApiException {
        StreamInfo streamInfo = getStreamInfoOrNullWhenNotExist(jetStreamManagement, streamConfiguration.getName());
        if (streamInfo == null) {
            return createStream(jetStreamManagement, streamConfiguration);
        }

//        // update subjects only
//        // check to see if the configuration has all the subjects we want
//        StreamConfiguration streamConfigurationDb = streamInfo.getConfiguration();
//        boolean needToUpdate = false;
//        for (String subject : streamConfiguration.getSubjects()) {
//            if (!streamConfigurationDb.getSubjects().contains(subject)) {
//                needToUpdate = true;
//                streamConfigurationDb.getSubjects().add(subject);
//            }
//        }
//        if (needToUpdate) {
//            streamConfigurationDb = StreamConfiguration.builder(streamConfigurationDb).subjects(streamConfigurationDb.getSubjects()).build();
//            streamInfo = jetStreamManagement.updateStream(streamConfigurationDb);
//            LOG.info(String.format("Existing stream %s was updated, has subject(s) %s.",
//                    streamConfiguration.getName(), streamInfo.getConfiguration().getSubjects()));
//        } else {
//            LOG.info(String.format("Existing stream %s already contained subject(s) %s.",
//                    streamConfiguration.getName(), streamInfo.getConfiguration().getSubjects()));
//        }

        if (configurationsChanged(streamInfo.getConfiguration(), streamConfiguration)) {
            streamInfo = jetStreamManagement.updateStream(streamConfiguration);
            LOG.info(String.format("Existing stream %s was updated.", streamConfiguration.getName()));
        }

        return streamInfo;
    }

    public static StreamInfo createStreamOrUpdateSubjects(Connection connection, StreamConfiguration streamConfiguration)
            throws IOException, JetStreamApiException {
        return createStreamOrUpdateSubjects(connection.jetStreamManagement(), streamConfiguration);
    }

    private static boolean configurationsChanged(StreamConfiguration db, StreamConfiguration req) {
        return !(db.getSubjects().equals(req.getSubjects())
                && emptyIfNull(db.getDescription()).equals(emptyIfNull(req.getDescription()))
                && db.getRetentionPolicy().equals(req.getRetentionPolicy())
                && db.getMaxConsumers() == req.getMaxConsumers()
                && db.getMaxBytes() == req.getMaxBytes()
                && db.getMaxAge() == req.getMaxAge()
                && db.getMaxMsgs() == req.getMaxMsgs()
                && db.getMaxMsgSize() == req.getMaxMsgSize()
                && db.getStorageType().equals(req.getStorageType())
                && db.getReplicas() == req.getReplicas()
                && db.getNoAck() == req.getNoAck()
                && emptyIfNull(db.getTemplateOwner()).equals(emptyIfNull(req.getTemplateOwner()))
                && db.getDiscardPolicy().equals(req.getDiscardPolicy())
                && db.getDuplicateWindow().equals(req.getDuplicateWindow())
        );
    }

    private static String emptyIfNull(String string) {
        if (string == null) {
            return "";
        }
        return string;
    }

//    public static void addOrUpdateConsumer(String connectionName, String streamName, ConsumerConfiguration consumerConfiguration) {
//        Connection connection = NatsConnection.getConnection(connectionName);
//        if (connection == null) {
//            LOG.severe(String.format("Unable to add/update consumer configuration for connection %s and stream %s because the connection was not established."
//                    , connectionName, streamName));
//        } else {
//            try {
//                connection.jetStreamManagement().addOrUpdateConsumer(streamName, consumerConfiguration);
//                LOG.info(String.format("Successfully added/updated consumer configuration for connection %s and stream %s."
//                        , connectionName, streamName));
//            } catch (IOException | JetStreamApiException e) {
//                LOG.log(Level.SEVERE, String.format("Unable to add/update consumer configuration for connection %s and stream %s."
//                        , connectionName, streamName), e);
//            }
//        }
//    }
}
