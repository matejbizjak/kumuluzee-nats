package com.kumuluz.ee.nats.common.exception;

/**
 * @author Matej Bizjak
 */

public class InvocationException extends NatsException {

    public InvocationException() {
    }

    public InvocationException(String message) {
        super(message);
    }

    public InvocationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvocationException(Throwable cause) {
        super(cause);
    }
}
