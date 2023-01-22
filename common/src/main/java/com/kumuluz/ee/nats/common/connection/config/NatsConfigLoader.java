package com.kumuluz.ee.nats.common.connection.config;

import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import com.kumuluz.ee.nats.common.exception.ConfigurationException;
import io.nats.client.JetStreamOptions;
import io.nats.client.api.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

/**
 * Class used for reading and parsing the configurations.
 * You can obtain the configurations by calling methods: getGeneralConfig(), getConnectionConfigs(), getConfigForConnection().
 *
 * @author Matej Bizjak
 */

public class NatsConfigLoader {

    private static NatsConfigLoader instance;
    private static GeneralConfig generalConfig;
    private static final HashMap<String, ConnectionConfig> CONNECTION_CONFIGS = new HashMap<>();
    private final ConfigurationUtil configurationUtil = ConfigurationUtil.getInstance();

    public static NatsConfigLoader getInstance() {
        if (instance == null) {
            instance = new NatsConfigLoader();
        }
        return instance;
    }

    public GeneralConfig getGeneralConfig() {
        return generalConfig;
    }

    public HashMap<String, ConnectionConfig> getConnectionConfigs() {
        return CONNECTION_CONFIGS;
    }

    public ConnectionConfig getConfigForConnection(String connectionName) {
        return CONNECTION_CONFIGS.get(connectionName);
    }

    public void readConfiguration() {
        // general settings
        generalConfig = new GeneralConfig();
        readAndSetGeneralConfigClass();
        // connection settings
        String clusterPrefix = "kumuluzee.nats.servers";
        Optional<Integer> size = configurationUtil.getListSize(clusterPrefix);
        if (size.isPresent()) {  // cluster configuration
            for (int i = 0; i < size.get(); i++) {
                String currentPrefix = clusterPrefix + "[" + i + "]";
                String name = configurationUtil.get(currentPrefix + ".name")
                        .orElseThrow(configNotFoundException(currentPrefix + ".name"));
                ClusterConnectionConfig clusterConfig = new ClusterConnectionConfig(name);
                readAndSetConnectionConfigClass(clusterConfig, currentPrefix);
                CONNECTION_CONFIGS.put(name, clusterConfig);
            }
        } else {
            String natsCorePrefix = "kumuluzee.nats";
            if (configurationUtil.get(natsCorePrefix).isPresent()) {  // single configuration
                SingleConnectionConfig singleConfig = new SingleConnectionConfig();
                readAndSetConnectionConfigClass(singleConfig, natsCorePrefix);
                CONNECTION_CONFIGS.put(singleConfig.getName(), singleConfig);
            } else {
                throw configNotFoundException(natsCorePrefix).get();
            }
        }
    }

    private Supplier<ConfigurationException> configNotFoundException(String configKey) {
        return () -> new ConfigurationException("Configuration key '" + configKey + "' required but not found.");
    }

    private void readAndSetGeneralConfigClass() {
        String prefix = "kumuluzee.nats";
        GeneralConfig.Builder builder = GeneralConfig.builder();
        // response timeout
        Optional<String> responseTimeout = configurationUtil.get(prefix + ".response-timeout");
        responseTimeout.ifPresent(x -> builder.responseTimeout(Duration.parse(x)));
        // ack confirmation timeout
        Optional<String> ackConfirmationTimeout = configurationUtil.get(prefix + ".ack-confirmation-timeout");
        ackConfirmationTimeout.ifPresent(x -> builder.ackConfirmationTimeout(Duration.parse(x)));
        // ack confirmation retries
        Optional<Integer> ackConfirmationRetries = configurationUtil.getInteger(prefix + ".ack-confirmation-retries");
        ackConfirmationRetries.ifPresent(builder::ackConfirmationRetries);
        // drain timeout
        Optional<String> drainTimeout = configurationUtil.get(prefix + ".drain-timeout");
        drainTimeout.ifPresent(x -> builder.drainTimeout(Duration.parse(x)));
        generalConfig = builder.build();
    }

