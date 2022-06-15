package com.kumuluz.ee.nats.common.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Matej Bizjak
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurationOverride {
    String key();

    String value();
}
