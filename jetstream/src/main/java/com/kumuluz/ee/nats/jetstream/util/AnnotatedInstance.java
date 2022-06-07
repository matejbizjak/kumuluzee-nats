package com.kumuluz.ee.nats.jetstream.util;

import javax.enterprise.inject.spi.Bean;
import java.lang.reflect.Method;

/**
 * @author Matej Bizjak
 */

public class AnnotatedInstance<T> {

    private Bean bean;

    private Method method;

    private T annotation;

    public AnnotatedInstance(Bean bean, Method method, T annotation) {
        this.bean = bean;
        this.method = method;
        this.annotation = annotation;
    }

    public Bean getBean() {
        return bean;
    }

    public void setBean(Bean bean) {
        this.bean = bean;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public T getAnnotation() {
        return annotation;
    }

    public void setAnnotation(T annotation) {
        this.annotation = annotation;
    }
}
