package com.kumuluz.ee.nats.common.connection.config;

/**
 * Connection configuration class that is used when 'servers' prefix is used in the configuration.
 * This way you can specify more than one server.
 *
 * @author Matej Bizjak
 */

public class ClusterConnectionConfig extends ConnectionConfig {

    public ClusterConnectionConfig(String name) {
        super(name);
    }
}
