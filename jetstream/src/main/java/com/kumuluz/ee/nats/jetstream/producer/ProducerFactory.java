package com.kumuluz.ee.nats.jetstream.producer;

import com.kumuluz.ee.nats.common.connection.NatsConnection;
import com.kumuluz.ee.nats.common.connection.config.NatsConfigLoader;
import com.kumuluz.ee.nats.common.connection.config.NatsConnectionConfig;
import com.kumuluz.ee.nats.jetstream.NatsJetStreamExtension;
import com.kumuluz.ee.nats.jetstream.annotations.JetStreamProducer;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamOptions;

import javax.enterprise.inject.spi.InjectionPoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Matej Bizjak
 */

public class ProducerFactory {

    private static final Logger LOG = Logger.getLogger(ProducerFactory.class.getName());

    private static ProducerFactory instance;

    private final Map<Connection, JetStream> producers = new HashMap<>();

    public ProducerFactory() {
    }

    private static synchronized void init() {
        if (instance == null) {
            instance = new ProducerFactory();
        }
    }

    public static ProducerFactory getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    public JetStream createProducer(Connection connection, String connectionName) {
        JetStream producer = null;
        NatsConfigLoader configLoader = NatsConfigLoader.getInstance();
        NatsConnectionConfig config = configLoader.getConfigForConnection(connectionName);

//        config.getStreamConfigurations()
        JetStreamOptions jetStreamOptions = JetStreamOptions.builder().build(); // TODO

        try {
            producer = connection.jetStream(jetStreamOptions);
            LOG.info("Created JetStream context.");
        } catch (IOException e) {
            LOG.severe(String.format("Cannot create a JetStream context for connection: %s", connectionName));
        }

        return producer;
    }

    public JetStream getProducer(InjectionPoint injectionPoint) {
        if (!NatsJetStreamExtension.isExtensionEnabled()) {
            return null;
        }

        JetStreamProducer annotation = injectionPoint.getAnnotated().getAnnotation(JetStreamProducer.class);
        String connectionName = annotation.connection();
        Connection connection = NatsConnection.getConnection(connectionName);

        if (!producers.containsKey(connection)) {
            JetStream producer = createProducer(connection, connectionName);
            producers.put(connection, producer);
        }

        return producers.get(connection);
    }
}
