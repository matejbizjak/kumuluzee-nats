package com.kumuluz.ee.nats.jetstream.consumer.listener;

import com.kumuluz.ee.nats.common.annotations.ConsumerConfig;
import com.kumuluz.ee.nats.common.connection.NatsConnection;
import com.kumuluz.ee.nats.common.connection.config.ConnectionConfig;
import com.kumuluz.ee.nats.common.connection.config.GeneralConfig;
import com.kumuluz.ee.nats.common.connection.config.NatsConfigLoader;
import com.kumuluz.ee.nats.common.connection.config.StreamConsumerConfiguration;
import com.kumuluz.ee.nats.common.exception.DefinitionException;
import com.kumuluz.ee.nats.common.exception.InvocationException;
import com.kumuluz.ee.nats.common.exception.SerializationException;
import com.kumuluz.ee.nats.common.management.StreamManagement;
import com.kumuluz.ee.nats.common.util.AnnotatedInstance;
import com.kumuluz.ee.nats.common.util.CollectionSerDes;
import com.kumuluz.ee.nats.common.util.SerDes;
import com.kumuluz.ee.nats.jetstream.JetStreamExtension;
import com.kumuluz.ee.nats.jetstream.annotations.JetStreamListener;
import com.kumuluz.ee.nats.jetstream.context.ContextFactory;
import com.kumuluz.ee.nats.jetstream.wrappers.JetStreamMessage;
import io.nats.client.*;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.StreamInfo;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Finds methods annotated with a {@link JetStreamListener} annotations and initializes them as listeners to previously created NATS connections.
 *
 * @author Matej Bizjak
 */

public class ListenerInitializerExtension implements Extension {

    private static final Logger LOG = Logger.getLogger(ListenerInitializerExtension.class.getName());
    private final List<AnnotatedInstance<JetStreamListener, ConsumerConfig>> INSTANCES = new ArrayList<>();

    public <T> void processStreamListeners(@Observes ProcessBean<T> processBean) {
        for (Method method : processBean.getBean().getBeanClass().getMethods()) {
            if (method.getAnnotation(JetStreamListener.class) != null) {
                JetStreamListener jetStreamListenerAnnotation = method.getAnnotation(JetStreamListener.class);
                ConsumerConfig consumerConfigAnnotaion = null;
                if (method.getAnnotation(ConsumerConfig.class) != null) {
                    consumerConfigAnnotaion = method.getAnnotation(ConsumerConfig.class);
                }
                INSTANCES.add(new AnnotatedInstance<>(processBean.getBean(), method, jetStreamListenerAnnotation, consumerConfigAnnotaion));
            }
        }
    }

