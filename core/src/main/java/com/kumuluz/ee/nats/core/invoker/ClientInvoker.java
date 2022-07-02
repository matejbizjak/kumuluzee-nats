package com.kumuluz.ee.nats.core.invoker;

import com.kumuluz.ee.nats.common.connection.NatsConnection;
import com.kumuluz.ee.nats.common.connection.config.ConfigLoader;
import com.kumuluz.ee.nats.common.connection.config.SingleConnectionConfig;
import com.kumuluz.ee.nats.common.util.SerDes;
import com.kumuluz.ee.nats.core.annotations.RegisterNatsClient;
import com.kumuluz.ee.nats.core.annotations.Subject;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.NatsMessage;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author Matej Bizjak
 */

public class ClientInvoker implements InvocationHandler {

    private static final Logger LOG = Logger.getLogger(ClientInvoker.class.getName());

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String connectionName = getAnnotatedConnection(method);
        String subjectName = getAnnotatedSubject(method, args);
        Object payload = getPayload(method, args);
        Class<?> returnType = method.getReturnType();

        Connection connection = NatsConnection.getConnection(connectionName);
        if (connection == null) {
            LOG.severe(String.format("Cannot invoke NATS Client method %s in class %s for connection %s, because the connection was not established."
                    , method.getName(), method.getDeclaringClass().getName(), connectionName));
            return null;
        }

        NatsMessage.Builder builder = NatsMessage.builder();
        builder.data(SerDes.serialize(payload));
        builder.subject(subjectName);
        Message message = builder.build();

        if (returnType.equals(Void.class) || returnType.equals(void.class)) {  // doesn't expect a response
            connection.publish(message);
        } else { // wait for response
            CompletableFuture<Message> incoming = connection.request(message);
            Message response = incoming.get(getAnnotatedResponseTimeout(method).get(ChronoUnit.SECONDS), TimeUnit.SECONDS);
            if (response != null) {
                return SerDes.deserialize(response.getData(), returnType);
            }
        }
        return null;
    }

    private String getAnnotatedConnection(Method method) {
        String connectionName;

        // declaring class annotation
        RegisterNatsClient registerNatsClientAnnotation = method.getDeclaringClass().getAnnotation(RegisterNatsClient.class);
        connectionName = registerNatsClientAnnotation.connection();

        // method annotation variables
        Subject subjectAnnotation = method.getAnnotation(Subject.class);
        if (subjectAnnotation != null) {
            connectionName = subjectAnnotation.connection();
        }

        if (connectionName.isEmpty()) {
            connectionName = SingleConnectionConfig.DEFAULT_NAME;
        }

        return connectionName;
    }

    private String getAnnotatedSubject(Method method, Object[] args) {
        String subject = null;
        // method annotation
        Subject subjectAnnotation = method.getAnnotation(Subject.class);
        if (subjectAnnotation != null) {
            subject = subjectAnnotation.value();
        }
        // parameter annotation - overrides the method annotation value if both exists
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(Subject.class)) {
                return (String) args[i];
            }
        }
        return subject;
    }

    private Duration getAnnotatedResponseTimeout(Method method) {
        Duration responseTimeout = null;

        RegisterNatsClient registerNatsClientAnnotation = method.getDeclaringClass().getAnnotation(RegisterNatsClient.class);
        if (!registerNatsClientAnnotation.responseTimeout().isEmpty()) {
            responseTimeout = Duration.parse(registerNatsClientAnnotation.responseTimeout());
        }

        Subject subjectAnnotation = method.getAnnotation(Subject.class);
        if (subjectAnnotation != null && !subjectAnnotation.responseTimeout().isEmpty()) {
            responseTimeout = Duration.parse(subjectAnnotation.responseTimeout());
        }

        if (responseTimeout == null) {
            responseTimeout = ConfigLoader.getInstance().getGeneralConfig().getResponseTimeout();
        }
        return responseTimeout;
    }

    private Object getPayload(Method method, Object[] args) {
        int index = 0;
        for (int i = 0; i < method.getParameters().length; i++) {
            if (!method.getParameters()[i].isAnnotationPresent(Subject.class)) {
                index = i;
            }
        }
        return args[index];
    }
}
