package com.kumuluz.ee.nats.core.exception;

/**
 * @author Matej Bizjak
 */

/**
 * @author Matej Bizjak
 */

public class NatsListenerException extends RuntimeException {

    public NatsListenerException() {
    }

    public NatsListenerException(String message) {
        super(message);
    }

    public NatsListenerException(String message, Throwable cause) {
        super(message, cause);
    }

    public NatsListenerException(Throwable cause) {
        super(cause);
    }
}
