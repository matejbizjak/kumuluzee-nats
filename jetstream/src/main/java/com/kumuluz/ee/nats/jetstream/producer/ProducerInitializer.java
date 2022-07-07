package com.kumuluz.ee.nats.jetstream.producer;

import com.kumuluz.ee.nats.jetstream.annotations.JetStreamProducer;
import com.kumuluz.ee.nats.jetstream.context.ContextFactory;
import io.nats.client.JetStream;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Producer for {@link JetStreamProducer} annotation.
 *
 * @author Matej Bizjak
 */

@Dependent
public class ProducerInitializer {

    @Produces
    @JetStreamProducer
    public static JetStream getProducer(InjectionPoint injectionPoint) {
        JetStreamProducer annotation = injectionPoint.getAnnotated().getAnnotation(JetStreamProducer.class);
        return ContextFactory.getInstance().getContext(annotation.connection(), annotation.context());
    }
}
