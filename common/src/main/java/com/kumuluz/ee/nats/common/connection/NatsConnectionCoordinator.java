package com.kumuluz.ee.nats.common.connection;

import com.kumuluz.ee.nats.common.connection.config.NatsConfigLoader;
import com.kumuluz.ee.nats.common.connection.config.NatsConnectionConfig;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Coordinates configuration reading and connection establishing.
 * @author Matej Bizjak
 */

public class NatsConnectionCoordinator {

    public static void establishAll() {
        NatsConfigLoader natsConfigLoader = NatsConfigLoader.getInstance();
        natsConfigLoader.readConfiguration();
        HashMap<String, NatsConnectionConfig> connectionConfigs = natsConfigLoader.getConnectionConfigs();
        if (connectionConfigs.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(connectionConfigs.size());
            connectionConfigs.forEach((name, config) -> executorService.submit(() -> NatsConnection.establishConnection(config)));
            waitForCompletion(executorService);
        }
    }

    private static void waitForCompletion(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
