package com.kumuluz.ee.nats.common.management;

import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.ConsumerInfo;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Helper for managing consumers.
 *
 * @author Matej Bizjak
 */

public class ConsumerManagement {

    private static final Logger LOG = Logger.getLogger(ConsumerManagement.class.getName());

    // TODO remove commented methods
//    public static void establishAll() {
//        HashMap<String, Connection> connections = NatsConnection.getAllConnections();
//        HashMap<String, ConnectionConfig> connectionConfigs = NatsConfigLoader.getInstance().getConnectionConfigs();
//        connections.forEach(
//                (connectionName, connection) -> connectionConfigs.get(connectionName).getStreamConsumerConfigurations().forEach(
//                        streamConfiguration -> {
//                            streamConfiguration.getConsumerConfigurations().forEach(
//                                    consumerConfiguration -> {
//                                        Optional<ConsumerConfiguration> buildedConsumerConfigurationOptional = streamConfiguration.getAndCombineConsumerConfig(connection, consumerConfiguration.getName(), null);
//                                        buildedConsumerConfigurationOptional.ifPresent(x -> createOrUpdateConsumer(connectionName, streamConfiguration.getStreamName(), consumerConfiguration.getName(), x));
//                                    }
//                            );
//                        }
//                )
//        );
//    }

//    public static void createOrUpdateConsumer(String connectionName, String streamName, String consumerName, ConsumerConfiguration consumerConfiguration) {
//        try {
//            Connection connection = NatsConnection.getConnection(connectionName);
//            connection.jetStreamManagement().addOrUpdateConsumer(streamName, consumerConfiguration);
//            LOG.info(String.format("Successfully created/updated consumer configuration %s for connection %s and stream %s."
//                    , consumerName, connectionName, streamName));
//        } catch (IOException | JetStreamApiException e) {
//            LOG.log(Level.SEVERE, String.format("Unable to create/update consumer configuration %s for connection %s and stream %s."
//                    , consumerName, connectionName, streamName), e);
//        }
//    }

    public static ConsumerInfo getConsumerInfoOrNullWhenNotExist(JetStreamManagement jetStreamManagement, String streamName, String consumerName) throws IOException, JetStreamApiException {
        try {
            return jetStreamManagement.getConsumerInfo(streamName, consumerName);
        } catch (JetStreamApiException jsae) {
            if (jsae.getErrorCode() == 404) {
                return null;
            }
            throw jsae;
        }
    }

    public static ConsumerConfiguration getConsumerConfigurationOrNullWhenNotExist(Connection connection, String streamName, String consumerName) {
        try {
            ConsumerInfo consumerInfo = getConsumerInfoOrNullWhenNotExist(connection.jetStreamManagement(), streamName, consumerName);
            if (consumerInfo == null) {
                return null;
            }
            return consumerInfo.getConsumerConfiguration();
        } catch (JetStreamApiException | IOException e) {
            return null;
        }
    }
}
