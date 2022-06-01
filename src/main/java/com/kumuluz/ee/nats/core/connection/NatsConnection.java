package com.kumuluz.ee.nats.core.connection;

import com.kumuluz.ee.nats.core.connection.config.NatsConnectionConfig;
import io.nats.client.Connection;
import io.nats.client.Nats;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Matej Bizjak
 */

/**
 * @author Matej Bizjak
 */

@ApplicationScoped
public class NatsConnection {
    private static final Map<String, Connection> connections = new HashMap<>();
    private static final Logger LOG = Logger.getLogger(NatsConnection.class.getName());

    public static Connection getConnection(String name) {
        Connection connection = connections.get(name);
        if (connection == null) {
            LOG.severe("Cannot find a connection with a name: " + name);
        }
        return connection;
    }

    public static void establishConnection(NatsConnectionConfig config) {
        try {
            Connection connection = Nats.connect(config.toOptionsBuilder().build());
            connections.put(config.getName(), connection);
            LOG.info(String.format("Connection to a NATS server/cluster named '%s' was created successfully", config.getName()));
        } catch (Exception e) {
            LOG.severe(String.format("Cannot create a connection to a NATS server/cluster named '%s': %s", config.getName(), e.getLocalizedMessage()));
        }
    }
}