    private ConsumerConfiguration readConsumerConfiguration(String currentPrefix) {
        ConsumerConfiguration consumerConfiguration = new ConsumerConfiguration();
        // name
        Optional<String> name = configurationUtil.get(currentPrefix + ".name");
        name.ifPresent(consumerConfiguration::setName);
        // deliver policy
        Optional<String> deliverPolicy = configurationUtil.get(currentPrefix + ".deliver-policy");
        deliverPolicy.ifPresent(x -> consumerConfiguration.setDeliverPolicy(DeliverPolicy.get(x)));
        // ack policy
        Optional<String> ackPolicy = configurationUtil.get(currentPrefix + ".ack-policy");
        ackPolicy.ifPresent(x -> consumerConfiguration.setAckPolicy(AckPolicy.get(x)));
        // replay policy
        Optional<String> replayPolicy = configurationUtil.get(currentPrefix + ".replay-policy");
        replayPolicy.ifPresent(x -> consumerConfiguration.setReplayPolicy(ReplayPolicy.get(x)));
        // description
        Optional<String> description = configurationUtil.get(currentPrefix + ".description");
        description.ifPresent(consumerConfiguration::setDescription);
        // deliver subject
        Optional<String> deliverSubject = configurationUtil.get(currentPrefix + ".deliver-subject");
        deliverSubject.ifPresent(consumerConfiguration::setDeliverSubject);
        // deliver group
        Optional<String> deliverGroup = configurationUtil.get(currentPrefix + ".deliver-group");
        deliverGroup.ifPresent(consumerConfiguration::setDeliverGroup);
        // filter subject
        Optional<String> filterSubject = configurationUtil.get(currentPrefix + ".filter-subject");
        filterSubject.ifPresent(consumerConfiguration::setFilterSubject);
        // sample frequency
        Optional<String> sampleFrequency = configurationUtil.get(currentPrefix + ".sample-frequency");
        sampleFrequency.ifPresent(consumerConfiguration::setSampleFrequency);
        // start time
        Optional<String> startTime = configurationUtil.get(currentPrefix + ".start-time");
        startTime.ifPresent(x -> consumerConfiguration.setStartTime(ZonedDateTime.parse(x, DateTimeFormatter.ISO_DATE_TIME)));
        // ack wait
        Optional<String> ackWait = configurationUtil.get(currentPrefix + ".ack-wait");
        ackWait.ifPresent(x -> consumerConfiguration.setAckWait(Duration.parse(x)));
        // idle heartbeat
        Optional<String> idleHeartbeat = configurationUtil.get(currentPrefix + ".idle-heartbeat");
        idleHeartbeat.ifPresent(x -> consumerConfiguration.setIdleHeartbeat(Duration.parse(x)));
        // max expires
        Optional<String> maxExpires = configurationUtil.get(currentPrefix + ".max-expires");
        maxExpires.ifPresent(x -> consumerConfiguration.setMaxExpires(Duration.parse(x)));
        // inactive threshold
        Optional<String> inactiveThreshold = configurationUtil.get(currentPrefix + ".inactive-threshold");
        inactiveThreshold.ifPresent(x -> consumerConfiguration.setInactiveThreshold(Duration.parse(x)));
        // start seq
        Optional<Long> startSeq = configurationUtil.getLong(currentPrefix + ".start-seq");
        startSeq.ifPresent(consumerConfiguration::setStartSeq);
        // max deliver
        Optional<Long> maxDeliver = configurationUtil.getLong(currentPrefix + ".max-deliver");
        maxDeliver.ifPresent(consumerConfiguration::setMaxDeliver);
        // rate limit
        Optional<Long> rateLimit = configurationUtil.getLong(currentPrefix + ".rate-limit");
        rateLimit.ifPresent(consumerConfiguration::setRateLimit);
        // max ack pending
        Optional<Long> maxAckPending = configurationUtil.getLong(currentPrefix + ".max-ack-pending");
        maxAckPending.ifPresent(consumerConfiguration::setMaxAckPending);
        // max pull waiting
        Optional<Long> maxPullWaiting = configurationUtil.getLong(currentPrefix + ".max-pull-waiting");
        maxPullWaiting.ifPresent(consumerConfiguration::setMaxPullWaiting);
        // max batch
        Optional<Long> maxBatch = configurationUtil.getLong(currentPrefix + ".max-batch");
        maxBatch.ifPresent(consumerConfiguration::setMaxBatch);
        // max bytes
        Optional<Long> maxBytes = configurationUtil.getLong(currentPrefix + ".max-bytes");
        maxBytes.ifPresent(consumerConfiguration::setMaxBytes);
        // num replicas
        Optional<Integer> numReplicas = configurationUtil.getInteger(currentPrefix + ".num-replicas");
        numReplicas.ifPresent(consumerConfiguration::setNumReplicas);
        // flow control
        Optional<Boolean> flowControl = configurationUtil.getBoolean(currentPrefix + ".flow-control");
        flowControl.ifPresent(consumerConfiguration::setFlowControl);
        // headers only
        Optional<Boolean> headersOnly = configurationUtil.getBoolean(currentPrefix + ".headers-only");
        headersOnly.ifPresent(consumerConfiguration::setHeadersOnly);
        // headers only
        Optional<Boolean> memStorage = configurationUtil.getBoolean(currentPrefix + ".mem-storage");
        memStorage.ifPresent(consumerConfiguration::setMemStorage);
        // backoff
        Optional<Integer> backoffListSize = configurationUtil.getListSize(currentPrefix + ".backoff");
        List<Duration> backoffList = new ArrayList<>();
        if (backoffListSize.isPresent()) {
            for (int i = 0; i < backoffListSize.get(); i++) {
                Optional<String> backoff = configurationUtil.get(currentPrefix + ".backoff" + "[" + i + "]");
                backoff.ifPresent(x -> backoffList.add(Duration.parse(x)));
            }
        }
        consumerConfiguration.setBackoff(backoffList);

        return consumerConfiguration;
    }

