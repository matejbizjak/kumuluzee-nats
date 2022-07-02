package com.kumuluz.ee.nats.common.exception;

/**
 * @author Matej Bizjak
 */

public class DefinitionException extends NatsException {

    public DefinitionException() {
    }

    public DefinitionException(String message) {
        super(message);
    }

    public DefinitionException(String message, Throwable cause) {
        super(message, cause);
    }

    public DefinitionException(Throwable cause) {
        super(cause);
    }
}
