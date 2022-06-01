package com.kumuluz.ee.nats.core.annotations;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Matej Bizjak
 */

@Documented
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface NatsClient {
    NatsClient LITERAL = new NatsClient.NatsClientLiteral();

    public static class NatsClientLiteral extends AnnotationLiteral<NatsClient> implements NatsClient {
        public NatsClientLiteral() {

        }
    }

}