    private void readAndSetConnectionConfigClass(ConnectionConfig connectionConfig, String currentPrefix) {
        // addresses
        Optional<Integer> addressesSize = configurationUtil.getListSize(currentPrefix + ".addresses");
        List<String> addresses = new ArrayList<>();
        if (addressesSize.isPresent()) {
            for (int i = 0; i < addressesSize.get(); i++) {
                Optional<String> address = configurationUtil.get(currentPrefix + ".addresses" + "[" + i + "]");
                address.ifPresent(addresses::add);
            }
        }
        connectionConfig.setAddresses(addresses);

        // max reconnect
        Optional<Integer> maxReconnect = configurationUtil.getInteger(currentPrefix + ".max-reconnect");
        maxReconnect.ifPresent(connectionConfig::setMaxReconnect);
        // reconnect wait
        Optional<String> reconnectWait = configurationUtil.get(currentPrefix + ".reconnect-wait");
        reconnectWait.ifPresent(x -> connectionConfig.setReconnectWait(Duration.parse(x)));
        // connection timeout
        Optional<String> connectionTimeout = configurationUtil.get(currentPrefix + ".connection-timeout");
        connectionTimeout.ifPresent(x -> connectionConfig.setConnectionTimeout(Duration.parse(x)));
        // ping interval
        Optional<String> pingInterval = configurationUtil.get(currentPrefix + ".ping-interval");
        pingInterval.ifPresent(x -> connectionConfig.setPingInterval(Duration.parse(x)));
        // reconnect-buffer size
        Optional<Long> reconnectBufferSize = configurationUtil.getLong(currentPrefix + ".reconnect-buffer-size");
        reconnectBufferSize.ifPresent(connectionConfig::setReconnectBufferSize);
        // inbox prefix
        Optional<String> inboxPrefix = configurationUtil.get(currentPrefix + ".inbox-prefix");
        inboxPrefix.ifPresent(connectionConfig::setInboxPrefix);
        // no echo
        Optional<Boolean> noEcho = configurationUtil.getBoolean(currentPrefix + ".no-echo");
        noEcho.ifPresent(connectionConfig::setNoEcho);
        // username
        Optional<String> username = configurationUtil.get(currentPrefix + ".username");
        username.ifPresent(connectionConfig::setUsername);
        // password
        Optional<String> password = configurationUtil.get(currentPrefix + ".password");
        password.ifPresent(connectionConfig::setPassword);
        // credentials
        Optional<String> credentials = configurationUtil.get(currentPrefix + ".credentials");
        credentials.ifPresent(connectionConfig::setCredentials);

        // (jet)streams
        Optional<Integer> streamsSize = configurationUtil.getListSize(currentPrefix + ".streams");
        List<StreamConsumerConfiguration> streams = new ArrayList<>();
        if (streamsSize.isPresent()) {
            for (int i = 0; i < streamsSize.get(); i++) {
                streams.add(readStreamsConfiguration(currentPrefix + ".streams" + "[" + i + "]"));
            }
        }
        connectionConfig.setStreamConsumerConfigurations(streams);

        // jetStreamContext options
        Optional<Integer> jetStreamContextsSize = configurationUtil.getListSize(currentPrefix + ".jetstream-contexts");
        Map<String, JetStreamOptions> jetStreamContexts = new HashMap<>();
        if (jetStreamContextsSize.isPresent()) {
            for (int i = 0; i < jetStreamContextsSize.get(); i++) {
                NamedJetStreamOptions namedJetStreamOptions = readJetStreamOptions(currentPrefix + ".jetstream-contexts" + "[" + i + "]");
                jetStreamContexts.put(namedJetStreamOptions.getName(), namedJetStreamOptions.getJetStreamOptions());
            }
        }
        connectionConfig.setJetStreamContextOptions(jetStreamContexts);

        // TLS
        Optional<List<String>> tlsKeys = configurationUtil.getMapKeys(currentPrefix + ".tls");
        if (tlsKeys.isEmpty()) {
            return;
        }
        ConnectionConfig.TLS tls = new ConnectionConfig.TLS();
        // trust store type
        Optional<String> trustStoreType = configurationUtil.get(currentPrefix + ".tls" + ".trust-store-type");
        trustStoreType.ifPresent(tls::setTrustStoreType);
        // trust store path
        Optional<String> trustStorePath = configurationUtil.get(currentPrefix + ".tls" + ".trust-store-path");
        trustStorePath.ifPresent(tls::setTrustStorePath);
        // trust store password
        Optional<String> trustStorePassword = configurationUtil.get(currentPrefix + ".tls" + ".trust-store-password");
        trustStorePassword.ifPresent(tls::setTrustStorePassword);
        // certificate path
        Optional<String> certificatePath = configurationUtil.get(currentPrefix + ".tls" + ".certificate-path");
        certificatePath.ifPresent(tls::setCertificatePath);
        // key store path
        Optional<String> keyStorePath = configurationUtil.get(currentPrefix + ".tls" + ".key-store-path");
        keyStorePath.ifPresent(tls::setKeyStorePath);
        // key store password
        Optional<String> keyStorePassword = configurationUtil.get(currentPrefix + ".tls" + ".key-store-password");
        keyStorePassword.ifPresent(tls::setKeyStorePassword);
        // key store type
        Optional<String> keyStoreType = configurationUtil.get(currentPrefix + ".tls" + ".key-store-type");
        keyStoreType.ifPresent(tls::setKeyStoreType);
        connectionConfig.setTls(tls);
    }

