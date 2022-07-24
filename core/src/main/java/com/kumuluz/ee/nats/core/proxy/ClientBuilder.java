package com.kumuluz.ee.nats.core.proxy;

import com.kumuluz.ee.nats.core.invoker.ClientInvoker;

import java.io.Closeable;
import java.lang.reflect.Proxy;

/**
 * Creates NATS Client by invoking {@link ClientInvoker}.
 *
 * @author Matej Bizjak
 */

public class ClientBuilder {
    private static final ClientBuilder INSTANCE = new ClientBuilder();

    public static ClientBuilder getInstance() {
        return INSTANCE;
    }

    public <T> T build(Class<T> aClass) {
        return this.create(aClass);
    }

    private <T> T create(Class<T> aClass) {
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader()
                , new Class[]{aClass, Closeable.class, AutoCloseable.class}, new ClientInvoker());
    }
}
