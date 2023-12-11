package com.kumuluz.ee.nats.jetstream.invoker;

import com.kumuluz.ee.nats.common.connection.config.SingleConnectionConfig;
import com.kumuluz.ee.nats.common.exception.DefinitionException;
import com.kumuluz.ee.nats.common.util.SerDes;
import com.kumuluz.ee.nats.jetstream.annotations.JetStreamSubject;
import com.kumuluz.ee.nats.jetstream.annotations.RegisterJetStreamClient;
import com.kumuluz.ee.nats.jetstream.context.ContextFactory;
import io.nats.client.JetStream;
import io.nats.client.Message;
import io.nats.client.api.PublishAck;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Invokes NATS Client for methods of interfaces annotated with {@link com.kumuluz.ee.nats.jetstream.annotations.RegisterJetStreamClient}.
 *
 * @author Matej Bizjak
 */

public class JetStreamClientInvoker implements InvocationHandler {

    private static final Logger LOG = Logger.getLogger(JetStreamClientInvoker.class.getName());

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String connectionName = getAnnotatedConnection(method);
        String contextName = getContext(method);
        String subjectName = getAnnotatedSubject(method, args);
        boolean uniqueHeader = isHeaderUnique(method);
        Object payload = getPayload(method, args);
        Class<?> returnType = method.getReturnType();

        JetStream jetStream = ContextFactory.getInstance().getContext(connectionName, contextName);
        if (jetStream == null) {
            LOG.severe(String.format("Cannot invoke JetStream Client method %s in class %s for connection %s and context %s, because the connection was not established."
                    , method.getName(), method.getDeclaringClass().getName(), connectionName, contextName));
            return null;
        }

        NatsMessage.Builder builder = NatsMessage.builder();
        builder.data(SerDes.serialize(payload));
        builder.subject(subjectName);
        if (uniqueHeader) {
            String uniqueID = UUID.randomUUID().toString();
            Headers headers = new Headers().add("Nats-Msg-Id", uniqueID);
            builder.headers(headers);
        }
        Message message = builder.build();

        if (returnType.equals(PublishAck.class)) {  // synchronous publishing
            return jetStream.publish(message);
        } else {  // asynchronous publishing
            // we validated the correctness of the interface before (we know the type can only be CompletableFuture<PublishAck>)
            return jetStream.publishAsync(message);
        }
    }

    private String getAnnotatedConnection(Method method) {
        String connectionName;

        // declaring class annotation
        RegisterJetStreamClient registerJetStreamClientAnnotation = method.getDeclaringClass()
                .getAnnotation(RegisterJetStreamClient.class);
        connectionName = registerJetStreamClientAnnotation.connection();

        // method annotation variables
        JetStreamSubject jetStreamSubjectAnnotation = method.getAnnotation(JetStreamSubject.class);
        if (jetStreamSubjectAnnotation != null && !jetStreamSubjectAnnotation.connection().isEmpty()) {
            connectionName = jetStreamSubjectAnnotation.connection();
        }

        if (connectionName.isEmpty()) {
            connectionName = SingleConnectionConfig.DEFAULT_NAME;
        }

        return connectionName;
    }

    private String getContext(Method method) {
        String contextName;

        // declaring class annotation
        RegisterJetStreamClient registerJetStreamClientAnnotation = method.getDeclaringClass()
                .getAnnotation(RegisterJetStreamClient.class);
        contextName = registerJetStreamClientAnnotation.context();

        // method annotation variables
        JetStreamSubject jetStreamSubjectAnnotation = method.getAnnotation(JetStreamSubject.class);
        if (jetStreamSubjectAnnotation != null && !jetStreamSubjectAnnotation.context().isEmpty()) {
            contextName = jetStreamSubjectAnnotation.context();
        }

        if (contextName.isEmpty()) {
            contextName = "default";
        }

        return contextName;
    }

    private String getAnnotatedSubject(Method method, Object[] args) {
        String subject = null;
        // method annotation
        JetStreamSubject jetStreamSubjectAnnotation = method.getAnnotation(JetStreamSubject.class);
        if (jetStreamSubjectAnnotation != null) {
            subject = jetStreamSubjectAnnotation.value();
        }
        // parameter annotation - overrides the method annotation value if both exists
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(JetStreamSubject.class)) {
                return (String) args[i];
            }
        }
        return subject;
    }

    private boolean isHeaderUnique(Method method) {
        JetStreamSubject jetStreamSubjectAnnotation = method.getAnnotation(JetStreamSubject.class);
        if (jetStreamSubjectAnnotation != null) {
            return jetStreamSubjectAnnotation.uniqueMessageHeader();
        }
        return false;
    }

    private Object getPayload(Method method, Object[] args) {
        int index = 0;
        for (int i = 0; i < method.getParameters().length; i++) {
            if (!method.getParameters()[i].isAnnotationPresent(JetStreamSubject.class)) {
                index = i;
            }
        }
        return args[index];
    }
}
