package com.kumuluz.ee.nats.common.exception;

/**
 * NATS Exception about de/serialization problems.
 *
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
