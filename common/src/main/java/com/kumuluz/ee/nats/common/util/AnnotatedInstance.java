package com.kumuluz.ee.nats.common.util;

import javax.enterprise.inject.spi.Bean;
import java.lang.reflect.Method;

/**
 * Represents an annotated method in a bean.
 *
 * @author Matej Bizjak
 */

public class AnnotatedInstance<T, S> {

    private Bean bean;
    private Method method;
    private T annotation1;
    private S annotation2;

    public AnnotatedInstance(Bean bean, Method method, T annotation1, S annotation2) {
        this.bean = bean;
        this.method = method;
        this.annotation1 = annotation1;
        this.annotation2 = annotation2;
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

    public T getAnnotation1() {
        return annotation1;
    }

    public void setAnnotation1(T annotation1) {
        this.annotation1 = annotation1;
    }

    public S getAnnotation2() {
        return annotation2;
    }

    public void setAnnotation2(S annotation2) {
        this.annotation2 = annotation2;
    }
}
