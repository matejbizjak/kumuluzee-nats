package com.kumuluz.ee.nats.jetstream.producer;

import com.kumuluz.ee.nats.jetstream.annotations.JetStreamProducer;
import io.nats.client.JetStream;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Producer for JetStreamProducer annotation.
 *
 * @author Matej Bizjak
 */

@Dependent
public class ProducerInitializer {

    @Produces
    @JetStreamProducer
    public static JetStream getProducer(InjectionPoint injectionPoint) {
        return ProducerFactory.getInstance().getProducer(injectionPoint);
    }
}