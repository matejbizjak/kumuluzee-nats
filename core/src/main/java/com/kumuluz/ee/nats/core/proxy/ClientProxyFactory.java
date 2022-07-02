package com.kumuluz.ee.nats.core.proxy;

import org.apache.deltaspike.proxy.api.DeltaSpikeProxyFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * @author Matej Bizjak
 */

public class ClientProxyFactory extends DeltaSpikeProxyFactory {

    private static final ClientProxyFactory INSTANCE = new ClientProxyFactory();

    public static ClientProxyFactory getInstance() {
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
