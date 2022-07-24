package com.kumuluz.ee.nats.jetstream.consumer.subscriber;

import com.kumuluz.ee.nats.common.annotations.ConsumerConfig;
import com.kumuluz.ee.nats.common.util.AnnotatedInstance;
import com.kumuluz.ee.nats.jetstream.JetStreamExtension;
import com.kumuluz.ee.nats.jetstream.annotations.JetStreamSubscriber;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Finds methods annotated with a {@link JetStreamSubscriber} annotations and initializes them as subscribers (pull consumers) to previously created NATS connections.
 *
 * @author Matej Bizjak
 */


public class SubscriberInitializerExtension implements Extension {

    private final List<AnnotatedInstance<JetStreamSubscriber, ConsumerConfig>> INSTANCES = new ArrayList<>();

    public <T> void processStreamSubscribers(@Observes ProcessBean<T> processBean) {
        for (Field field : processBean.getBean().getBeanClass().getDeclaredFields()) {
            if (field.getAnnotation(JetStreamSubscriber.class) != null) {
                JetStreamSubscriber jetStreamSubscriberAnnotation = field.getAnnotation(JetStreamSubscriber.class);
                ConsumerConfig consumerConfigAnnotation = null;
                if (field.getAnnotation(ConsumerConfig.class) != null) {
                    consumerConfigAnnotation = field.getAnnotation(ConsumerConfig.class);
                }
                INSTANCES.add(new AnnotatedInstance<>(processBean.getBean(), null, jetStreamSubscriberAnnotation, consumerConfigAnnotation));
            }
        }
    }

    public void after(@Observes AfterDeploymentValidation adv) {
        if (!JetStreamExtension.isExtensionEnabled()) {
            return;
        }

        for (AnnotatedInstance<JetStreamSubscriber, ConsumerConfig> inst : INSTANCES) {
            SubscriberFactory.getInstance().getSubscription(inst.getAnnotation1(), inst.getAnnotation2());
        }
    }
}
