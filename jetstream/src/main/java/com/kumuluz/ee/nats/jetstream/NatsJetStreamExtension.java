package com.kumuluz.ee.nats.jetstream;

import com.kumuluz.ee.common.Extension;
import com.kumuluz.ee.common.config.EeConfig;
import com.kumuluz.ee.common.dependencies.EeExtensionDef;
import com.kumuluz.ee.common.wrapper.KumuluzServerWrapper;
import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamOptions;
import io.nats.client.Nats;

import java.io.IOException;
import java.util.logging.Logger;

@EeExtensionDef(group = "nats", name = "NATS JetStream")
public class NatsJetStreamExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(NatsJetStreamExtension.class.getName());

    Connection connection;

    {
        try {
            connection = Nats.connect();
            JetStreamOptions jetStreamOptions = JetStreamOptions.DEFAULT_JS_OPTIONS;
            JetStream jetStream = connection.jetStream(jetStreamOptions);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void load() {
    }

    @Override
    public void init(KumuluzServerWrapper kumuluzServerWrapper, EeConfig eeConfig) {
        LOGGER.info("Initialising KumuluzEE NATS JetStream extension.");
    }

    @Override
    public boolean isEnabled() {
        return isExtensionEnabled();
    }

    public static boolean isExtensionEnabled() {
        ConfigurationUtil config = ConfigurationUtil.getInstance();
        return config.getBoolean("kumuluzee.nats-jetstream.enabled")
                .orElse(true);
    }
}
