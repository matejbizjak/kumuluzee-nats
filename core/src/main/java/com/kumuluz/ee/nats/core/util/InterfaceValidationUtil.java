package com.kumuluz.ee.nats.core.util;

import com.kumuluz.ee.nats.core.annotations.Subject;
import com.kumuluz.ee.nats.core.exception.NatsClientDefinitionException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
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
        if (subjectAnnotation != null) {
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
            throw new NatsClientDefinitionException(String.format("NATS client's method is not annotated with @Subject or its value is null! Cause: %s", method));
        }
    }

    private static void checkMethodParameters(Method method) {
        if (method.getParameterCount() < 1) {
            throw new NatsClientDefinitionException(String.format("Not enough method parameters! Cause: %s", method));
        } else if (method.getParameterCount() == 1) {
            if (method.getParameters()[0].isAnnotationPresent(Subject.class)) {
                throw new NatsClientDefinitionException(String.format("Not enough method parameters! Cause: %s", method));
            }
        } else if (method.getParameterCount() == 2) {
            int numberOfSubjectAnnotations = 0;
            for (Parameter parameter : method.getParameters()) {
                if (parameter.isAnnotationPresent(Subject.class)) {
                    numberOfSubjectAnnotations += 1;
                }
            }
            if (numberOfSubjectAnnotations != 1) {
                throw new NatsClientDefinitionException(String.format("Wrong method parameters! Cause: %s", method));
            }
        } else {
            throw new NatsClientDefinitionException(String.format("Wrong method parameters! Cause: %s", method));
        }
    }

}
