package com.kumuluz.ee.nats;

import com.kumuluz.ee.common.Extension;
import com.kumuluz.ee.common.config.EeConfig;
import com.kumuluz.ee.common.dependencies.EeExtensionDef;
import com.kumuluz.ee.common.wrapper.KumuluzServerWrapper;

import java.util.logging.Logger;

/**
 * @author Matej Bizjak
 */

@EeExtensionDef(group = "nats.core", name = "NATS Core")
//@EeComponentDependency(EeComponentType.CDI)
public class NatsExtension implements Extension {

    private static final Logger LOGGER = Logger.getLogger(NatsExtension.class.getName());

    @Override
    public void load() {
    }

    @Override
    public void init(KumuluzServerWrapper kumuluzServerWrapper, EeConfig eeConfig) {
        LOGGER.info("Initialising KumuluzEE NATS extension.");
    }
}
