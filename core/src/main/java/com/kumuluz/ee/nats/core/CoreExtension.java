package com.kumuluz.ee.nats.core;

import com.kumuluz.ee.common.Extension;
import com.kumuluz.ee.common.config.EeConfig;
import com.kumuluz.ee.common.dependencies.EeExtensionDef;
import com.kumuluz.ee.common.wrapper.KumuluzServerWrapper;
import com.kumuluz.ee.configuration.utils.ConfigurationUtil;

import java.util.logging.Logger;

/**
 * @author Matej Bizjak
 */

@EeExtensionDef(group = "nats-core", name = "NATS Core")
public class CoreExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(CoreExtension.class.getName());

    @Override
    public void load() {
    }

    @Override
    public void init(KumuluzServerWrapper kumuluzServerWrapper, EeConfig eeConfig) {
        LOGGER.info("Initialising KumuluzEE NATS Core extension.");
    }

    @Override
    public boolean isEnabled() {
        return isExtensionEnabled();
    }

    public static boolean isExtensionEnabled() {
        ConfigurationUtil config = ConfigurationUtil.getInstance();
        return config.getBoolean("kumuluzee.nats.enabled")
                .orElse(true);
    }
}
