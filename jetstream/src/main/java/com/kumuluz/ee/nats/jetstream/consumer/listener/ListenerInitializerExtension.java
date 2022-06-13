package com.kumuluz.ee.nats.jetstream.consumer.listener;

import com.kumuluz.ee.nats.common.connection.NatsConnection;
import com.kumuluz.ee.nats.common.connection.NatsConnectionCoordinator;
import com.kumuluz.ee.nats.common.exception.NatsListenerException;
import com.kumuluz.ee.nats.common.util.SerDes;
import com.kumuluz.ee.nats.jetstream.NatsJetStreamExtension;
import com.kumuluz.ee.nats.jetstream.annotations.JetStreamListener;
import com.kumuluz.ee.nats.jetstream.context.JetStreamContextFactory;
import com.kumuluz.ee.nats.jetstream.management.StreamManagement;
import com.kumuluz.ee.nats.jetstream.util.AnnotatedInstance;
import io.nats.client.*;
import io.nats.client.api.ConsumerConfiguration;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Matej Bizjak
 */

public class ListenerInitializerExtension implements Extension {

    private static final Logger LOG = Logger.getLogger(ListenerInitializerExtension.class.getName());

    List<AnnotatedInstance<JetStreamListener>> instanceList = new ArrayList<>();

    public <T> void processStreamListeners(@Observes ProcessBean<T> processBean) {
        for (Method method : processBean.getBean().getBeanClass().getMethods()) {
            if (method.getAnnotation(JetStreamListener.class) != null) {
                JetStreamListener annotation = method.getAnnotation(JetStreamListener.class);
                instanceList.add(new AnnotatedInstance<>(processBean.getBean(), method, annotation));
            }
        }
    }

    public void after(@Observes AfterDeploymentValidation adv, BeanManager beanManager) {
        if (!NatsJetStreamExtension.isExtensionEnabled()) {
            return;
        }

        // establish connections
        if (!NatsConnection.connectionsAlreadyEstablished()) {  // Nats Core extension might have been used  TODO stestiraj če uporabiš oba extensiona - lahko moreš nastavit prioriteto enemu višjo
            NatsConnectionCoordinator.establishAll();
        }
        StreamManagement.establishAll();
        // then, create subscriptions

        for (AnnotatedInstance<JetStreamListener> inst : instanceList) {
            LOG.info("Found method " + inst.getMethod().getName() + " in class " +
                    inst.getMethod().getDeclaringClass());
        }

        for (AnnotatedInstance<JetStreamListener> inst : instanceList) {
            Method method = inst.getMethod();

            if (method.getParameterCount() != 1) {
                throw new NatsListenerException(String.format("Listener method must have exactly 1 parameter! Cause: %s"
                        , method));
            }

            Object reference = beanManager.getReference(inst.getBean(), method.getDeclaringClass()
                    , beanManager.createCreationalContext(inst.getBean()));
            Object[] args = new Object[method.getParameterCount()];

            JetStreamListener annotation = inst.getAnnotation();
            Connection connection = NatsConnection.getConnection(annotation.connection());
            try {
                JetStream jetStream = JetStreamContextFactory.getInstance().getContext(annotation.connection(), annotation.context());
                Dispatcher dispatcher = connection.createDispatcher();

                MessageHandler handler = msg -> {
                    Object receivedMsg;
                    try {
                        receivedMsg = SerDes.deserialize(msg.getData(), method.getParameterTypes()[0]);
                        args[0] = receivedMsg;
                    } catch (IOException e) {
                        throw new NatsListenerException(String.format("Cannot deserialize the message as class %s!"
                                , method.getParameterTypes()[0].getSimpleName()), e);
                    }
                    try {
                        method.invoke(reference, args);
//                        msg.ackSync();  // TODO
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        throw new NatsListenerException(String.format("Method %s could not be invoked.", method.getName()), e);
                    }
                };

                ConsumerConfiguration consumerConfiguration = ConsumerConfiguration
                        .builder()
                        .build(); // TODO

                PushSubscribeOptions.Builder builder = PushSubscribeOptions.builder();
                builder.configuration(consumerConfiguration);
                builder.ordered(annotation.ordered());
                builder.deliverSubject(annotation.deliverSubject());
                builder.deliverGroup(annotation.deliverGroup());
                builder.bind(annotation.bind());
                builder.stream(annotation.stream());
                builder.durable(annotation.durable());  // TODO durable consumer mora biti vnaprej specificiran. glej NatsJsPushSubBindDurable v Java nats examplih. to verjetno ni smiselno, da se dela prek extensiona
                PushSubscribeOptions pushSubscribeOptions = builder.build();

                jetStream.subscribe(annotation.subject(), annotation.queue(), dispatcher, handler, true, pushSubscribeOptions);  // TODO autoAck je zdaj fiksno na true

            } catch (JetStreamApiException | IOException e) {
                LOG.severe(String.format("Cannot create a JetStream context for connection: %s", annotation.connection()));
            }
        }
    }
}
