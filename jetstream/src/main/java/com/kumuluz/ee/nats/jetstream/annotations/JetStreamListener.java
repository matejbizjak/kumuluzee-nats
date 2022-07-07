package com.kumuluz.ee.nats.jetstream.annotations;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Annotation for defining method as a NATS JetStream Listener.
 *
 * @author Matej Bizjak
 */

@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JetStreamListener {

    @Nonbinding String connection() default "default";

    @Nonbinding String context() default "default";

    @Nonbinding String stream() default "";

    @Nonbinding String subject() default "";

    @Nonbinding String queue() default "";

    @Nonbinding boolean doubleAck() default false;

    @Nonbinding boolean bind() default false;

    @Nonbinding String durable() default "";

    @Nonbinding boolean ordered() default false;
}
