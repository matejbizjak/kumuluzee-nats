package com.kumuluz.ee.nats.jetstream.annotations;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * @author Matej Bizjak
 */

@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JetStreamSubscription {

    @Nonbinding String connection() default "";

    @Nonbinding String subject() default "";

    @Nonbinding String queue() default "";

    @Nonbinding boolean autoAck() default true;

    @Nonbinding String durable() default "";
}