    private StreamConsumerConfiguration readStreamsConfiguration(String currentPrefix) {
        StreamConsumerConfiguration.Builder streamConsumerBuilder = StreamConsumerConfiguration.builder();

        // stream
        StreamConfiguration.Builder streamBuilder = StreamConfiguration.builder();
        // name
        String name = configurationUtil.get(currentPrefix + ".name").orElseThrow(configNotFoundException(currentPrefix + ".name"));
        streamBuilder.name(name);
        // subjects
        Optional<Integer> subjectsSize = configurationUtil.getListSize(currentPrefix + ".subjects");
        List<String> subjects = new ArrayList<>();
        if (subjectsSize.isPresent()) {
            for (int i = 0; i < subjectsSize.get(); i++) {
                Optional<String> subject = configurationUtil.get(currentPrefix + ".subjects" + "[" + i + "]");
                subject.ifPresent(subjects::add);
            }
        }
        streamBuilder.subjects(subjects);
        // description
        Optional<String> description = configurationUtil.get(currentPrefix + ".description");
        description.ifPresent(streamBuilder::description);
        // retention policy
        Optional<String> retentionPolicy = configurationUtil.get(currentPrefix + ".retention-policy");
        retentionPolicy.ifPresent(x -> streamBuilder.retentionPolicy(RetentionPolicy.get(x)));
        // max consumers
        Optional<Long> maxConsumers = configurationUtil.getLong(currentPrefix + ".max-consumers");
        maxConsumers.ifPresent(streamBuilder::maxConsumers);
        // max bytes
        Optional<Long> maxBytes = configurationUtil.getLong(currentPrefix + ".max-bytes");
        maxBytes.ifPresent(streamBuilder::maxBytes);
        // max age
        Optional<Long> maxAge = configurationUtil.getLong(currentPrefix + ".max-age");
        maxAge.ifPresent(streamBuilder::maxAge);
        // max messages
        Optional<Long> maxMsgs = configurationUtil.getLong(currentPrefix + ".max-msgs");
        maxMsgs.ifPresent(streamBuilder::maxMessages);
        // max message size
        Optional<Long> maxMsgSize = configurationUtil.getLong(currentPrefix + ".max-msg-size");
        maxMsgSize.ifPresent(streamBuilder::maxMsgSize);
        // storage type
        Optional<String> storageType = configurationUtil.get(currentPrefix + ".storage-type");
        storageType.ifPresent(x -> streamBuilder.storageType(StorageType.get(x)));
        // replicas
        Optional<Integer> replicas = configurationUtil.getInteger(currentPrefix + ".replicas");
        replicas.ifPresent(streamBuilder::replicas);
        // no ack
        Optional<Boolean> noAck = configurationUtil.getBoolean(currentPrefix + ".no-ack");
        noAck.ifPresent(streamBuilder::noAck);
        // template owner
        Optional<String> templateOwner = configurationUtil.get(currentPrefix + ".template-owner");
        templateOwner.ifPresent(streamBuilder::templateOwner);
        // discard policy
        Optional<String> discardPolicy = configurationUtil.get(currentPrefix + ".discard-policy");
        discardPolicy.ifPresent(x -> streamBuilder.discardPolicy(DiscardPolicy.get(x)));
        // duplicate window
        Optional<String> duplicateWindow = configurationUtil.get(currentPrefix + ".duplicate-window");
        if (duplicateWindow.isPresent()) {
            streamBuilder.duplicateWindow(Duration.parse(duplicateWindow.get()));
        } else {
            // This is a default value on the server anyway but StreamConfiguration builder defaults it to ZERO
            // which is then overwritten on the server by 2 min. To make StreamManagement.configurationsChanged() work
            // properly we set it to 2 min at the beginning. https://github.com/nats-io/nats.java/issues/682
            streamBuilder.duplicateWindow(Duration.ofMinutes(2));
        }

        // consumers
        Optional<Integer> consumersSize = configurationUtil.getListSize(currentPrefix + ".consumers");
        List<ConsumerConfiguration> consumerConfigurations = new ArrayList<>();
        if (consumersSize.isPresent()) {
            for (int i = 0; i < consumersSize.get(); i++) {
                consumerConfigurations.add(readConsumerConfiguration(currentPrefix + ".consumers" + "[" + i + "]"));
            }
        }
        streamConsumerBuilder.consumerConfigurations(consumerConfigurations);

        streamConsumerBuilder.streamName(name);
        streamConsumerBuilder.streamConfiguration(streamBuilder.build());

        return streamConsumerBuilder.build();
    }

