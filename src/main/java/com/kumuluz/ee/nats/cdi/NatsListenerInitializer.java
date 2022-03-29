package com.kumuluz.ee.nats.cdi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kumuluz.ee.nats.annotations.NatsListener;
import com.kumuluz.ee.nats.annotations.Subject;
import com.kumuluz.ee.nats.connection.NatsConnection;
import com.kumuluz.ee.nats.connection.config.SingleNatsConnectionConfig;
import com.kumuluz.ee.nats.exception.NatsClientDefinitionException;
import com.kumuluz.ee.nats.exception.NatsListenerException;
import com.kumuluz.ee.nats.util.SerDes;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;

import javax.annotation.Priority;
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
 * Finds methods which are annotated with a Subject annotations and their class is annotated with NatsListener annotation.
 * And initializes them as listeners to NATS connections.
 *
 * @author Matej Bizjak
 */

public class NatsListenerInitializer implements Extension {

    private static final Logger LOG = Logger.getLogger(NatsListenerInitializer.class.getName());
    List<ListenerMethod> listenerMethods = new ArrayList<>();

    public <T> void processStreamListeners(@Observes ProcessBean<T> processBean) {
        Class<?> aClass = processBean.getBean().getBeanClass();
        if (aClass.isAnnotationPresent(NatsListener.class)) {
            NatsListener natsListenerAnnotation = aClass.getAnnotation(NatsListener.class);
            for (Method method : aClass.getMethods()) {
                if (method.isAnnotationPresent(Subject.class)) {
                    Subject subjectAnnotation = method.getAnnotation(Subject.class);
                    listenerMethods.add(new ListenerMethod(processBean.getBean(), method, subjectAnnotation
                            , natsListenerAnnotation));
                }
            }
        }
    }

    public void after(@Observes @Priority(2600) AfterDeploymentValidation adv, BeanManager beanManager) {
        for (ListenerMethod inst : listenerMethods) {
            LOG.info("Found method " + inst.getMethod().getName() + " in class " +
                    inst.getMethod().getDeclaringClass());
        }

        for (ListenerMethod inst : listenerMethods) {
            Method method = inst.getMethod();

            if (method.getParameterCount() != 1) {
                throw new NatsListenerException(String.format("Listener method must have exactly 1 parameter! Cause: %s"
                        , method));
            }

            Subject subjectAnnotation = inst.getSubjectAnnotation();
            NatsListener natsListenerAnnotation = inst.getNatsListenerAnnotation();

            String subjectName = subjectAnnotation.value();
            if (subjectName.isEmpty()) {
                throw new NatsListenerException("The subject is not valid.");  // TODO preveri če dela tudi s praznim subject-om
            }
            String connectionName = subjectAnnotation.connection();
            if (connectionName.isEmpty()) {
                connectionName = natsListenerAnnotation.connection();
                if (connectionName.isEmpty()) {
                    connectionName = SingleNatsConnectionConfig.DEFAULT_NAME;
                }
            }
            String queueName = subjectAnnotation.queue();
            if (queueName.isEmpty()) {
                queueName = natsListenerAnnotation.queue();
            }

            Class<?> methodReturnType = method.getReturnType();
            boolean isVoid = methodReturnType.equals(Void.class) || methodReturnType.equals(void.class);

            Connection connection = NatsConnection.getConnection(connectionName);

            Object reference = beanManager.getReference(inst.getBean(), method.getDeclaringClass()
                    , beanManager.createCreationalContext(inst.getBean()));
            Object[] args = new Object[method.getParameterCount()];

            Dispatcher dispatcher = connection.createDispatcher(msg -> {

                Object receivedMsg;
                try {
                    receivedMsg = SerDes.deserialize(msg.getData(), method.getParameterTypes()[0]);
                    args[0] = receivedMsg;
                } catch (IOException e) {
                    throw new NatsListenerException(String.format("Cannot deserialize the message as class %s!"
                            , method.getParameterTypes()[0].getSimpleName()), e);
                }

                Object responseMsg;
                try {
                    responseMsg = method.invoke(reference, args);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new NatsListenerException("Method could not be invoked.", e);
                }

                if (!isVoid && msg.getReplyTo() != null && !msg.getReplyTo().isEmpty()) {
                    try {
                        connection.publish(msg.getReplyTo(), SerDes.serialize(responseMsg));
                    } catch (JsonProcessingException e) {
                        throw new NatsClientDefinitionException("Cannot serialize the response message object", e);
                    }
                }
            });

            if (queueName != null && !queueName.isEmpty()) {
                dispatcher.subscribe(subjectName, queueName);
            } else {
                dispatcher.subscribe(subjectName);
            }
        }
    }
}
