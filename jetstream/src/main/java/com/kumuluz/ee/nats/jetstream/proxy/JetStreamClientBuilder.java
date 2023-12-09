package com.kumuluz.ee.nats.jetstream.proxy;

import com.kumuluz.ee.nats.jetstream.invoker.JetStreamClientInvoker;

import java.io.Closeable;
import java.lang.reflect.Proxy;

/**
 * Creates NATS Client by invoking {@link JetStreamClientInvoker}.
 *
 * @author Matej Bizjak
 */

public class JetStreamClientBuilder {
    private static final JetStreamClientBuilder INSTANCE = new JetStreamClientBuilder();

    public static JetStreamClientBuilder getInstance() {
        return INSTANCE;
    }

    public <T> T build(Class<T> aClass) {
        return this.create(aClass);
    }

    private <T> T create(Class<T> aClass) {
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader()
                , new Class[]{aClass, Closeable.class, AutoCloseable.class}, new JetStreamClientInvoker());
    }
}
