package com.kumuluz.ee.nats.jetstream.cdi.client;

import com.kumuluz.ee.nats.jetstream.JetStreamExtension;
import com.kumuluz.ee.nats.jetstream.annotations.RegisterJetStreamClient;
import com.kumuluz.ee.nats.jetstream.util.JetStreamInterfaceValidationUtil;

import javax.enterprise.context.*;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Matej Bizjak
 */

/**
 * CDI {@link Extension} that adds dynamically created beans from interfaces annotated with {@link RegisterJetStreamClient}.
 *
 * @author Matej Bizjak
 */

public class JetStreamClientInitializerExtension implements Extension {

    private final Set<AnnotatedType> classes;

    public JetStreamClientInitializerExtension() {
        this.classes = new HashSet<>();
    }

    public <T> void processAnnotatedType(@Observes @WithAnnotations(RegisterJetStreamClient.class) ProcessAnnotatedType<T> anType) {
        Class<T> javaClass = anType.getAnnotatedType().getJavaClass();

        if (!javaClass.isInterface()) {
            throw new IllegalArgumentException(String.format("JetStream client %s need to be an interface.", javaClass));
        }
        JetStreamInterfaceValidationUtil.validateInterface(javaClass);

        this.addAnnotatedType(anType.getAnnotatedType());
        anType.veto();
    }

    public <T> void after(@Observes AfterBeanDiscovery afterBeanDiscovery) {
        if (!JetStreamExtension.isExtensionEnabled()) {
            return;
        }

        for (AnnotatedType anType : this.classes) {
            Class<? extends Annotation> scopeClass = resolveScope(anType.getJavaClass());
            afterBeanDiscovery.addBean(new JetStreamClientInvokerDelegateBean(anType.getJavaClass(), scopeClass));
        }
    }

    private void addAnnotatedType(AnnotatedType annotatedType) {
        if (this.classes.stream().noneMatch(anType -> anType.getJavaClass().equals(annotatedType.getJavaClass()))) {
            this.classes.add(annotatedType);
        }
    }

    private Class<? extends Annotation> resolveScope(Class interfaceClass) {
        if (interfaceClass.isAnnotationPresent(RequestScoped.class)) {
            return RequestScoped.class;
        } else if (interfaceClass.isAnnotationPresent(ApplicationScoped.class)) {
            return ApplicationScoped.class;
        } else if (interfaceClass.isAnnotationPresent(SessionScoped.class)) {
            return SessionScoped.class;
        } else if (interfaceClass.isAnnotationPresent(ConversationScoped.class)) {
            return ConversationScoped.class;
        } else if (interfaceClass.isAnnotationPresent(Singleton.class)) {
            return Singleton.class;
        } else {
            return Dependent.class;
        }
    }
}
