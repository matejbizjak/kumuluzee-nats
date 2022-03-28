package com.kumuluz.ee.nats.cdi;

import com.kumuluz.ee.nats.annotations.NatsListener;
import com.kumuluz.ee.nats.annotations.Subject;
import com.kumuluz.ee.nats.exception.NatsListenerException;

import javax.annotation.Priority;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Finds methods which are annotated with a Subject annotations and their class is annotated with NatsListener annotation
 *
 * @author Matej Bizjak
 */

public class NatsListenerInitializer implements Extension {

    private static final Logger LOG = Logger.getLogger(NatsListenerInitializer.class.getName());
    List<ListenerMethod> listenerMethods = new ArrayList<>();

    public <T> void processStreamListeners(@Observes ProcessBean<T> processBean) {
        Class<?> aClass = processBean.getBean().getBeanClass();
        if (aClass.isAnnotationPresent(NatsListener.class)) {
            NatsListener natsListenerAnnotation = aClass.getAnnotation(NatsListener.class);
            for (Method method : aClass.getMethods()) {
                Subject subjectAnnotation = null;
                if (method.isAnnotationPresent(Subject.class)) {  // method annotation
                    subjectAnnotation = method.getAnnotation(Subject.class);
                } else {  // method parameter annotation (if both method annotation and method parameter annotation are present, the latter will replace a method annotation)
                    for (Annotation[] parameterAnnotation : method.getParameterAnnotations()) {
                        for (Annotation annotation : parameterAnnotation) {
                            if (annotation.annotationType().equals(Subject.class)) {
                                subjectAnnotation = (Subject) annotation;
                            }
                        }
                    }
                }
                if (subjectAnnotation != null) {
                    listenerMethods.add(new ListenerMethod(processBean.getBean(), method, subjectAnnotation, natsListenerAnnotation));
                }
            }
        }
    }

    public void after(@Observes @Priority(2600) AfterDeploymentValidation adv, BeanManager beanManager) {
        for (ListenerMethod inst : listenerMethods) {
            LOG.info("Found method " + inst.getMethod().getName() + " in class " +
                    inst.getMethod().getDeclaringClass());
        }

        for (ListenerMethod inst : listenerMethods) {
            Method method = inst.getMethod();
            Subject subjectAnnotation = inst.getSubjectAnnotation();
            NatsListener natsListenerAnnotation = inst.getNatsListenerAnnotation();

            String subject = subjectAnnotation.value();
            if (subject.isEmpty()) {
                throw new NatsListenerException("The subject is not valid.");
            }
            String connection = subjectAnnotation.connection();
            if (connection.isEmpty()) {
                connection = natsListenerAnnotation.connection();
                if (connection.isEmpty()) {
                    connection = "default";
                }
            }
            String queue = subjectAnnotation.queue();
            if (queue.isEmpty()) {
                queue = natsListenerAnnotation.queue();
            }

            // TODO glej NatsConsumerAdvice
        }
    }
}
