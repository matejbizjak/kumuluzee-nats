package com.kumuluz.ee.nats.common.annotations;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Annotation for specifying new consumer configuration.
 * Copies values from the base consumer with an option to override certain values.
 *
 * @author Matej Bizjak
 */

@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface ConsumerConfig {

    /**
     * @return Base consumer configuration.
     */
    @Nonbinding String base() default "";

    /**
     * @return Overrided values.
     */
    @Nonbinding ConfigurationOverride[] configOverrides() default {};

}
