package com.kumuluz.ee.nats.common.connection.config;

/**
 * @author Matej Bizjak
 */

public class SingleConnectionConfig extends ConnectionConfig {

    public static final String DEFAULT_NAME = "default";

    public SingleConnectionConfig() {
        super(DEFAULT_NAME);
    }
}
