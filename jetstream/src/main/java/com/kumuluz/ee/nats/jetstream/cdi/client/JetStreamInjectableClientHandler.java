package com.kumuluz.ee.nats.jetstream.cdi.client;


import com.kumuluz.ee.nats.jetstream.proxy.JetStreamClientBuilder;

import javax.enterprise.context.Dependent;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matej Bizjak
 */

/**
 * Caches and invokes {@link com.kumuluz.ee.nats.jetstream.proxy.JetStreamClientBuilder}.
 *
 * @author Matej Bizjak
 */

@Dependent
public class JetStreamInjectableClientHandler implements InvocationHandler {

    private final Map<Class, Object> natsClientInvokerCache = new HashMap<>();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object natsClientInvoker = natsClientInvokerCache.get(method.getDeclaringClass());

        if (natsClientInvoker == null) {
            JetStreamClientBuilder clientBuilder = JetStreamClientBuilder.getInstance();

            natsClientInvoker = clientBuilder.build(method.getDeclaringClass());
            natsClientInvokerCache.put(method.getDeclaringClass(), natsClientInvoker);
        }

        try {
            return method.invoke(natsClientInvoker, args);
        } catch (InvocationTargetException e) {
            Throwable targetException = e.getTargetException();
            if (targetException != null) {
                throw targetException;
            }
            throw e;
        }
    }
}
