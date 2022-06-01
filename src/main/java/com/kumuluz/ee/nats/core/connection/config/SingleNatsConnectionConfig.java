package com.kumuluz.ee.nats.core.connection.config;

/**
 * @author Matej Bizjak
 */

/**
 * @author Matej Bizjak
 */

//@Named(SingleNatsConnectionConfig.DEFAULT_NAME)
public class SingleNatsConnectionConfig extends NatsConnectionConfig{

    public static final String DEFAULT_NAME = "default";

    public SingleNatsConnectionConfig() {
        super(DEFAULT_NAME);
    }
}
