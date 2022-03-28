package com.kumuluz.ee.nats.cdi;

import com.kumuluz.ee.nats.annotations.NatsListener;
import com.kumuluz.ee.nats.annotations.Subject;

import javax.enterprise.inject.spi.Bean;
import java.lang.reflect.Method;

/**
 * A class representing annotated methods
 * @author Matej Bizjak
 */

public class ListenerMethod {
    private Bean<?> bean;
    private Method method;
    private Subject subjectAnnotation;
    private NatsListener natsListenerAnnotation;

    public ListenerMethod(Bean<?> bean, Method method, Subject subjectAnnotation, NatsListener natsListenerAnnotation) {
        this.bean = bean;
        this.method = method;
        this.subjectAnnotation = subjectAnnotation;
        this.natsListenerAnnotation = natsListenerAnnotation;
    }

    public Bean<?> getBean() {
        return bean;
    }

    public void setBean(Bean<?> bean) {
        this.bean = bean;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Subject getSubjectAnnotation() {
        return subjectAnnotation;
    }

    public void setSubjectAnnotation(Subject subjectAnnotation) {
        this.subjectAnnotation = subjectAnnotation;
    }

    public NatsListener getNatsListenerAnnotation() {
        return natsListenerAnnotation;
    }

    public void setNatsListenerAnnotation(NatsListener natsListenerAnnotation) {
        this.natsListenerAnnotation = natsListenerAnnotation;
    }
}
