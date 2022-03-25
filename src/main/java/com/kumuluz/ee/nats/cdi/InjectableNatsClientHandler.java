package com.kumuluz.ee.nats.cdi;

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
public class InjectableNatsClientHandler implements InvocationHandler {

    private Map<Class, Object> natsClientInvokerCache = new HashMap<>();

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object natsClientInvoker = natsClientInvokerCache.get(method.getDeclaringClass());

        if (natsClientInvoker == null) {
            // TODO
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
