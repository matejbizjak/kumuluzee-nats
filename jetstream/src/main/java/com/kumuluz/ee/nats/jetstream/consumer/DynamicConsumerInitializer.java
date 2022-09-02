package com.kumuluz.ee.nats.jetstream.consumer;

import com.kumuluz.ee.nats.jetstream.annotations.JetStreamDynamicConsumer;
import com.kumuluz.ee.nats.jetstream.context.ContextFactory;
import io.nats.client.JetStream;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Producer for {@link JetStreamDynamicConsumer} annotation.
 *
 * @author Matej Bizjak
 */

@Dependent
public class DynamicConsumerInitializer {

    @Produces
    @JetStreamDynamicConsumer
    public static JetStream getProducer(InjectionPoint injectionPoint) {
        JetStreamDynamicConsumer annotation = injectionPoint.getAnnotated().getAnnotation(JetStreamDynamicConsumer.class);
        return ContextFactory.getInstance().getContext(annotation.connection(), annotation.context());
    }
}
