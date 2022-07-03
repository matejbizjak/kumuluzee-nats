package com.kumuluz.ee.nats.core.cdi;

import com.kumuluz.ee.nats.core.CoreExtension;
import com.kumuluz.ee.nats.core.annotations.RegisterNatsClient;

import javax.enterprise.context.*;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * CDI {@link Extension} that adds dynamically created beans from interfaces annotated with {@link RegisterNatsClient}.
 *
 * @author Matej Bizjak
 */

public class ClientInitializerExtension implements Extension {

    private final Set<AnnotatedType> classes;

    public ClientInitializerExtension() {
        this.classes = new HashSet<>();
    }

    public <T> void processAnnotatedType(@Observes @WithAnnotations(RegisterNatsClient.class) ProcessAnnotatedType<T> anType) {
        Class<T> javaClass = anType.getAnnotatedType().getJavaClass();

        if (!javaClass.isInterface()) {
            throw new IllegalArgumentException("Nats client need to be an interface: " + javaClass);
        }

        this.addAnnotatedType(anType.getAnnotatedType());
        anType.veto();
    }

    public <T> void after(@Observes AfterBeanDiscovery afterBeanDiscovery) {
        if (!CoreExtension.isExtensionEnabled()) {
            return;
        }

        for (AnnotatedType anType : this.classes) {
            Class<? extends Annotation> scopeClass = resolveScope(anType.getJavaClass());
            afterBeanDiscovery.addBean(new InvokerDelegateBean(anType.getJavaClass(), scopeClass));
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
