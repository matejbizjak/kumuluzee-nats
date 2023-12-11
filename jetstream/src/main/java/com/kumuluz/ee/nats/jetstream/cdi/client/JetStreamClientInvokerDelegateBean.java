package com.kumuluz.ee.nats.jetstream.cdi.client;

import com.kumuluz.ee.nats.jetstream.annotations.JetStreamClient;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.util.AnnotationLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Matej Bizjak
 */

/**
 * Bean that creates a JetStream Client using a {@link JetStreamInjectableClientHandler}.
 *
 * @author Matej Bizjak
 */

public class JetStreamClientInvokerDelegateBean implements Bean<Object>, PassivationCapable {

    private final Class<?> restClientType;
    private final Class<? extends Annotation> scope;

    public JetStreamClientInvokerDelegateBean(Class<?> restClientType, Class<? extends Annotation> scope) {
        this.restClientType = restClientType;
        this.scope = scope;
    }

    @Override
    public Class<?> getBeanClass() {
        return restClientType;
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public Object create(CreationalContext<Object> creationalContext) {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{restClientType},
                new JetStreamInjectableClientHandler());
    }

    @Override
    public void destroy(Object o, CreationalContext<Object> creationalContext) {

    }

    @Override
    public Set<Type> getTypes() {
        return Collections.singleton(restClientType);
    }

    @Override
    public Set<Annotation> getQualifiers() {
        Set<Annotation> qualifiers = new HashSet<>();
        qualifiers.add(new AnnotationLiteral<Default>() {
        });
        qualifiers.add(new AnnotationLiteral<Any>() {
        });
        qualifiers.add(JetStreamClient.LITERAL);

        return qualifiers;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return scope;
    }

    @Override
    public String getName() {
        return restClientType.getName();
    }

    @Override
    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isAlternative() {
        return false;
    }

    @Override
    public String getId() {
        return getName();
    }
}
