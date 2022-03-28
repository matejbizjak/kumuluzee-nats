package com.kumuluz.ee.nats.exception;

/**
 * @author Matej Bizjak
 */

public class NatsClientDefinitionException extends RuntimeException {

    public NatsClientDefinitionException() {

    }

    public NatsClientDefinitionException(String message) {
        super(message);
    }

    public NatsClientDefinitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NatsClientDefinitionException(Throwable cause) {
        super(cause);
    }
}
