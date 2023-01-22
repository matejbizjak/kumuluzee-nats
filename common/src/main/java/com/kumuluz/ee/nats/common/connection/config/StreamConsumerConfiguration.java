package com.kumuluz.ee.nats.common.connection.config;

import com.kumuluz.ee.nats.common.annotations.ConfigurationOverride;
import com.kumuluz.ee.nats.common.annotations.ConsumerConfig;
import com.kumuluz.ee.nats.common.management.ConsumerManagement;
import io.nats.client.Connection;
import io.nats.client.api.AckPolicy;
import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;
import io.nats.client.api.StreamConfiguration;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Matej Bizjak
 */

public class StreamConsumerConfiguration {

    public static class Builder {
        private String streamName;
        private StreamConfiguration streamConfiguration;
        private List<ConsumerConfiguration> consumerConfigurations = new ArrayList<>();

        public Builder streamName(String streamName) {
            this.streamName = streamName;
            return this;
        }

        public Builder streamConfiguration(StreamConfiguration streamConfiguration) {
            this.streamConfiguration = streamConfiguration;
            return this;
        }

        public Builder consumerConfigurations(List<ConsumerConfiguration> consumerConfigurations) {
            this.consumerConfigurations = consumerConfigurations;
            return this;
        }

        public StreamConsumerConfiguration build() {
            StreamConsumerConfiguration streamConsumerConfiguration = new StreamConsumerConfiguration();
            streamConsumerConfiguration.streamName = streamName;
            streamConsumerConfiguration.streamConfiguration = streamConfiguration;
            streamConsumerConfiguration.consumerConfigurations = consumerConfigurations;
            return streamConsumerConfiguration;
        }
    }

    private String streamName;
    private StreamConfiguration streamConfiguration;
    private List<ConsumerConfiguration> consumerConfigurations;

    public String getStreamName() {
        return streamName;
    }

    public StreamConfiguration getStreamConfiguration() {
        return streamConfiguration;
    }

    public List<ConsumerConfiguration> getConsumerConfigurations() {
        return consumerConfigurations;
    }

