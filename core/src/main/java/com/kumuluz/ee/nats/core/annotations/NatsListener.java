package com.kumuluz.ee.nats.core.annotations;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Annotation for defining class as a NATS Core Listener.
 *
 * @author Matej Bizjak
 */

@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NatsListener {

    @Nonbinding String connection() default "";

    @Nonbinding String queue() default "";
}
