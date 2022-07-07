package com.kumuluz.ee.nats.common.exception;

/**
 * Parent NATS Exception.
 *
 * @author Matej Bizjak
 */

public class NatsException extends RuntimeException {

    public NatsException() {
    }

    public NatsException(String message) {
        super(message);
    }

    public NatsException(String message, Throwable cause) {
        super(message, cause);
    }

    public NatsException(Throwable cause) {
        super(cause);
    }
}
