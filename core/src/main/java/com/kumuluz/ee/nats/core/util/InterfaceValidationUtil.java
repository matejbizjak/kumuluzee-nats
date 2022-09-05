package com.kumuluz.ee.nats.core.util;

import com.kumuluz.ee.nats.common.exception.DefinitionException;
import com.kumuluz.ee.nats.core.annotations.RegisterNatsClient;
import com.kumuluz.ee.nats.core.annotations.Subject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Helper for validating interfaces annotated with {@link RegisterNatsClient}.
 *
 * @author Matej Bizjak
 */

public class InterfaceValidationUtil {
    public static <T> void validateInterface(Class<T> aClass) {
        // method params
        for (Method method : aClass.getMethods()) {
            checkSubjectValue(method);
            checkMethodParameters(method);
        }
    }

    private static void checkSubjectValue(Method method) {
        boolean isAnnotated = false;
        // method annotation variables
        Subject subjectAnnotation = method.getAnnotation(Subject.class);
        if (subjectAnnotation != null && !subjectAnnotation.value().isEmpty()) {
            isAnnotated = true;
        }
        // method parameter variables
        for (Annotation[] annotations : method.getParameterAnnotations()) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(Subject.class)) {
                    isAnnotated = true;
                }
            }
        }

        if (!isAnnotated) {
            throw new DefinitionException(String
                    .format("NATS client's method %s in class %s is not annotated with @Subject or its value is null or empty."
                            , method.getName(), method.getDeclaringClass().getName()));
        }
    }

    private static void checkMethodParameters(Method method) {
        if (method.getParameterCount() < 1) {
            throw new DefinitionException(String.format("Not enough method parameters at method %s in class %s."
                    , method.getName(), method.getDeclaringClass().getName()));
        } else if (method.getParameterCount() == 1) {
            if (method.getParameters()[0].isAnnotationPresent(Subject.class)) {
                throw new DefinitionException(String.format("Not enough method parameters at method %s in class %s."
                        , method.getName(), method.getDeclaringClass().getName()));
            }
        } else if (method.getParameterCount() == 2) {
            int numberOfSubjectAnnotations = 0;
            for (Parameter parameter : method.getParameters()) {
                if (parameter.isAnnotationPresent(Subject.class)) {
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

}
