package com.kumuluz.ee.nats.jetstream.context;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.kumuluz.ee.nats.common.connection.NatsConnection;
import com.kumuluz.ee.nats.common.connection.config.ConfigLoader;
import com.kumuluz.ee.nats.common.connection.config.ConnectionConfig;
import com.kumuluz.ee.nats.jetstream.JetStreamExtension;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamOptions;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for JetStream contexts.
 *
 * @author Matej Bizjak
 */

public class ContextFactory {

    private static final Logger LOG = Logger.getLogger(ContextFactory.class.getName());

    private static ContextFactory instance;

    private static final Table<String, String, JetStream> jetStreamContexts = HashBasedTable.create();

    public ContextFactory() {
    }

    private static synchronized void init() {
        if (instance == null) {
            instance = new ContextFactory();
        }
    }

    public static ContextFactory getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    private JetStream createContext(String connectionName, String contextName) {
        JetStream jetStream = null;
        ConfigLoader configLoader = ConfigLoader.getInstance();
        ConnectionConfig config = configLoader.getConfigForConnection(connectionName);
        JetStreamOptions jetStreamOptions = config.getJetStreamContextOptions().get(contextName);
        Connection connection = NatsConnection.getConnection(connectionName);
        if (connection == null) {
            LOG.severe(String.format("Cannot create a JetStream context %s for a connection %s because the connection was not established."
                    , contextName, connectionName));
        } else {
            try {
                jetStream = connection.jetStream(jetStreamOptions);
                LOG.info(String.format("JetStream context %s for a connection %s was created successfully."
                        , contextName, connectionName));
            } catch (IOException e) {
                LOG.log(Level.SEVERE, String.format("Cannot create a JetStream context %s for a connection %s."
                        , contextName, connectionName), e);
            }
        }
        return jetStream;
    }

    public JetStream getContext(String connectionName, String contextName) {
        if (!JetStreamExtension.isExtensionEnabled()) {
            return null;
        }

        if (!jetStreamContexts.contains(connectionName, contextName)) {
            JetStream jetStream = createContext(connectionName, contextName);
            if (jetStream != null) {
                jetStreamContexts.put(connectionName, contextName, jetStream);
            }
        }
        return jetStreamContexts.get(connectionName, contextName);
    }

}
