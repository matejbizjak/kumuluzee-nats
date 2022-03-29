package com.kumuluz.ee.nats.connection;

import com.kumuluz.ee.nats.NatsExtension;
import com.kumuluz.ee.nats.connection.config.NatsConfigLoader;
import com.kumuluz.ee.nats.connection.config.NatsConnectionConfig;

import javax.annotation.Priority;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Extension;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Coordinates configuration reading and connection establishing.
 * @author Matej Bizjak
 */

public class NatsConnectionInitializer implements Extension {

    void after(@Observes @Priority(2500) AfterDeploymentValidation adv) {
        if (!NatsExtension.isExtensionEnabled()) {
            return;
        }

        NatsConfigLoader natsConfigLoader = NatsConfigLoader.getInstance();
        natsConfigLoader.readConfiguration();
        Set<NatsConnectionConfig> configs = natsConfigLoader.getConfigs();

        ExecutorService executorService = Executors.newFixedThreadPool(configs.size());
        configs.forEach(config -> executorService.execute(() -> NatsConnection.establishConnection(config)));
//        configs.forEach(NatsConnection::establishConnection);
    }
}
