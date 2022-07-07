package com.kumuluz.ee.nats.common.annotations;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
/**
 * Annotation for overriding specific values of the consumer configuration.
 *
 * @author Matej Bizjak
 */

@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigurationOverride {
    String key();

    String value();
}
