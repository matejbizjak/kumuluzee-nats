package com.kumuluz.ee.nats.common.annotations;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Annotation for specifying the consumer configuration with an option to override specific values.
 *
 * @author Matej Bizjak
 */

@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface ConsumerConfig {

    @Nonbinding String name() default "";

    @Nonbinding ConfigurationOverride[] configOverrides() default {};

}
