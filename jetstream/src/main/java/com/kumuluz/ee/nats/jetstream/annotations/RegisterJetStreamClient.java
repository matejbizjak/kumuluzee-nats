package com.kumuluz.ee.nats.jetstream.annotations;

import javax.enterprise.util.Nonbinding;
import java.lang.annotation.*;

/**
 * Annotation for defining interface as a NATS JetStream Client (producer).
 *
 * @author Matej Bizjak
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterJetStreamClient {

    @Nonbinding String connection() default "default";

    @Nonbinding String context() default "default";
}
