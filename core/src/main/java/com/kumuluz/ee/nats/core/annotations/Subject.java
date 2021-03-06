package com.kumuluz.ee.nats.core.annotations;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Annotation for defining basic properties like subject and connection for both NATS Core producers and consumers.
 *
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

    /**
     * @return Time to wait for the response.
     */
    @Nonbinding String responseTimeout() default "";
}
