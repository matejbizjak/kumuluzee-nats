package com.kumuluz.ee.nats.core.connection.config;

/**
 * @author Matej Bizjak
 */

/**
 * @author Matej Bizjak
 */



public class NatsGeneralConfig {

    private int responseTimeout = 10;

    public NatsGeneralConfig() {
    }

    public int getResponseTimeout() {
        return responseTimeout;
    }

    public void setResponseTimeout(int responseTimeout) {
        this.responseTimeout = responseTimeout;
    }
}
