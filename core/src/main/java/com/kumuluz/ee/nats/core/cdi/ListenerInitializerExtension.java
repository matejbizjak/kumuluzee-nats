package com.kumuluz.ee.nats.core.cdi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kumuluz.ee.nats.common.connection.NatsConnection;
import com.kumuluz.ee.nats.common.connection.config.SingleConnectionConfig;
import com.kumuluz.ee.nats.common.exception.DefinitionException;
import com.kumuluz.ee.nats.common.exception.InvocationException;
import com.kumuluz.ee.nats.common.exception.SerializationException;
import com.kumuluz.ee.nats.common.util.AnnotatedInstance;
import com.kumuluz.ee.nats.common.util.CollectionSerDes;
import com.kumuluz.ee.nats.common.util.SerDes;
import com.kumuluz.ee.nats.core.CoreExtension;
import com.kumuluz.ee.nats.core.annotations.NatsListener;
import com.kumuluz.ee.nats.core.annotations.Subject;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;

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
 * Finds methods which are annotated with a {@link Subject} annotations and their class is annotated with {@link NatsListener} annotation.
 * Also initializes them as listeners to previously created NATS connections.
 *
 * @author Matej Bizjak
 */

public class ListenerInitializerExtension implements Extension {

    private static final Logger LOG = Logger.getLogger(ListenerInitializerExtension.class.getName());
    private final List<AnnotatedInstance<Subject, NatsListener>> INSTANCES = new ArrayList<>();

    public <T> void processListeners(@Observes ProcessBean<T> processBean) {
        Class<?> aClass = processBean.getBean().getBeanClass();
        if (aClass.isAnnotationPresent(NatsListener.class)) {
            NatsListener natsListenerAnnotation = aClass.getAnnotation(NatsListener.class);
            for (Method method : aClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Subject.class)) {
                    Subject subjectAnnotation = method.getAnnotation(Subject.class);
                    INSTANCES.add(new AnnotatedInstance<>(processBean.getBean(), method, subjectAnnotation
                            , natsListenerAnnotation));
                }
            }
        }
    }

    public void after(@Observes AfterDeploymentValidation adv, BeanManager beanManager) {
        if (!CoreExtension.isExtensionEnabled()) {
            return;
        }

        for (AnnotatedInstance<Subject, NatsListener> inst : INSTANCES) {
            LOG.info(String.format("Found Core listener method %s in class %s.", inst.getMethod().getName(), inst.getMethod().getDeclaringClass()));
        }

        for (AnnotatedInstance<Subject, NatsListener> inst : INSTANCES) {
            Method method = inst.getMethod();

            if (method.getParameterCount() != 1) {
                throw new DefinitionException(String.format("Listener method %s in class %s must have exactly 1 parameter."
                        , method.getName(), method.getDeclaringClass().getName()));
            }

            Subject subjectAnnotation = inst.getAnnotation1();
            NatsListener natsListenerAnnotation = inst.getAnnotation2();

            String subjectName = subjectAnnotation.value();
            if (subjectName.isEmpty()) {
                throw new DefinitionException(String.format("Subject cannot be null or empty at listener method %s in class %s."
                        , method.getName(), method.getDeclaringClass().getName()));
            }

            String connectionName = subjectAnnotation.connection();
            if (connectionName.isEmpty()) {
                connectionName = natsListenerAnnotation.connection();
                if (connectionName.isEmpty()) {
                    connectionName = SingleConnectionConfig.DEFAULT_NAME;
                }
            }
            String queueName = subjectAnnotation.queue();
            if (queueName.isEmpty()) {
                queueName = natsListenerAnnotation.queue();
            }

            Class<?> methodReturnType = method.getReturnType();
            boolean isVoid = methodReturnType.equals(Void.class) || methodReturnType.equals(void.class);

            Connection connection = NatsConnection.getConnection(connectionName);
            if (connection == null) {
                LOG.severe(String.format("Cannot establish a NATS Core listener for method %s class %s and connection %s, because the connection was not established."
                        , method.getName(), method.getDeclaringClass().getName(), connectionName));
                continue;
            }

            Object reference = beanManager.getReference(inst.getBean(), method.getDeclaringClass()
                    , beanManager.createCreationalContext(inst.getBean()));
            Object[] args = new Object[method.getParameterCount()];

            Dispatcher dispatcher = connection.createDispatcher(msg -> {
                Object receivedMsg;
                try {
                    receivedMsg = SerDes.deserialize(msg.getData(), CollectionSerDes.getCollectionParameterType(method));
                    args[0] = receivedMsg;
                } catch (IOException e) {
                    throw new SerializationException(String
                            .format("Cannot deserialize the message as class %s for subject %s and connection %s."
                                    , method.getParameterTypes()[0].getName(), msg.getSubject()
                                    , msg.getConnection().getConnectedUrl()
                            ), e);
                }

                Object responseMsg;
                try {
                    responseMsg = method.invoke(reference, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new InvocationException(String
                            .format("Method %s could not be invoked for subject %s and connection %s."
                                    , method.getName(), msg.getSubject(), msg.getConnection().getConnectedUrl()
                            ), e);
                }

                if (!isVoid && msg.getReplyTo() != null && !msg.getReplyTo().isEmpty()) {
                    try {
                        connection.publish(msg.getReplyTo(), SerDes.serialize(responseMsg));
                    } catch (JsonProcessingException e) {
                        throw new SerializationException(String
                                .format("Cannot serialize the response message as object %s for subject %s and connection %s."
                                        , method.getReturnType().getName(), msg.getSubject()
                                        , msg.getConnection().getConnectedUrl()
                                ), e);
                    }
                }
            });

            if (queueName != null && !queueName.isEmpty()) {
                dispatcher.subscribe(subjectName, queueName);
            } else {
                dispatcher.subscribe(subjectName);
            }

            // disconnect
            Runtime.getRuntime().addShutdownHook(new Thread(() -> dispatcher.unsubscribe(subjectName)));
        }
    }
}
