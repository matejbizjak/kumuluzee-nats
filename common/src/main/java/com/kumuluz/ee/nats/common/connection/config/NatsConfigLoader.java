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
        // response timeout
        Optional<String> responseTimeout = configurationUtil.get(prefix + ".response-timeout");
        responseTimeout.ifPresent(x -> generalConfig.setResponseTimeout(Duration.parse("PT" + x)));
        // ack confirmation timeout
        Optional<String> ackConfirmationTimeout = configurationUtil.get(prefix + ".ack-confirmation-timeout");
        ackConfirmationTimeout.ifPresent(x -> generalConfig.setAckConfirmationTimeout(Duration.parse("PT" + x)));
        // ack confirmation retries
        Optional<Integer> ackConfirmationRetries = configurationUtil.getInteger(prefix + ".ack-confirmation-retries");
        ackConfirmationRetries.ifPresent(generalConfig::setAckConfirmationRetries);
        // consumer configurations
        Optional<Integer> consumerConfigSize = configurationUtil.getListSize(prefix + ".consumer-configuration");
        List<NatsConsumerConfiguration> consumerConfigurations = new ArrayList<>();
        if (consumerConfigSize.isPresent()) {
            for (int i = 0; i < consumerConfigSize.get(); i++) {
                consumerConfigurations.add(readConsumerConfiguration(prefix + ".consumer-configuration" + "[" + i + "]"));
            }
        }
        generalConfig.setConsumerConfigurations(consumerConfigurations);
    }

    private NatsConsumerConfiguration readConsumerConfiguration(String currentPrefix) {
        NatsConsumerConfiguration consumerConfiguration = new NatsConsumerConfiguration();
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
        // durable
        Optional<String> durable = configurationUtil.get(currentPrefix + ".durable");
        durable.ifPresent(consumerConfiguration::setDurable);
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
        ackWait.ifPresent(x -> consumerConfiguration.setAckWait(Duration.parse("PT" + x)));
        // idle heartbeat
        Optional<String> idleHeartbeat = configurationUtil.get(currentPrefix + ".idle-heartbeat");
        idleHeartbeat.ifPresent(x -> consumerConfiguration.setIdleHeartbeat(Duration.parse("PT" + x)));
        // max expires
        Optional<String> maxExpires = configurationUtil.get(currentPrefix + ".max-expires");
        maxExpires.ifPresent(x -> consumerConfiguration.setMaxExpires(Duration.parse("PT" + x)));
        // inactive threshold
        Optional<String> inactiveThreshold = configurationUtil.get(currentPrefix + ".inactive-threshold");
        inactiveThreshold.ifPresent(x -> consumerConfiguration.setInactiveThreshold(Duration.parse("PT" + x)));
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
        // flow control
        Optional<Boolean> flowControl = configurationUtil.getBoolean(currentPrefix + ".flow-control");
        flowControl.ifPresent(consumerConfiguration::setFlowControl);
        // headers only
        Optional<Boolean> headersOnly = configurationUtil.getBoolean(currentPrefix + ".headers-only");
        headersOnly.ifPresent(consumerConfiguration::setHeadersOnly);
        // backoff
        Optional<Integer> backoffListSize = configurationUtil.getListSize(currentPrefix + ".backoff");
        List<Duration> backoffList = new ArrayList<>();
        if (backoffListSize.isPresent()) {
            for (int i = 0; i < backoffListSize.get(); i++) {
                Optional<String> backoff = configurationUtil.get(currentPrefix + ".backoff" + "[" + i + "]");
                backoff.ifPresent(x -> backoffList.add(Duration.parse("PT" + x)));
            }
        }
        consumerConfiguration.setBackoff(backoffList);

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
        natsConnectionConfig.setAddresses(addresses);

        // max reconnect
        Optional<Integer> maxReconnect = configurationUtil.getInteger(currentPrefix + ".max-reconnect");
        maxReconnect.ifPresent(natsConnectionConfig::setMaxReconnect);
        // reconnect wait
        Optional<String> reconnectWait = configurationUtil.get(currentPrefix + ".reconnect-wait");
        reconnectWait.ifPresent(x -> natsConnectionConfig.setReconnectWait(Duration.parse("PT" + x)));
        // connection timeout
        Optional<String> connectionTimeout = configurationUtil.get(currentPrefix + ".connection-timeout");
        connectionTimeout.ifPresent(x -> natsConnectionConfig.setConnectionTimeout(Duration.parse("PT" + x)));
        // ping interval
        Optional<String> pingInterval = configurationUtil.get(currentPrefix + ".ping-interval");
        pingInterval.ifPresent(x -> natsConnectionConfig.setPingInterval(Duration.parse("PT" + x)));
        // reconnect-buffer size
        Optional<Long> reconnectBufferSize = configurationUtil.getLong(currentPrefix + ".reconnect-buffer-size");
        reconnectBufferSize.ifPresent(natsConnectionConfig::setReconnectBufferSize);
        // inbox prefix
        Optional<String> inboxPrefix = configurationUtil.get(currentPrefix + ".inbox-prefix");
        inboxPrefix.ifPresent(natsConnectionConfig::setInboxPrefix);
        // no echo
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
        natsConnectionConfig.setStreamConfigurations(streams);

        // jetStreamContext options
        Optional<Integer> jetStreamContextsSize = configurationUtil.getListSize(currentPrefix + ".jetstream-contexts");
        Map<String, JetStreamOptions> jetStreamContexts = new HashMap<>();
        if (jetStreamContextsSize.isPresent()) {
            for (int i = 0; i < jetStreamContextsSize.get(); i++) {
                NamedJetStreamOptions namedJetStreamOptions = readJetStreamOptions(currentPrefix + ".jetstream-contexts" + "[" + i + "]");
                jetStreamContexts.put(namedJetStreamOptions.getName(), namedJetStreamOptions.getJetStreamOptions());
            }
        }
        natsConnectionConfig.setJetStreamContextOptions(jetStreamContexts);

        // TLS
        Optional<String> tlsConf = configurationUtil.get(currentPrefix + ".tls");
        if (!tlsConf.isPresent()) {
            return;
        }
        NatsConnectionConfig.TLS tls = new NatsConnectionConfig.TLS();
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
        builder.subjects(subjects);
        // description
        Optional<String> description = configurationUtil.get(currentPrefix + ".description");
        description.ifPresent(builder::description);
        // retention policy
        Optional<String> retentionPolicy = configurationUtil.get(currentPrefix + ".retention-policy");
        retentionPolicy.ifPresent(x -> builder.retentionPolicy(RetentionPolicy.get(x)));
        // max consumers
        Optional<Long> maxConsumers = configurationUtil.getLong(currentPrefix + ".max-consumers");
        maxConsumers.ifPresent(builder::maxConsumers);
        // max bytes
        Optional<Long> maxBytes = configurationUtil.getLong(currentPrefix + ".max-bytes");
        maxBytes.ifPresent(builder::maxBytes);
        // max age
        Optional<Long> maxAge = configurationUtil.getLong(currentPrefix + ".max-age");
        maxAge.ifPresent(builder::maxAge);
        // max messages
        Optional<Long> maxMsgs = configurationUtil.getLong(currentPrefix + ".max-msgs");
        maxMsgs.ifPresent(builder::maxMessages);
        // max message size
        Optional<Long> maxMsgSize = configurationUtil.getLong(currentPrefix + ".max-msg-size");
        maxMsgSize.ifPresent(builder::maxMsgSize);
        // storage type
        Optional<String> storageType = configurationUtil.get(currentPrefix + ".storage-type");
        storageType.ifPresent(x -> builder.storageType(StorageType.get(x)));
        // replicas
        Optional<Integer> replicas = configurationUtil.getInteger(currentPrefix + ".replicas");
        replicas.ifPresent(builder::replicas);
        // no ack
        Optional<Boolean> noAck = configurationUtil.getBoolean(currentPrefix + ".no-ack");
        noAck.ifPresent(builder::noAck);
        // template owner
        Optional<String> templateOwner = configurationUtil.get(currentPrefix + ".template-owner");
        templateOwner.ifPresent(builder::templateOwner);
        // discard policy
        Optional<String> discardPolicy = configurationUtil.get(currentPrefix + ".discard-policy");
        discardPolicy.ifPresent(x -> builder.discardPolicy(DiscardPolicy.get(x)));
        // duplicate window
//        Optional<String> duplicateWindow = configurationUtil.get(currentPrefix + ".duplicateWindow");
//        duplicateWindow.ifPresent(x -> builder.duplicateWindow(Duration.parse(duplicateWindow.get())));
        Optional<String> duplicateWindow = configurationUtil.get(currentPrefix + ".duplicate-window");
        if (duplicateWindow.isPresent()) {
            builder.duplicateWindow(Duration.parse("PT" + duplicateWindow.get()));
        } else {
            // This is a default value on the server anyway but StreamConfiguration builder defaults it to ZERO
            // which is then overwritten on the server by 2 min. To make StreamManagement.configurationsChanged() work
            // properly we set it to 2 min at the beginning. https://github.com/nats-io/nats.java/issues/682
            builder.duplicateWindow(Duration.ofMinutes(2));
        }

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
        Optional<Boolean> publishNoAck = configurationUtil.getBoolean(currentPrefix + ".publish-no-ack");
        publishNoAck.ifPresent(builder::publishNoAck);
        // request timeout
        Optional<String> requestTimeout = configurationUtil.get(currentPrefix + ".request-timeout");
        requestTimeout.ifPresent(x -> builder.requestTimeout(Duration.parse("PT" + x)));

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
}
