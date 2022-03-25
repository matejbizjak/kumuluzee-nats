package com.kumuluz.ee.nats.connection;

import com.kumuluz.ee.nats.connection.config.NatsConnectionConfig;
import io.nats.client.Connection;
import io.nats.client.Options;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
        Options.Builder builder = new Options.Builder();
        // TODO
    }
}
