package com.kumuluz.ee.nats.common.connection;

import com.kumuluz.ee.nats.common.connection.config.NatsConnectionConfig;
import io.nats.client.Connection;
import io.nats.client.Nats;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Matej Bizjak
 */

@ApplicationScoped
public class NatsConnection {
    private static final HashMap<String, Connection> connections = new HashMap<>();
    private static final Logger LOG = Logger.getLogger(NatsConnection.class.getName());

    public static Connection getConnection(String name) {
        return connections.get(name);
    }

    public static void establishConnection(NatsConnectionConfig config) {
        try {
            Connection connection = Nats.connect(config.toOptionsBuilder().build());
            connections.put(config.getName(), connection);
            LOG.info(String.format("Connection to a NATS server/cluster %s was created successfully.", config.getName()));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, String.format("Cannot create a connection to a NATS server/cluster %s.", config.getName()), e);
        }
    }

    public static HashMap<String, Connection> getAllConnections() {
        return connections;
    }
}
