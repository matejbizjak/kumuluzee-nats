package com.kumuluz.ee.nats.common.exception;

/**
 * @author Matej Bizjak
 */

public class SerializationException extends NatsException {

    public SerializationException() {
    }

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializationException(Throwable cause) {
        super(cause);
    }
}