    public void after(@Observes AfterDeploymentValidation adv, BeanManager beanManager) {
        if (!JetStreamExtension.isExtensionEnabled()) {
            return;
        }

        for (AnnotatedInstance<JetStreamListener, ConsumerConfig> inst : INSTANCES) {
            LOG.info(String.format("Found JetStream listener method %s in class %s.", inst.getMethod().getName()
                    , inst.getMethod().getDeclaringClass().getName()));
        }

        for (AnnotatedInstance<JetStreamListener, ConsumerConfig> inst : INSTANCES) {
            Method method = inst.getMethod();
            GeneralConfig generalConfig = NatsConfigLoader.getInstance().getGeneralConfig();
            JetStreamListener jetStreamListenerAnnotation = inst.getAnnotation1();
            ConsumerConfig consumerConfigAnnotation = inst.getAnnotation2();

            //region Validation
            if (method.getParameterCount() < 1 || method.getParameterCount() > 2) {
                throw new DefinitionException(String.format("Listener method %s in class %s must have exactly 1 or 2 parameters."
                        , method.getName(), method.getDeclaringClass().getName()));
            }
            if (method.getParameterCount() == 2 && !method.getParameters()[1].getType().equals(JetStreamMessage.class)) {
                throw new DefinitionException(String
                        .format("The 2nd parameter of listener method %s in class %s must be of type JetStreamMessage."
                                , method.getName(), method.getDeclaringClass().getName()));
            }
            if (jetStreamListenerAnnotation.subject().isEmpty()) {
                throw new DefinitionException(String.format("Subject was not specified for listener method %s in class %s."
                        , method.getName(), method.getDeclaringClass().getName()));
            }
            if (jetStreamListenerAnnotation.stream().isEmpty()) {
                throw new DefinitionException(String.format("Stream was not specified for listener method %s in class %s."
                        , method.getName(), method.getDeclaringClass().getName()));
            }
            Connection connection = NatsConnection.getConnection(jetStreamListenerAnnotation.connection());
            if (connection == null) {
                LOG.severe(String.format("Cannot establish a NATS JetStream listener for method %s class %s and connection %s, because the connection was not established."
                        , method.getName(), method.getDeclaringClass().getName(), jetStreamListenerAnnotation.connection()));
                continue;
            }
            //endregion

            Object reference = beanManager.getReference(inst.getBean(), method.getDeclaringClass()
                    , beanManager.createCreationalContext(inst.getBean()));
            Object[] args = new Object[method.getParameterCount()];

            JetStream jetStream = ContextFactory.getInstance().getContext(jetStreamListenerAnnotation.connection()
                    , jetStreamListenerAnnotation.context());
            Dispatcher dispatcher = connection.createDispatcher();

            MessageHandler handler = msg -> {
                Object receivedMsg;
                try {
                    receivedMsg = SerDes.deserialize(msg.getData(), CollectionSerDes.getCollectionParameterType(method));
                    args[0] = receivedMsg;
                    if (method.getParameterCount() == 2) {
                        args[1] = new JetStreamMessage(msg);
                    }
                } catch (IOException e) {
                    exponentialNak(msg);
                    throw new SerializationException(String
                            .format("Cannot deserialize the message as class %s for subject %s and connection %s."
                                    , method.getParameterTypes()[0].getName(), msg.getSubject()
                                    , msg.getConnection().getConnectedUrl()
                            ), e);
                }
                try {
                    method.invoke(reference, args);
                    if (jetStreamListenerAnnotation.doubleAck()) {
                        ackSyncWithRetries(msg, generalConfig);
                    } else {
                        msg.ack();
                    }
                } catch (InvocationTargetException | IllegalAccessException e) {
                    exponentialNak(msg);
                    throw new InvocationException(String
                            .format("Method %s could not be invoked for subject %s and connection %s."
                                    , method.getName(), msg.getSubject(), msg.getConnection().getConnectedUrl()
                            ), e);
                }
            };

            //region Configuration
            PushSubscribeOptions.Builder builder = PushSubscribeOptions.builder()
                    .ordered(jetStreamListenerAnnotation.ordered())
                    .bind(jetStreamListenerAnnotation.bind())
                    .stream(jetStreamListenerAnnotation.stream())
                    .durable(jetStreamListenerAnnotation.durable());
            ConnectionConfig connectionConfig = NatsConfigLoader.getInstance().getConfigForConnection(jetStreamListenerAnnotation.connection());
            StreamConsumerConfiguration streamConsumerConfiguration = connectionConfig
                    .getStreamConsumerConfiguration(jetStreamListenerAnnotation.stream());
            if (streamConsumerConfiguration == null) {  // stream not specified in configuration
                StreamInfo streamInfo = null;
                try {
                    streamInfo = StreamManagement.getStreamInfoOrNullWhenNotExist(connection, jetStreamListenerAnnotation.stream());
                } catch (JetStreamApiException | IOException e) {
                    LOG.log(Level.SEVERE, String.format("There was a problem obtaining stream info for method %s in class %s."
                            , method.getName(), method.getDeclaringClass().getName()), e);
                }
                if (streamInfo == null) {  // check if stream already exists (was created previously)
                    throw new DefinitionException(String.format("Stream must be set and valid - at method %s in class %s."
                            , method.getName(), method.getDeclaringClass().getName()));
                }
            } else {  // stream specified in configuration
                Optional<ConsumerConfiguration> consumerConfiguration = streamConsumerConfiguration
                        .getAndCombineConsumerConfig(connection, jetStreamListenerAnnotation.durable(), consumerConfigAnnotation);
                consumerConfiguration.ifPresent(builder::configuration);
            }
            PushSubscribeOptions pushSubscribeOptions = builder.build();
            //endregion

            try {
                jetStream.subscribe(jetStreamListenerAnnotation.subject(), jetStreamListenerAnnotation.queue()
                        , dispatcher, handler, false, pushSubscribeOptions);
            } catch (JetStreamApiException | IOException e) {
                LOG.log(Level.SEVERE, String
                        .format("There was a problem with the JetStream listener at the method %s in class %s for connection %s."
                                , method.getName(), method.getDeclaringClass().getName(), jetStreamListenerAnnotation.connection()
                        ), e);
            }
        }
    }

    private void ackSyncWithRetries(Message msg, GeneralConfig generalConfig) {
        for (int retries = 0; ; retries++) {
            try {
                msg.ackSync(generalConfig.getAckConfirmationTimeout());
                return;
            } catch (InterruptedException | TimeoutException e) {
                if (retries < generalConfig.getAckConfirmationRetries()) {
                    try {
                        Thread.sleep(generalConfig.getAckConfirmationTimeout().toMillis());
                        LOG.log(Level.SEVERE, "Could not receive an ack confirmation from the server. Retrying...", e);
                    } catch (InterruptedException ex) {
                        LOG.log(Level.SEVERE, "Could not receive an ack confirmation from the server. Retrying...", ex);
                    }
                } else {
                    LOG.log(Level.SEVERE, "Could not receive an ack confirmation from the server. This was a final try.", e);
                }
            }
        }
    }

    private void exponentialNak(Message msg) {
        msg.nakWithDelay(Duration.ofSeconds((long) Math.pow(5, msg.metaData().deliveredCount())));
    }
}
