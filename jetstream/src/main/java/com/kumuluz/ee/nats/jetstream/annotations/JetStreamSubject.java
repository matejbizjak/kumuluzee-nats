package com.kumuluz.ee.nats.jetstream.annotations;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;
import java.lang.annotation.*;

/**
 * Annotation for defining JetStream producer properties.
 *
 * @author Matej Bizjak
 */

@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface JetStreamSubject {

    /**
     * @return The subject to subscribe to.
     */
    @Nonbinding String value() default "";

    /**
     * @return The connection to use.
     */
    @Nonbinding String connection() default "";

    /**
     * @return The context to use.
     */
    @Nonbinding String context() default "";

    /**
     * @return Weather the unique header should be generated (Message Deduplication feature - Exactly one delivery).
     */
    @Nonbinding boolean uniqueMessageHeader() default false;

}
