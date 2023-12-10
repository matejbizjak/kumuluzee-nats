package com.kumuluz.ee.nats.jetstream.annotations;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for injecting a NATS JetStream Client (producer).
 *
 * @author Matej Bizjak
 */

@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface JetStreamClient {
    JetStreamClient LITERAL = new JetStreamClient.JetStreamClientLiteral();

    class JetStreamClientLiteral extends AnnotationLiteral<JetStreamClient> implements JetStreamClient {
        public JetStreamClientLiteral() {

        }
    }

}
