package com.kumuluz.ee.nats.jetstream;

import com.kumuluz.ee.common.Extension;
import com.kumuluz.ee.common.config.EeConfig;
import com.kumuluz.ee.common.dependencies.EeExtensionDef;
import com.kumuluz.ee.common.wrapper.KumuluzServerWrapper;
import com.kumuluz.ee.configuration.utils.ConfigurationUtil;

import java.util.logging.Logger;

@EeExtensionDef(group = "nats-jetstream", name = "NATS JetStream")
public class JetStreamExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(JetStreamExtension.class.getName());

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
        return config.getBoolean("kumuluzee.nats.enabled").orElse(true)
                && config.getBoolean("kumuluzee.nats.jetStream").orElse(true);
    }
}
