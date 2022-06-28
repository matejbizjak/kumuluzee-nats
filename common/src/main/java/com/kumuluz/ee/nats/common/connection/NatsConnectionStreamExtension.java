package com.kumuluz.ee.nats.common.connection;

import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import com.kumuluz.ee.nats.common.connection.config.NatsConfigLoader;
import com.kumuluz.ee.nats.common.connection.config.NatsConnectionConfig;
import com.kumuluz.ee.nats.common.management.StreamManagement;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Extension for establishing connections to NATS servers and updating JetStream streams
 *
 * @author Matej Bizjak
 */

public class NatsConnectionStreamExtension implements Extension {

    private static final ConfigurationUtil config = ConfigurationUtil.getInstance();

    void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscovery) {
        if (!isNatsEnabled()) {
            return;
        }
        establishAllConnections();
        if (isJetStreamEnabled()) {
            StreamManagement.establishAll();
        }
    }

    private static boolean isNatsEnabled() {
        return config.getBoolean("kumuluzee.nats.enabled").orElse(true);
    }

    private static boolean isJetStreamEnabled() {
        return isNatsEnabled() && config.getBoolean("kumuluzee.nats.jetStream").orElse(true);
    }

    public static void establishAllConnections() {
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