    private NamedJetStreamOptions readJetStreamOptions(String currentPrefix) {
        JetStreamOptions.Builder builder = JetStreamOptions.builder();
        NamedJetStreamOptions.Builder namedBuilder = new NamedJetStreamOptions.Builder();
        // name
        Optional<String> name = configurationUtil.get(currentPrefix + ".name");
        if (name.isEmpty()) {
            throw configNotFoundException(currentPrefix + ".name").get();
        }
        namedBuilder.name(name.get());
        // domain
        Optional<String> domain = configurationUtil.get(currentPrefix + ".domain");
        domain.ifPresent(builder::domain);
        // prefix
        Optional<String> prefix = configurationUtil.get(currentPrefix + ".prefix");
        prefix.ifPresent(builder::prefix);
        // publishNoAck
        Optional<Boolean> publishNoAck = configurationUtil.getBoolean(currentPrefix + ".publish-no-ack");
        publishNoAck.ifPresent(builder::publishNoAck);
        // request timeout
        Optional<String> requestTimeout = configurationUtil.get(currentPrefix + ".request-timeout");
        requestTimeout.ifPresent(x -> builder.requestTimeout(Duration.parse(x)));

        namedBuilder.jetStreamOptions(builder.build());
        return namedBuilder.build();
    }

    private static class NamedJetStreamOptions {

        private static class Builder {
            private String name;
            private JetStreamOptions jetStreamOptions;

            private Builder name(String name) {
                this.name = name;
                return this;
            }

            private Builder jetStreamOptions(JetStreamOptions jetStreamOptions) {
                this.jetStreamOptions = jetStreamOptions;
                return this;
            }

            private NamedJetStreamOptions build() {
                NamedJetStreamOptions namedJetStreamOptions = new NamedJetStreamOptions();
                namedJetStreamOptions.name = name;
                namedJetStreamOptions.jetStreamOptions = jetStreamOptions;
                return namedJetStreamOptions;
            }
        }

        private String name;
        private JetStreamOptions jetStreamOptions;

        public NamedJetStreamOptions() {
        }

        public String getName() {
            return name;
        }

        public JetStreamOptions getJetStreamOptions() {
            return jetStreamOptions;
        }
    }
}
