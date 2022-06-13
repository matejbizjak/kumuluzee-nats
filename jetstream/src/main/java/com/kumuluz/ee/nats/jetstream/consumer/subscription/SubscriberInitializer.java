package com.kumuluz.ee.nats.jetstream.consumer.subscription;

import com.kumuluz.ee.nats.jetstream.annotations.JetStreamSubscriber;
import io.nats.client.JetStreamSubscription;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * @author Matej Bizjak
 */

@Dependent
public class SubscriberInitializer {

    @Produces
    @JetStreamSubscriber
    public static JetStreamSubscription getSubscription(InjectionPoint injectionPoint) {
        JetStreamSubscriber annotation = injectionPoint.getAnnotated().getAnnotation(JetStreamSubscriber.class);
        return SubscriberFactory.getInstance().getSubscription(annotation);
    }

}
