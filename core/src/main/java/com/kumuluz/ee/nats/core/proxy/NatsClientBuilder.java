package com.kumuluz.ee.nats.core.proxy;

import com.kumuluz.ee.nats.core.invoker.NatsClientInvoker;
import com.kumuluz.ee.nats.core.util.InterfaceValidationUtil;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.proxy.spi.DeltaSpikeProxy;
import org.apache.deltaspike.proxy.spi.invocation.DeltaSpikeProxyInvocationHandler;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Matej Bizjak
 */

public class NatsClientBuilder {
    private static NatsClientBuilder instance = new NatsClientBuilder();

    private DeltaSpikeProxyInvocationHandler deltaSpikeProxyInvocationHandler;
    private BeanManager beanManager;

    public static NatsClientBuilder getInstance() {
        return instance;
    }

    public <T> T build(Class<T> aClass) {
        InterfaceValidationUtil.validateInterface(aClass);

        NatsClientProxyFactory proxyFactory = NatsClientProxyFactory.getInstance();
        beanManager = CDI.current().getBeanManager();

        Class<T> proxyClass = proxyFactory.getProxyClass(beanManager, aClass);
        Method[] delegateMethods = proxyFactory.getDelegateMethods(aClass);

        return this.create(aClass, proxyClass, delegateMethods);
    }

    private <T> T create(Class<T> aClass, Class<T> proxyClass, Method[] delegateMethods) {
        try {
            lazyinit();

            T instance = proxyClass.getDeclaredConstructor().newInstance();

            DeltaSpikeProxy deltaSpikeProxy = (DeltaSpikeProxy) instance;
            deltaSpikeProxy.setInvocationHandler(deltaSpikeProxyInvocationHandler);

            deltaSpikeProxy.setDelegateMethods(delegateMethods);

            NatsClientInvoker ncInvoker = new NatsClientInvoker();
            deltaSpikeProxy.setDelegateInvocationHandler(ncInvoker);

            return instance;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void lazyinit() {
        if (deltaSpikeProxyInvocationHandler == null) {
            init();
        }
    }

    private synchronized void init() {
        if (deltaSpikeProxyInvocationHandler == null) {
            deltaSpikeProxyInvocationHandler = BeanProvider.getContextualReference(beanManager, DeltaSpikeProxyInvocationHandler.class, false);
        }
    }
}
