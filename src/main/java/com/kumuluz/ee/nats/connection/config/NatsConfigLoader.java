package com.kumuluz.ee.nats.connection.config;

import com.kumuluz.ee.configuration.utils.ConfigurationUtil;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author Matej Bizjak
 */

public class NatsConfigLoader {

    private static NatsConfigLoader instance;
    private static final Set<NatsConnectionConfig> configs = new HashSet<>();

    public static NatsConfigLoader getInstance() {
        if (instance == null) {
            instance = new NatsConfigLoader();
        }
        return instance;
    }

    public Set<NatsConnectionConfig> getConfigs() {
        return configs;
    }

    public void readConfiguration() {
        ConfigurationUtil configurationUtil = ConfigurationUtil.getInstance();
        String clusterPrefix = "kumuluzee.nats-core.servers";
        Optional<Integer> size = configurationUtil.getListSize(clusterPrefix);
        if (size.isPresent()) {  // cluster configuration
            for (int i = 0; i < size.get(); i++) {
                String currentPrefix = clusterPrefix + "[" + i + "]";
                String name = configurationUtil.get(currentPrefix + ".name")
                        .orElseThrow(configNotFoundException(currentPrefix + ".name"));
                ClusterNatsConnectionConfig clusterConfig = new ClusterNatsConnectionConfig(name);
                readAndSetConfigClass(configurationUtil, clusterConfig, currentPrefix);
                configs.add(clusterConfig);
            }
        } else {
            String natsCorePrefix = "kumuluzee.nats-core";
            if (configurationUtil.get(natsCorePrefix).isPresent()) {  // single configuration
                SingleNatsConnectionConfig singleConfig = new SingleNatsConnectionConfig();
                readAndSetConfigClass(configurationUtil, singleConfig, natsCorePrefix);
            } else {
                throw new IllegalStateException("Configuration key '" + natsCorePrefix + "' required but not found.");
            }
        }
    }

    private Supplier<IllegalStateException> configNotFoundException(String configKey) {
        return () -> new IllegalStateException("Configuration key '" + configKey + "' required but not found.");
    }

    private void readAndSetConfigClass(ConfigurationUtil configurationUtil, NatsConnectionConfig natsConnectionConfig, String currentPrefix) {
        // addresses
        Optional<Integer> addressesSize = configurationUtil.getListSize(currentPrefix + ".addresses");
        List<String> addresses = new ArrayList<>();
        if (addressesSize.isPresent()) {
            for (int i = 0; i < addressesSize.get(); i++) {
                Optional<String> address = configurationUtil.get(currentPrefix + "[" + i + "]");
                address.ifPresent(addresses::add);
            }
        }
        if (!addresses.isEmpty()) {
            natsConnectionConfig.setAddresses(addresses);
        }
        // max-reconnect
        Optional<Integer> maxReconnect = configurationUtil.getInteger(currentPrefix + ".max-reconnect");
        maxReconnect.ifPresent(natsConnectionConfig::setMaxReconnect);
        // reconnect-wait
        Optional<Integer> reconnectWait = configurationUtil.getInteger(currentPrefix + ".reconnect-wait");
        reconnectWait.ifPresent(integer -> natsConnectionConfig.setReconnectWait(Duration.ofSeconds(integer)));
        // TODO
    }
}
