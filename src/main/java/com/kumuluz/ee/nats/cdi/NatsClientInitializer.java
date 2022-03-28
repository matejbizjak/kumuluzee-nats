package com.kumuluz.ee.nats.cdi;

import com.kumuluz.ee.nats.annotations.NatsClient;
import com.kumuluz.ee.nats.annotations.RegisterNatsClient;
import com.kumuluz.ee.nats.proxy.NatsClientProxyFactory;
import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.core.api.literal.DefaultLiteral;
import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.proxy.api.DeltaSpikeProxyContextualLifecycle;

import javax.enterprise.context.*;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;
import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Matej Bizjak
 */

public class NatsClientInitializer implements Extension {

    private final Set<AnnotatedType> annotatedTypes;

    public NatsClientInitializer() {
        this.annotatedTypes = new HashSet<>();
    }

    public <T> void processAnnotatedType(@Observes @WithAnnotations(RegisterNatsClient.class) ProcessAnnotatedType<T> pat) {
        Class<T> javaClass = pat.getAnnotatedType().getJavaClass();

        if (!javaClass.isInterface()) {
            throw new IllegalArgumentException("Nats client need to be an interface: " + javaClass);
        }

        this.addAnnotatedType(pat.getAnnotatedType());
        pat.veto();
    }

    public <T> void afterBean(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
        for (AnnotatedType annotatedType : this.annotatedTypes) {
            DeltaSpikeProxyContextualLifecycle lifecycle = new DeltaSpikeProxyContextualLifecycle(
                    annotatedType.getJavaClass()
                    , InjectableNatsClientHandler.class
                    , NatsClientProxyFactory.getInstance()
                    , beanManager
            );

            BeanBuilder<T> beanBuilder = new BeanBuilder<>(beanManager)
                    .readFromType(annotatedType)
                    .qualifiers(new NatsClient.NatsClientLiteral(), new DefaultLiteral(), new AnyLiteral())
                    .passivationCapable(true)
                    .scope(resolveScope(annotatedType.getJavaClass()))
                    .beanLifecycle(lifecycle);

            afterBeanDiscovery.addBean(beanBuilder.create());
        }
    }

    private void addAnnotatedType(AnnotatedType annotatedType) {
        if (this.annotatedTypes.stream().noneMatch(anType -> anType.getJavaClass().equals(annotatedType.getJavaClass()))) {
            this.annotatedTypes.add(annotatedType);
        }
    }

    private Class resolveScope(Class interfaceClass) {
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
