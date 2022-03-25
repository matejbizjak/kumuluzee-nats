package com.kumuluz.ee.nats.proxy;

import org.apache.deltaspike.proxy.api.DeltaSpikeProxyFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * @author Matej Bizjak
 */

public class NatsClientProxyFactory extends DeltaSpikeProxyFactory {
    private static final NatsClientProxyFactory INSTANCE = new NatsClientProxyFactory();

    public static NatsClientProxyFactory getInstance() {
        return INSTANCE;
    }

    @Override
    protected ArrayList<Method> getDelegateMethods(Class<?> targetClass, ArrayList<Method> allMethods) {
        ArrayList<Method> methods = new ArrayList<>();

        for (Method method : allMethods) {
            if (Modifier.isAbstract(method.getModifiers())) {
                methods.add(method);
            }
        }
        return methods;
    }

    @Override
    protected String getProxyClassSuffix() {
        return "$$NCProxyClient";
    }
}
