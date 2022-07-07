package com.kumuluz.ee.nats.common.connection.config;

/**
 * Connection configuration class that is used when 'servers' prefix is not used in the configuration.
 * This way you can specify only one server.
 *
 * @author Matej Bizjak
 */

public class SingleConnectionConfig extends ConnectionConfig {

    public static final String DEFAULT_NAME = "default";

    public SingleConnectionConfig() {
        super(DEFAULT_NAME);
    }
}
