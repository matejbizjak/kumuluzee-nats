package com.kumuluz.ee.nats.jetstream.annotations;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Annotation for injecting a NATS JetStream subscriber (pull listener).
 *
 * @author Matej Bizjak
 */

@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JetStreamSubscriber {

    @Nonbinding String connection() default "default";

    @Nonbinding String context() default "default";

    @Nonbinding String stream() default "";

    @Nonbinding String subject() default "";

    @Nonbinding String durable() default "";

    @Nonbinding boolean bind() default false;
}