    public ConsumerConfiguration getConsumerConfiguration(String name) {
        return consumerConfigurations.stream()
                .filter(x -> Objects.equals(x.getName(), name))
                .findFirst()
                .orElse(null);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<io.nats.client.api.ConsumerConfiguration> getAndCombineConsumerConfig(Connection connection, String consumerName, ConsumerConfig consumerConfigAnnotation) {
        ConsumerConfiguration consumerConfiguration;
        if (consumerConfigAnnotation == null) {  // nothing to combine
            consumerConfiguration = getConsumerConfiguration(consumerName);
            if (consumerConfiguration == null) {
                return Optional.empty();
            } else {
                return Optional.of(buildConsumerConfiguration(consumerConfiguration));
            }
        } else {  // combine existing config and overrided values
            String newConsumerName = consumerName;
            String oldConsumerName = consumerConfigAnnotation.base();

            if (oldConsumerName.isEmpty()) {  // if base consumer is not set, just use the default config as a base
                consumerConfiguration = new ConsumerConfiguration();
            } else {
                consumerConfiguration = getConsumerConfiguration(oldConsumerName);  // try to find the consumer configuration locally - configuration
                if (consumerConfiguration == null) {
                    consumerConfiguration = unbuildConsumerConfiguration(ConsumerManagement.getConsumerConfigurationOrNullWhenNotExist(connection
                            , streamName, oldConsumerName));  // try to find the consumer at the server
                    if (consumerConfiguration == null) {
                        return Optional.empty();
                    }
                }
            }

            consumerConfiguration.setName(newConsumerName);
            for (ConfigurationOverride override : consumerConfigAnnotation.configOverrides()) {
                String key = override.key();
                String value = override.value();

                switch (key) {
                    case "deliver-policy":
                        consumerConfiguration.setDeliverPolicy(DeliverPolicy.get(value));
                        break;
                    case "ack-policy":
                        consumerConfiguration.setAckPolicy(AckPolicy.get(value));
                        break;
                    case "replay-policy":
                        consumerConfiguration.setReplayPolicy(ReplayPolicy.get(value));
                        break;
                    case "description":
                        consumerConfiguration.setDescription(value);
                        break;
                    case "deliver-subject":
                        consumerConfiguration.setDeliverSubject(value);
                        break;
                    case "deliver-group":
                        consumerConfiguration.setDeliverGroup(value);
                        break;
                    case "filter-subject":
                        consumerConfiguration.setFilterSubject(value);
                        break;
                    case "sample-frequency":
                        consumerConfiguration.setSampleFrequency(value);
                        break;
                    case "start-time":
                        consumerConfiguration.setStartTime(ZonedDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME));
                        break;
                    case "ack-wait":
                        consumerConfiguration.setAckWait(Duration.parse(value));
                        break;
                    case "idle-heartbeat":
                        consumerConfiguration.setIdleHeartbeat(Duration.parse(value));
                        break;
                    case "max-expires":
                        consumerConfiguration.setMaxExpires(Duration.parse(value));
                        break;
                    case "inactive-threshold":
                        consumerConfiguration.setInactiveThreshold(Duration.parse(value));
                        break;
                    case "start-seq":
                        consumerConfiguration.setStartSeq(Long.valueOf(value));
                        break;
                    case "max-deliver":
                        consumerConfiguration.setMaxDeliver(Long.valueOf(value));
                        break;
                    case "rate-limit":
                        consumerConfiguration.setRateLimit(Long.valueOf(value));
                        break;
                    case "max-ack-pending":
                        consumerConfiguration.setMaxAckPending(Long.valueOf(value));
                        break;
                    case "max-pull-waiting":
                        consumerConfiguration.setMaxPullWaiting(Long.valueOf(value));
                        break;
                    case "max-batch":
                        consumerConfiguration.setMaxBatch(Long.valueOf(value));
                        break;
                    case "max-bytes":
                        consumerConfiguration.setMaxBytes(Long.valueOf(value));
                        break;
                    case "num-replicas":
                        consumerConfiguration.setNumReplicas(Integer.valueOf(value));
                        break;
                    case "flow-control":
                        consumerConfiguration.setFlowControl(Boolean.valueOf(value));
                        break;
                    case "headers-only":
                        consumerConfiguration.setHeadersOnly(Boolean.valueOf(value));
                        break;
                    case "mem-storage":
                        consumerConfiguration.setMemStorage(Boolean.valueOf(value));
                        break;
                    case "backoff":
                        List<Duration> backoffList = Arrays.stream(value.split(","))
                                .map(String::trim)
                                .map(Duration::parse)
                                .collect(Collectors.toList());
                        consumerConfiguration.setBackoff(backoffList);
                        break;
                    default:
                        break;
                }
            }
            return Optional.of(buildConsumerConfiguration(consumerConfiguration));
        }
    }

    private ConsumerConfiguration unbuildConsumerConfiguration(io.nats.client.api.ConsumerConfiguration cc) {
        if (cc == null) {
            return null;
        }
        ConsumerConfiguration consumerConfiguration = new ConsumerConfiguration();
        consumerConfiguration.setName(cc.getDurable());
        consumerConfiguration.setAckPolicy(cc.getAckPolicy());
        consumerConfiguration.setAckWait(cc.getAckWait());
        consumerConfiguration.setBackoff(cc.getBackoff());
        consumerConfiguration.setDeliverGroup(cc.getDeliverGroup());
        consumerConfiguration.setDescription(cc.getDescription());
        consumerConfiguration.setDeliverPolicy(cc.getDeliverPolicy());
        consumerConfiguration.setDeliverSubject(cc.getDeliverSubject());
        consumerConfiguration.setFilterSubject(cc.getFilterSubject());
        consumerConfiguration.setFlowControl(cc.isFlowControl());
        consumerConfiguration.setHeadersOnly(cc.isHeadersOnly());
        consumerConfiguration.setMemStorage(cc.isMemStorage());
        consumerConfiguration.setIdleHeartbeat(cc.getIdleHeartbeat());
        consumerConfiguration.setInactiveThreshold(cc.getInactiveThreshold());
        consumerConfiguration.setMaxAckPending((long) cc.getMaxAckPending());
        consumerConfiguration.setMaxBatch((long) cc.getMaxBatch());
        consumerConfiguration.setMaxBytes((long) cc.getMaxBytes());
        consumerConfiguration.setNumReplicas(cc.getNumReplicas());
        consumerConfiguration.setMaxDeliver((long) cc.getMaxDeliver());
        consumerConfiguration.setMaxExpires(cc.getMaxExpires());
        consumerConfiguration.setMaxPullWaiting((long) cc.getMaxPullWaiting());
        consumerConfiguration.setRateLimit(cc.getRateLimit());
        consumerConfiguration.setReplayPolicy(cc.getReplayPolicy());
        consumerConfiguration.setSampleFrequency(cc.getSampleFrequency());
        consumerConfiguration.setStartSeq(cc.getStartSequence());
        consumerConfiguration.setStartTime(cc.getStartTime());
        return consumerConfiguration;
    }

