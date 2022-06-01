package com.kumuluz.ee.nats.core.annotations;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * @author Matej Bizjak
 */

@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface Subject {

    /**
     * @return The subject to subscribe to.
     */
    @Nonbinding String value() default "";

    /**
     * @return The connection to use.
     */
    @Nonbinding String connection() default "";

    /**
     * @return The queue of the consumer.
     */
    @Nonbinding String queue() default "";
}
