package com.kumuluz.ee.nats.common.connection.config;

import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import io.nats.client.api.DiscardPolicy;
import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author Matej Bizjak
 */

public class NatsConfigLoader {

    private static NatsConfigLoader instance;
    private static NatsGeneralConfig generalConfig;
    private static final HashMap<String, NatsConnectionConfig> connectionConfigs = new HashMap<>();

    public static NatsConfigLoader getInstance() {
        if (instance == null) {
            instance = new NatsConfigLoader();
        }
        return instance;
    }

    public NatsGeneralConfig getGeneralConfig() {
        return generalConfig;
    }

    public HashMap<String, NatsConnectionConfig> getConnectionConfigs() {
        return connectionConfigs;
    }

    public NatsConnectionConfig getConfigForConnection(String connectionName) {
        return connectionConfigs.get(connectionName);
    }

    public void readConfiguration() {
        ConfigurationUtil configurationUtil = ConfigurationUtil.getInstance();
        // general settings
        generalConfig = new NatsGeneralConfig();
        readAndSetGeneralConfigClass(configurationUtil);
        // connection settings
        String clusterPrefix = "kumuluzee.nats.servers";
        Optional<Integer> size = configurationUtil.getListSize(clusterPrefix);
        if (size.isPresent()) {  // cluster configuration
            for (int i = 0; i < size.get(); i++) {
                String currentPrefix = clusterPrefix + "[" + i + "]";
                String name = configurationUtil.get(currentPrefix + ".name")
                        .orElseThrow(configNotFoundException(currentPrefix + ".name"));
                ClusterNatsConnectionConfig clusterConfig = new ClusterNatsConnectionConfig(name);
                readAndSetConnectionConfigClass(configurationUtil, clusterConfig, currentPrefix);
                connectionConfigs.put(name, clusterConfig);
            }
        } else {
            String natsCorePrefix = "kumuluzee.nats";
            if (configurationUtil.get(natsCorePrefix).isPresent()) {  // single configuration
                SingleNatsConnectionConfig singleConfig = new SingleNatsConnectionConfig();
                readAndSetConnectionConfigClass(configurationUtil, singleConfig, natsCorePrefix);
                connectionConfigs.put(singleConfig.getName(), singleConfig);
            } else {
                throw configNotFoundException(natsCorePrefix).get();
            }
        }
    }

    private Supplier<IllegalStateException> configNotFoundException(String configKey) {
        return () -> new IllegalStateException("Configuration key '" + configKey + "' required but not found.");
    }

    private void readAndSetGeneralConfigClass(ConfigurationUtil configurationUtil) {
        String prefix = "kumuluzee.nats";
        // response-timeout
        Optional<Integer> responseTimeout = configurationUtil.getInteger(prefix + ".response-timeout");
        responseTimeout.ifPresent(generalConfig::setResponseTimeout);
    }

