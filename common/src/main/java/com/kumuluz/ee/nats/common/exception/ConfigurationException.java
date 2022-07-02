package com.kumuluz.ee.nats.common.exception;

/**
 * @author Matej Bizjak
 */

public class ConfigurationException extends NatsException {

    public ConfigurationException() {
    }

    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }
}
