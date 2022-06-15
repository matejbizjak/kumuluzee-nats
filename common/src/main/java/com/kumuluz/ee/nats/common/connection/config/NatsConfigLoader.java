package com.kumuluz.ee.nats.common.connection.config;

import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import io.nats.client.JetStreamOptions;
import io.nats.client.api.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author Matej Bizjak
 */

public class NatsConfigLoader {

    private static NatsConfigLoader instance;
    private static NatsGeneralConfig generalConfig;
    private static final HashMap<String, NatsConnectionConfig> connectionConfigs = new HashMap<>();
    private final ConfigurationUtil configurationUtil = ConfigurationUtil.getInstance();

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
        // general settings
        generalConfig = new NatsGeneralConfig();
        readAndSetGeneralConfigClass();
        // connection settings
        String clusterPrefix = "kumuluzee.nats.servers";
        Optional<Integer> size = configurationUtil.getListSize(clusterPrefix);
        if (size.isPresent()) {  // cluster configuration
            for (int i = 0; i < size.get(); i++) {
                String currentPrefix = clusterPrefix + "[" + i + "]";
                String name = configurationUtil.get(currentPrefix + ".name")
                        .orElseThrow(configNotFoundException(currentPrefix + ".name"));
                ClusterNatsConnectionConfig clusterConfig = new ClusterNatsConnectionConfig(name);
                readAndSetConnectionConfigClass(clusterConfig, currentPrefix);
                connectionConfigs.put(name, clusterConfig);
            }
        } else {
            String natsCorePrefix = "kumuluzee.nats";
            if (configurationUtil.get(natsCorePrefix).isPresent()) {  // single configuration
                SingleNatsConnectionConfig singleConfig = new SingleNatsConnectionConfig();
                readAndSetConnectionConfigClass(singleConfig, natsCorePrefix);
                connectionConfigs.put(singleConfig.getName(), singleConfig);
            } else {
                throw configNotFoundException(natsCorePrefix).get();
            }
        }
    }

    private Supplier<IllegalStateException> configNotFoundException(String configKey) {
        return () -> new IllegalStateException("Configuration key '" + configKey + "' required but not found.");
    }

    private void readAndSetGeneralConfigClass() {
        String prefix = "kumuluzee.nats";
        // response-timeout
        Optional<Integer> responseTimeout = configurationUtil.getInteger(prefix + ".response-timeout");
        responseTimeout.ifPresent(generalConfig::setResponseTimeout);
        // consumer configurations
        Optional<Integer> consumerConfigSize = configurationUtil.getListSize(prefix + ".consumerConfiguration");
        List<NatsConsumerConfiguration> consumerConfigurations = new ArrayList<>();
        if (consumerConfigSize.isPresent()) {
            for (int i = 0; i < consumerConfigSize.get(); i++) {
                consumerConfigurations.add(readConsumerConfiguration(prefix + ".consumerConfiguration" + "[" + i + "]"));
            }
        }
        if (!consumerConfigurations.isEmpty()) {
            generalConfig.setConsumerConfigurations(consumerConfigurations);
        }
    }

    private NatsConsumerConfiguration readConsumerConfiguration(String currentPrefix) {
        NatsConsumerConfiguration consumerConfiguration = new NatsConsumerConfiguration();
        // name
        Optional<String> name = configurationUtil.get(currentPrefix + ".name");
        name.ifPresent(consumerConfiguration::setName);
        // deliver policy
        Optional<String> deliverPolicy = configurationUtil.get(currentPrefix + ".deliverPolicy");
        deliverPolicy.ifPresent(x -> consumerConfiguration.setDeliverPolicy(DeliverPolicy.get(x)));
        // ack policy
        Optional<String> ackPolicy = configurationUtil.get(currentPrefix + ".ackPolicy");
        ackPolicy.ifPresent(x -> consumerConfiguration.setAckPolicy(AckPolicy.get(x)));
        // replay policy
        Optional<String> replayPolicy = configurationUtil.get(currentPrefix + ".replayPolicy");
        replayPolicy.ifPresent(x -> consumerConfiguration.setReplayPolicy(ReplayPolicy.get(x)));
        // description
        Optional<String> description = configurationUtil.get(currentPrefix + ".description");
        description.ifPresent(consumerConfiguration::setDescription);
        // durable
        Optional<String> durable = configurationUtil.get(currentPrefix + ".durable");
        durable.ifPresent(consumerConfiguration::setDurable);
        // deliver subject
        Optional<String> deliverSubject = configurationUtil.get(currentPrefix + ".deliverSubject");
        deliverSubject.ifPresent(consumerConfiguration::setDeliverSubject);
        // deliver group
        Optional<String> deliverGroup = configurationUtil.get(currentPrefix + ".deliverGroup");
        deliverGroup.ifPresent(consumerConfiguration::setDeliverGroup);
        // filter subject
        Optional<String> filterSubject = configurationUtil.get(currentPrefix + ".filterSubject");
        filterSubject.ifPresent(consumerConfiguration::setFilterSubject);
        // sample frequency
        Optional<String> sampleFrequency = configurationUtil.get(currentPrefix + ".sampleFrequency");
        sampleFrequency.ifPresent(consumerConfiguration::setSampleFrequency);
        // start time
        Optional<String> startTime = configurationUtil.get(currentPrefix + ".startTime");
        startTime.ifPresent(x -> consumerConfiguration.setStartTime(ZonedDateTime.parse(startTime.get(), DateTimeFormatter.ISO_DATE_TIME)));
        // ack wait
        Optional<String> ackWait = configurationUtil.get(currentPrefix + ".ackWait");
        ackWait.ifPresent(x -> consumerConfiguration.setAckWait(Duration.parse(ackWait.get())));
        // idle heartbeat
        Optional<String> idleHeartbeat = configurationUtil.get(currentPrefix + ".idleHeartbeat");
        idleHeartbeat.ifPresent(x -> consumerConfiguration.setIdleHeartbeat(Duration.parse(idleHeartbeat.get())));
        // max expires
        Optional<String> maxExpires = configurationUtil.get(currentPrefix + ".maxExpires");
        maxExpires.ifPresent(x -> consumerConfiguration.setMaxExpires(Duration.parse(maxExpires.get())));
        // inactive threshold
        Optional<String> inactiveThreshold = configurationUtil.get(currentPrefix + ".inactiveThreshold");
        inactiveThreshold.ifPresent(x -> consumerConfiguration.setInactiveThreshold(Duration.parse(inactiveThreshold.get())));
        // start seq
        Optional<Long> startSeq = configurationUtil.getLong(currentPrefix + ".startSeq");
        startSeq.ifPresent(consumerConfiguration::setStartSeq);
        // max deliver
        Optional<Long> maxDeliver = configurationUtil.getLong(currentPrefix + ".maxDeliver");
        maxDeliver.ifPresent(consumerConfiguration::setMaxDeliver);
        // rate limit
        Optional<Long> rateLimit = configurationUtil.getLong(currentPrefix + ".rateLimit");
        rateLimit.ifPresent(consumerConfiguration::setRateLimit);
        // max ack pending
        Optional<Long> maxAckPending = configurationUtil.getLong(currentPrefix + ".maxAckPending");
        maxAckPending.ifPresent(consumerConfiguration::setMaxAckPending);
        // max pull waiting
        Optional<Long> maxPullWaiting = configurationUtil.getLong(currentPrefix + ".maxPullWaiting");
        maxPullWaiting.ifPresent(consumerConfiguration::setMaxPullWaiting);
        // max batch
        Optional<Long> maxBatch = configurationUtil.getLong(currentPrefix + ".maxBatch");
        maxBatch.ifPresent(consumerConfiguration::setMaxBatch);
        // max bytes
        Optional<Long> maxBytes = configurationUtil.getLong(currentPrefix + ".maxBytes");
        maxBytes.ifPresent(consumerConfiguration::setMaxBytes);
        // flow control
        Optional<Boolean> flowControl = configurationUtil.getBoolean(currentPrefix + ".flowControl");
        flowControl.ifPresent(consumerConfiguration::setFlowControl);
        // headers only
        Optional<Boolean> headersOnly = configurationUtil.getBoolean(currentPrefix + ".headersOnly");
        headersOnly.ifPresent(consumerConfiguration::setHeadersOnly);
        // backoff
        Optional<Integer> backoffListSize = configurationUtil.getListSize(currentPrefix + ".backoff");
        List<Duration> backoffList = new ArrayList<>();
        if (backoffListSize.isPresent()) {
            for (int i = 0; i < backoffListSize.get(); i++) {
                Optional<String> backoff = configurationUtil.get(currentPrefix + ".backoff" + "[" + i + "]");
                backoff.ifPresent(x -> backoffList.add(Duration.parse(x)));
            }
        }
        if (!backoffList.isEmpty()) {
            consumerConfiguration.setBackoff(backoffList);
        }

        return consumerConfiguration;
    }

    private void readAndSetConnectionConfigClass(NatsConnectionConfig natsConnectionConfig, String currentPrefix) {
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
                streams.add(readStreamsConfiguration(currentPrefix + ".streams" + "[" + i + "]"));
            }
        }
        if (!streams.isEmpty()) {
            natsConnectionConfig.setStreamConfigurations(streams);
        }

        // jetStreamContext options
        Optional<Integer> jetStreamContextsSize = configurationUtil.getListSize(currentPrefix + ".jetStreamContexts");
        Map<String, JetStreamOptions> jetStreamContexts = new HashMap<>();
        if (jetStreamContextsSize.isPresent()) {
            for (int i = 0; i < jetStreamContextsSize.get(); i++) {
                NamedJetStreamOptions namedJetStreamOptions = readJetStreamOptions(currentPrefix + ".jetStreamContexts" + "[" + i + "]");
                jetStreamContexts.put(namedJetStreamOptions.getName(), namedJetStreamOptions.getJetStreamOptions());
            }
        }
        if (!jetStreamContexts.isEmpty()) {
            natsConnectionConfig.setJetStreamContextOptions(jetStreamContexts);
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

    private StreamConfiguration readStreamsConfiguration(String currentPrefix) {
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

    private NamedJetStreamOptions readJetStreamOptions(String currentPrefix) {
        NamedJetStreamOptions namedJetStreamOptions = new NamedJetStreamOptions();
        JetStreamOptions.Builder builder = JetStreamOptions.builder();
        // name
        Optional<String> name = configurationUtil.get(currentPrefix + ".name");
        if (!name.isPresent()) {
            throw configNotFoundException(currentPrefix + ".name").get();
        }
        namedJetStreamOptions.setName(name.get());
        // domain
        Optional<String> domain = configurationUtil.get(currentPrefix + ".domain");
        domain.ifPresent(builder::domain);
        // prefix
        Optional<String> prefix = configurationUtil.get(currentPrefix + ".prefix");
        prefix.ifPresent(builder::prefix);
        // publishNoAck
        Optional<Boolean> publishNoAck = configurationUtil.getBoolean(currentPrefix + ".publishNoAck");
        publishNoAck.ifPresent(builder::publishNoAck);
        // request timeout
        Optional<Long> requestTimeout = configurationUtil.getLong(currentPrefix + ".requestTimeout");
        requestTimeout.ifPresent(x -> builder.requestTimeout(Duration.ofSeconds(x)));

        namedJetStreamOptions.setJetStreamOptions(builder.build());
        return namedJetStreamOptions;
    }

    private static class NamedJetStreamOptions {
        private String name;
        private JetStreamOptions jetStreamOptions;

        public NamedJetStreamOptions() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public JetStreamOptions getJetStreamOptions() {
            return jetStreamOptions;
        }

        public void setJetStreamOptions(JetStreamOptions jetStreamOptions) {
            this.jetStreamOptions = jetStreamOptions;
        }
    }

    private static class NamedMap {
        private String name;
        private Map<String, String> map;

        public NamedMap() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, String> getMap() {
            return map;
        }

        public void setMap(Map<String, String> map) {
            this.map = map;
        }
    }
}
