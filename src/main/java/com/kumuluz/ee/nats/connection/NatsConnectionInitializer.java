package com.kumuluz.ee.nats.connection;

import com.kumuluz.ee.nats.connection.config.NatsConfigLoader;
import com.kumuluz.ee.nats.connection.config.NatsConnectionConfig;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Extension;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Matej Bizjak
 */

public class NatsConnectionInitializer implements Extension {

    private static final Logger LOG = Logger.getLogger(NatsConnectionInitializer.class.getName());

    void after(@Observes AfterDeploymentValidation adv) {
        NatsConfigLoader natsConfigLoader = NatsConfigLoader.getInstance();
        natsConfigLoader.readConfiguration();
        Set<NatsConnectionConfig> configs = natsConfigLoader.getConfigs();
        configs.forEach(NatsConnection::establishConnection);
    }
}
