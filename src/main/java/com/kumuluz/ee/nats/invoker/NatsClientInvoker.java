package com.kumuluz.ee.nats.invoker;

import com.kumuluz.ee.nats.annotations.RegisterNatsClient;
import com.kumuluz.ee.nats.annotations.Subject;
import com.kumuluz.ee.nats.connection.NatsConnection;
import com.kumuluz.ee.nats.connection.config.SingleNatsConnectionConfig;
import com.kumuluz.ee.nats.util.SerDes;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.impl.NatsMessage;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * @author Matej Bizjak
 */

public class NatsClientInvoker implements InvocationHandler {

    private static final Logger LOG = Logger.getLogger(NatsClientInvoker.class.getName());

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String connectionName = getAnnotatedConnection(method);
        String subjectName = getAnnotatedSubject(method);
        Object payload = getPayload(method, args);
        Class<?> returnType = method.getReturnType();

        Connection connection = NatsConnection.getConnection(connectionName);

        NatsMessage.Builder builder = NatsMessage.builder();
        builder.data(SerDes.serialize(payload));
        builder.subject(subjectName);
        Message message = builder.build();

        if (method.getReturnType().equals(Void.class) || method.getReturnType().equals(void.class)) {  // doesn't expect a response
            connection.publish(message);
        } else { // wait for response
            CompletableFuture<Message> incoming = connection.request(message);
            Message response = null;
//            try {
            response = incoming.get(10, TimeUnit.SECONDS);
//            } catch (InterruptedException | ExecutionException | TimeoutException e) {
//                LOG.info("Couldn't get a response: " + e.getLocalizedMessage());
//            }
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
            connectionName = SingleNatsConnectionConfig.DEFAULT_NAME;
        }

        return connectionName;
    }

    private String getAnnotatedSubject(Method method) {
        String subjectName = null;

        // method annotation variables
        Subject subjectAnnotation = method.getAnnotation(Subject.class);
        if (subjectAnnotation != null) {
            subjectName = subjectAnnotation.value();
        }
        // method parameter variables
        for (Annotation[] annotations : method.getParameterAnnotations()) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(Subject.class)) {
                    subjectAnnotation = (Subject) annotation;
                    subjectName = subjectAnnotation.value();
                }
            }
        }
        return subjectName;
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
