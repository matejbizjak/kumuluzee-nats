package com.kumuluz.ee.nats.connection.config;

/**
 * @author Matej Bizjak
 */

public class ClusterNatsConnectionConfig extends NatsConnectionConfig {

    public static final String PREFIX = "nats-core.servers";

    public ClusterNatsConnectionConfig(String name) {
        super(PREFIX + name);
    }
}
