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
public @interface JetStreamListener {

    @Nonbinding String connection() default "";

    @Nonbinding String subject() default "";

    @Nonbinding String queue() default "";

    @Nonbinding boolean autoAck() default false; // TODO

    @Nonbinding boolean bind() default false;

    @Nonbinding String stream() default "";

    @Nonbinding String durable() default "";

    @Nonbinding String deliverGroup() default "";

    @Nonbinding String deliverSubject() default "";

    @Nonbinding boolean ordered() default false;
}