    private void readAndSetConnectionConfigClass(ConfigurationUtil configurationUtil, NatsConnectionConfig natsConnectionConfig, String currentPrefix) {
        // addresses
        Optional<Integer> addressesSize = configurationUtil.getListSize(currentPrefix + ".addresses");
        List<String> addresses = new ArrayList<>();
        if (addressesSize.isPresent()) {
            for (int i = 0; i < addressesSize.get(); i++) {
                Optional<String> address = configurationUtil.get(currentPrefix + ".addresses" + "[" + i + "]");
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
        // connection-timeout
        Optional<Integer> connectionTimeout = configurationUtil.getInteger(currentPrefix + ".connection-timeout");
        connectionTimeout.ifPresent(integer -> natsConnectionConfig.setConnectionTimeout(Duration.ofSeconds(integer)));
        // ping-interval
        Optional<Integer> pingInterval = configurationUtil.getInteger(currentPrefix + ".ping-interval");
        pingInterval.ifPresent(integer -> natsConnectionConfig.setPingInterval(Duration.ofSeconds(integer)));
        // reconnect-buffer-size
        Optional<Long> reconnectBufferSize = configurationUtil.getLong(currentPrefix + ".reconnect-buffer-size");
        reconnectBufferSize.ifPresent(natsConnectionConfig::setReconnectBufferSize);
        // inbox-prefix
        Optional<String> inboxPrefix = configurationUtil.get(currentPrefix + ".inbox-prefix");
        inboxPrefix.ifPresent(natsConnectionConfig::setInboxPrefix);
        // no-echo
        Optional<Boolean> noEcho = configurationUtil.getBoolean(currentPrefix + ".no-echo");
        noEcho.ifPresent(natsConnectionConfig::setNoEcho);
        // username
        Optional<String> username = configurationUtil.get(currentPrefix + ".username");
        username.ifPresent(natsConnectionConfig::setUsername);
        // password
        Optional<String> password = configurationUtil.get(currentPrefix + ".password");
        password.ifPresent(natsConnectionConfig::setPassword);
        // credentials
        Optional<String> credentials = configurationUtil.get(currentPrefix + ".credentials");
        credentials.ifPresent(natsConnectionConfig::setCredentials);

        // (jet)streams
        Optional<Integer> streamsSize = configurationUtil.getListSize(currentPrefix + ".streams");
        List<StreamConfiguration> streams = new ArrayList<>();
        if (streamsSize.isPresent()) {
            for (int i = 0; i < streamsSize.get(); i++) {
                streams.add(readStreamsConfiguration(configurationUtil, currentPrefix + ".streams" + "[" + i + "]"));
            }
        }
        if (!streams.isEmpty()) {
            natsConnectionConfig.setStreamConfigurations(streams);
        }

        // TLS
        Optional<String> tlsConf = configurationUtil.get(currentPrefix + ".tls");
        if (!tlsConf.isPresent()) {
            return;
        }
        NatsConnectionConfig.TLS tls = new NatsConnectionConfig.TLS();
        // trust-store-type
        Optional<String> trustStoreType = configurationUtil.get(currentPrefix + ".tls" + ".trust-store-type");
        trustStoreType.ifPresent(tls::setTrustStoreType);
        // trust-store-path
        Optional<String> trustStorePath = configurationUtil.get(currentPrefix + ".tls" + ".trust-store-path");
        trustStorePath.ifPresent(tls::setTrustStorePath);
        // trust-store-password
        Optional<String> trustStorePassword = configurationUtil.get(currentPrefix + ".tls" + ".trust-store-password");
        trustStorePassword.ifPresent(tls::setTrustStorePassword);
        // certificate-path
        Optional<String> certificatePath = configurationUtil.get(currentPrefix + ".tls" + ".certificate-path");
        certificatePath.ifPresent(tls::setCertificatePath);
        // key-store-path
        Optional<String> keyStorePath = configurationUtil.get(currentPrefix + ".tls" + ".key-store-path");
        keyStorePath.ifPresent(tls::setKeyStorePath);
        // key-store-password
        Optional<String> keyStorePassword = configurationUtil.get(currentPrefix + ".tls" + ".key-store-password");
        keyStorePassword.ifPresent(tls::setKeyStorePassword);
        // key-store-type
        Optional<String> keyStoreType = configurationUtil.get(currentPrefix + ".tls" + ".key-store-type");
        keyStoreType.ifPresent(tls::setKeyStoreType);
        natsConnectionConfig.setTls(tls);
    }

    private StreamConfiguration readStreamsConfiguration(ConfigurationUtil configurationUtil, String currentPrefix) {
        StreamConfiguration.Builder builder = StreamConfiguration.builder();
        // name
        Optional<String> name = configurationUtil.get(currentPrefix + ".name");
        name.ifPresent(builder::name);
        // subjects
        Optional<Integer> subjectsSize = configurationUtil.getListSize(currentPrefix + ".subjects");
        List<String> subjects = new ArrayList<>();
        if (subjectsSize.isPresent()) {
            for (int i = 0; i < subjectsSize.get(); i++) {
                Optional<String> subject = configurationUtil.get(currentPrefix + ".subjects" + "[" + i + "]");
                subject.ifPresent(subjects::add);
            }
        }
        if (!subjects.isEmpty()) {
            builder.subjects(subjects);
        }
        // retention policy
        Optional<String> retentionPolicy = configurationUtil.get(currentPrefix + ".retentionPolicy");
        retentionPolicy.ifPresent(x -> builder.retentionPolicy(RetentionPolicy.get(retentionPolicy.get())));
        // max consumers
        Optional<Long> maxConsumers = configurationUtil.getLong(currentPrefix + ".maxConsumers");
        maxConsumers.ifPresent(builder::maxConsumers);
        // max bytes
        Optional<Long> maxBytes = configurationUtil.getLong(currentPrefix + ".maxBytes");
        maxBytes.ifPresent(builder::maxBytes);
        // max age
        Optional<Long> maxAge = configurationUtil.getLong(currentPrefix + ".maxAge");
        maxAge.ifPresent(builder::maxAge);
        // max message size
        Optional<Long> maxMsgSize = configurationUtil.getLong(currentPrefix + ".maxMsgSize");
        maxMsgSize.ifPresent(builder::maxMsgSize);
        // storage type
        Optional<String> storageType = configurationUtil.get(currentPrefix + ".storageType");
        storageType.ifPresent(x -> builder.storageType(StorageType.get(storageType.get())));
        // replicas
        Optional<Integer> replicas = configurationUtil.getInteger(currentPrefix + ".replicas");
        replicas.ifPresent(builder::replicas);
        // no ack
        Optional<Boolean> noAck = configurationUtil.getBoolean(currentPrefix + ".noAck");
        noAck.ifPresent(builder::noAck);
        // template owner
        Optional<String> templateOwner = configurationUtil.get(currentPrefix + ".templateOwner");
        templateOwner.ifPresent(builder::templateOwner);
        // discard policy
        Optional<String> discardPolicy = configurationUtil.get(currentPrefix + ".discardPolicy");
        discardPolicy.ifPresent(x -> builder.discardPolicy(DiscardPolicy.get(discardPolicy.get())));

        return builder.build();
    }
}
