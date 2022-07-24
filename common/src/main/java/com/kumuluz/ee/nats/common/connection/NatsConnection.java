package com.kumuluz.ee.nats.common.connection;

import com.kumuluz.ee.nats.common.connection.config.ConfigLoader;
import com.kumuluz.ee.nats.common.connection.config.ConnectionConfig;
import com.kumuluz.ee.nats.common.connection.config.GeneralConfig;
import io.nats.client.Connection;
import io.nats.client.Nats;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for managing and establishing connections.
 * You can retrieve the connection by calling function getConnection(String name).
 *
 * @author Matej Bizjak
 */

public class NatsConnection {
    private static final HashMap<String, Connection> CONNECTIONS = new HashMap<>();
    private static final Logger LOG = Logger.getLogger(NatsConnection.class.getName());

    public static Connection getConnection(String name) {
        return CONNECTIONS.get(name);
    }

    public static void establishConnection(ConnectionConfig config) {
        GeneralConfig generalConfig = ConfigLoader.getInstance().getGeneralConfig();
        try {
            Connection connection = Nats.connect(config.toOptionsBuilder().build());
            CONNECTIONS.put(config.getName(), connection);
            LOG.info(String.format("Connection to a NATS server/cluster %s was created successfully.", config.getName()));

            // drain messages before disconnect
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    CompletableFuture<Boolean> drain = connection.drain(generalConfig.getDrainTimeout());
                    if (drain.get().equals(Boolean.TRUE)) {
                        LOG.info(String.format("Draining messages for connection %s completed successfully.", config.getName()));
                    } else {
                        LOG.severe(String.format("Draining messages for connection %s failed.", config.getName()));
                    }
                } catch (TimeoutException | InterruptedException | ExecutionException e) {
                    LOG.log(Level.SEVERE, String.format("Draining messages for connection %s failed.", config.getName()), e);
                }
            }));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, String.format("Cannot create a connection to a NATS server/cluster %s.", config.getName()), e);
        }
    }

    public static HashMap<String, Connection> getAllConnections() {
        return CONNECTIONS;
    }
}
