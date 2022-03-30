package com.kumuluz.ee.nats.connection;

import com.kumuluz.ee.nats.connection.config.NatsConfigLoader;
import com.kumuluz.ee.nats.connection.config.NatsConnectionConfig;

import java.util.Set;
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
        Set<NatsConnectionConfig> configs = natsConfigLoader.getConnectionConfigs();
        if (configs.size() > 0) {
            ExecutorService executorService = Executors.newFixedThreadPool(configs.size());
            configs.forEach(config -> executorService.submit(() -> NatsConnection.establishConnection(config)));
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
