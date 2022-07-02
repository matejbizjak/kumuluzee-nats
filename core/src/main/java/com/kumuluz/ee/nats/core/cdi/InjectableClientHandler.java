package com.kumuluz.ee.nats.core.cdi;

import com.kumuluz.ee.nats.core.proxy.ClientBuilder;

import javax.enterprise.context.Dependent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matej Bizjak
 */

@Dependent
public class InjectableClientHandler implements InvocationHandler {

    private final Map<Class, Object> natsClientInvokerCache = new HashMap<>();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object natsClientInvoker = natsClientInvokerCache.get(method.getDeclaringClass());

        if (natsClientInvoker == null) {
            ClientBuilder clientBuilder = ClientBuilder.getInstance();

            natsClientInvoker = clientBuilder.build(method.getDeclaringClass());
            natsClientInvokerCache.put(method.getDeclaringClass(), natsClientInvoker);
        }

        try {
            return method.invoke(natsClientInvoker, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw cause;
        }
    }
}
