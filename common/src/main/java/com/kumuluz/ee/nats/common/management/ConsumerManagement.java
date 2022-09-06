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