    private io.nats.client.api.ConsumerConfiguration buildConsumerConfiguration(ConsumerConfiguration consumerConfiguration) {
        io.nats.client.api.ConsumerConfiguration.Builder builder = io.nats.client.api.ConsumerConfiguration.builder();
        if (consumerConfiguration.getDeliverPolicy() != null) {
            builder.deliverPolicy(consumerConfiguration.getDeliverPolicy());
        }
        if (consumerConfiguration.getAckPolicy() != null) {
            builder.ackPolicy(consumerConfiguration.getAckPolicy());
        }
        if (consumerConfiguration.getReplayPolicy() != null) {
            builder.replayPolicy(consumerConfiguration.getReplayPolicy());
        }
        if (consumerConfiguration.getDescription() != null) {
            builder.description(consumerConfiguration.getDescription());
        }
        if (consumerConfiguration.getName() != null) {
            builder.durable(consumerConfiguration.getName());
        }
        if (consumerConfiguration.getDeliverSubject() != null) {
            builder.deliverSubject(consumerConfiguration.getDeliverSubject());
        }
        if (consumerConfiguration.getDeliverGroup() != null) {
            builder.deliverGroup(consumerConfiguration.getDeliverGroup());
        }
        if (consumerConfiguration.getFilterSubject() != null) {
            builder.filterSubject(consumerConfiguration.getFilterSubject());
        }
        if (consumerConfiguration.getSampleFrequency() != null) {
            builder.sampleFrequency(consumerConfiguration.getSampleFrequency());
        }
        if (consumerConfiguration.getStartTime() != null) {
            builder.startTime(consumerConfiguration.getStartTime());
        }
        if (consumerConfiguration.getAckWait() != null) {
            builder.ackWait(consumerConfiguration.getAckWait());
        }
        if (consumerConfiguration.getIdleHeartbeat() != null) {
            builder.idleHeartbeat(consumerConfiguration.getIdleHeartbeat());
        }
        if (consumerConfiguration.getMaxExpires() != null) {
            builder.maxExpires(consumerConfiguration.getMaxExpires());
        }
        if (consumerConfiguration.getInactiveThreshold() != null) {
            builder.inactiveThreshold(consumerConfiguration.getInactiveThreshold());
        }
        if (consumerConfiguration.getStartSeq() != null) {
            builder.startSequence(consumerConfiguration.getStartSeq());
        }
        if (consumerConfiguration.getMaxDeliver() != null) {
            builder.maxDeliver(consumerConfiguration.getMaxDeliver());
        }
        if (consumerConfiguration.getRateLimit() != null) {
            builder.rateLimit(consumerConfiguration.getRateLimit());
        }
        if (consumerConfiguration.getMaxAckPending() != null) {
            builder.maxAckPending(consumerConfiguration.getMaxAckPending());
        }
        if (consumerConfiguration.getMaxPullWaiting() != null) {
            builder.maxPullWaiting(consumerConfiguration.getMaxPullWaiting());
        }
        if (consumerConfiguration.getMaxBatch() != null) {
            builder.maxBatch(consumerConfiguration.getMaxBatch());
        }
        if (consumerConfiguration.getMaxBytes() != null) {
            builder.maxBytes(consumerConfiguration.getMaxBytes());
        }
        if (consumerConfiguration.getNumReplicas() != null) {
            builder.numReplicas(consumerConfiguration.getNumReplicas());
        }
        if (consumerConfiguration.getFlowControl() == Boolean.TRUE) {
            builder.flowControl(consumerConfiguration.getIdleHeartbeat());
        }
        if (consumerConfiguration.getHeadersOnly() != null) {
            builder.headersOnly(consumerConfiguration.getHeadersOnly());
        }
        if (consumerConfiguration.getMemStorage() != null) {
            builder.memStorage(consumerConfiguration.getMemStorage());
        }
        if (consumerConfiguration.getBackoff() != null) {
            builder.backoff(consumerConfiguration.getBackoff().toArray(new Duration[0]));
        }
        return builder.build();
    }
}
