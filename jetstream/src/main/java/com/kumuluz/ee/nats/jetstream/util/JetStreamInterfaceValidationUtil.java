package com.kumuluz.ee.nats.jetstream.util;

import com.kumuluz.ee.nats.common.exception.DefinitionException;
import com.kumuluz.ee.nats.jetstream.annotations.JetStreamSubject;
import com.kumuluz.ee.nats.jetstream.annotations.RegisterJetStreamClient;
import io.nats.client.api.PublishAck;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;

/**
 * Helper for validating interfaces annotated with {@link RegisterJetStreamClient}.
 *
 * @author Matej Bizjak
 */

public class JetStreamInterfaceValidationUtil {
    public static <T> void validateInterface(Class<T> aClass) {
        // method params
        for (Method method : aClass.getMethods()) {
            checkSubjectValue(method);
            checkMethodParameters(method);
            checkMethodReturnType(method);
        }
    }

    private static void checkSubjectValue(Method method) {
        boolean isAnnotated = false;
        // method annotation variables
        JetStreamSubject jetStreamSubjectAnnotation = method.getAnnotation(JetStreamSubject.class);
        if (jetStreamSubjectAnnotation != null && !jetStreamSubjectAnnotation.value().isEmpty()) {
            isAnnotated = true;
        }
        // method parameter variables
        for (Annotation[] annotations : method.getParameterAnnotations()) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(JetStreamSubject.class)) {
                    isAnnotated = true;
                }
            }
        }

        if (!isAnnotated) {
            throw new DefinitionException(String
                    .format("JetStream client's method %s in class %s is not annotated with @JetStreamSubject or its value is null or empty."
                            , method.getName(), method.getDeclaringClass().getName()));
        }
    }

    private static void checkMethodParameters(Method method) {
        if (method.getParameterCount() < 1) {
            throw new DefinitionException(String.format("Not enough method parameters at method %s in class %s."
                    , method.getName(), method.getDeclaringClass().getName()));
        } else if (method.getParameterCount() == 1) {
            if (method.getParameters()[0].isAnnotationPresent(JetStreamSubject.class)) {
                throw new DefinitionException(String.format("Not enough method parameters at method %s in class %s."
                        , method.getName(), method.getDeclaringClass().getName()));
            }
        } else if (method.getParameterCount() == 2) {
            int numberOfSubjectAnnotations = 0;
            for (Parameter parameter : method.getParameters()) {
                if (parameter.isAnnotationPresent(JetStreamSubject.class)) {
                    numberOfSubjectAnnotations += 1;
                }
            }
            if (numberOfSubjectAnnotations != 1) {
                throw new DefinitionException(String.format("Wrong method parameters at method %s in class %s."
                        , method.getName(), method.getDeclaringClass().getName()));
            }
        } else {
            throw new DefinitionException(String.format("Wrong number of method parameters at method %s in class %s."
                    , method.getName(), method.getDeclaringClass().getName()));
        }
    }

    private static void checkMethodReturnType(Method method) {
        Type returnType = method.getGenericReturnType();

        if (returnType.equals(PublishAck.class)) {
            return;
        }

        // check whether the return type is CompletableFuture<PublishAck>
        if (returnType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) returnType;
            Type rawType = type.getRawType();

            if (rawType instanceof Class<?> && CompletableFuture.class.isAssignableFrom((Class<?>) rawType)) {
                Type[] typeArguments = type.getActualTypeArguments();
                if (typeArguments.length == 1 && typeArguments[0].equals(PublishAck.class)) {
                    return;
                }
            }
        }

        throw new DefinitionException(String.format("Wrong return type for JetStream publisher at method %s in class %s."
                , method.getName(), method.getDeclaringClass().getName()));
    }

}
