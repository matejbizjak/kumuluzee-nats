package com.kumuluz.ee.nats.jetstream.context;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.kumuluz.ee.nats.common.connection.NatsConnection;
import com.kumuluz.ee.nats.common.connection.config.NatsConfigLoader;
import com.kumuluz.ee.nats.common.connection.config.NatsConnectionConfig;
import com.kumuluz.ee.nats.jetstream.JetStreamExtension;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamOptions;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author Matej Bizjak
 */

public class JetStreamContextFactory {

    private static final Logger LOG = Logger.getLogger(JetStreamContextFactory.class.getName());

    private static JetStreamContextFactory instance;

    private static final Table<String, String, JetStream> jetStreamContexts = HashBasedTable.create();

    public JetStreamContextFactory() {
    }

    private static synchronized void init() {
        if (instance == null) {
            instance = new JetStreamContextFactory();
        }
    }

    public static JetStreamContextFactory getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    private JetStream createContext(String connectionName, String contextName) {
        JetStream jetStream = null;
        NatsConfigLoader configLoader = NatsConfigLoader.getInstance();
        NatsConnectionConfig config = configLoader.getConfigForConnection(connectionName);
        JetStreamOptions jetStreamOptions = config.getJetStreamContextOptions().get(contextName);
        Connection connection = NatsConnection.getConnection(connectionName);
        try {
            jetStream = connection.jetStream(jetStreamOptions);
            LOG.info(String.format("JetStream context %s for a connection %s was created successfully", contextName, connectionName));
        } catch (IOException e) {
            LOG.severe(String.format("Cannot create a JetStream context %s for a connection %s", contextName, connectionName));
        }
        return jetStream;
    }

    public JetStream getContext(String connectionName, String contextName) {
        if (!JetStreamExtension.isExtensionEnabled()) {
            return null;
        }

        if (!jetStreamContexts.contains(connectionName, contextName)) {
            JetStream jetStream = createContext(connectionName, contextName);
            jetStreamContexts.put(connectionName, contextName, jetStream);
        }
        return jetStreamContexts.get(connectionName, contextName);
    }

}
