package com.kumuluz.ee.nats.common.annotations;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * @author Matej Bizjak
 */

@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface ConsumerConfig {

    @Nonbinding String name() default "default";

    @Nonbinding ConfigurationOverride[] configOverrides() default {};

}
