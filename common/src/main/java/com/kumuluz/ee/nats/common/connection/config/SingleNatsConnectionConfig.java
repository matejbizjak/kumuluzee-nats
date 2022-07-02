package com.kumuluz.ee.nats.common.connection.config;

/**
 * @author Matej Bizjak
 */

public class SingleNatsConnectionConfig extends NatsConnectionConfig{

    public static final String DEFAULT_NAME = "default";

    public SingleNatsConnectionConfig() {
        super(DEFAULT_NAME);
    }
}
