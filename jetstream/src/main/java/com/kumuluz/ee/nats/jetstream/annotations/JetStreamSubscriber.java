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
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface JetStreamSubscriber {

    @Nonbinding String connection() default "default";

    @Nonbinding String context() default "default";

    @Nonbinding String stream() default "";

    @Nonbinding String subject() default "";

    @Nonbinding boolean autoAck() default true;

    @Nonbinding String durable() default "";
}
