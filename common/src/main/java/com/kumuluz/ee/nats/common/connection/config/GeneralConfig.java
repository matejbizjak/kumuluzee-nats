package com.kumuluz.ee.nats.common.connection.config;

import com.kumuluz.ee.nats.common.annotations.ConfigurationOverride;
import io.nats.client.api.AckPolicy;
import io.nats.client.api.DeliverPolicy;
import io.nats.client.api.ReplayPolicy;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Matej Bizjak
 */

public class GeneralConfig {

    public static class Builder {
        private Duration responseTimeout = Duration.ofSeconds(5);
        private Duration ackConfirmationTimeout = Duration.ofSeconds(5);
        private int ackConfirmationRetries = 5;
        private Duration drainTimeout = Duration.ofSeconds(10);
        private List<ConsumerConfiguration> consumerConfigurations = new ArrayList<>();

        public Builder responseTimeout(Duration responseTimeout) {
            this.responseTimeout = responseTimeout;
            return this;
        }

        public Builder ackConfirmationTimeout(Duration ackConfirmationTimeout) {
            this.ackConfirmationTimeout = ackConfirmationTimeout;
            return this;
        }

        public Builder ackConfirmationRetries(int ackConfirmationRetries) {
            this.ackConfirmationRetries = ackConfirmationRetries;
            return this;
        }

        public Builder drainTimeout(Duration drainTimeout) {
            this.drainTimeout = drainTimeout;
            return this;
        }

        public Builder consumerConfigurations(List<ConsumerConfiguration> consumerConfigurations) {
            this.consumerConfigurations = consumerConfigurations;
            return this;
        }

        public GeneralConfig build() {
            GeneralConfig generalConfig = new GeneralConfig();
            generalConfig.responseTimeout = responseTimeout;
            generalConfig.ackConfirmationTimeout = ackConfirmationTimeout;
            generalConfig.ackConfirmationRetries = ackConfirmationRetries;
            generalConfig.drainTimeout = drainTimeout;
            generalConfig.consumerConfigurations = consumerConfigurations;
            return generalConfig;
        }
    }

    private Duration responseTimeout;
    private Duration ackConfirmationTimeout;
    private int ackConfirmationRetries;
    private Duration drainTimeout;
    private List<ConsumerConfiguration> consumerConfigurations;

    public GeneralConfig() {
    }

    public Duration getResponseTimeout() {
        return responseTimeout;
    }

    public Duration getAckConfirmationTimeout() {
        return ackConfirmationTimeout;
    }

    public int getAckConfirmationRetries() {
        return ackConfirmationRetries;
    }

    public Duration getDrainTimeout() {
        return drainTimeout;
    }

    public ConsumerConfiguration getConsumerConfiguration(String name) {
        return consumerConfigurations.stream()
                .filter(x -> Objects.equals(x.getName(), name))
                .findFirst()
                .orElse(null);
    }

    public io.nats.client.api.ConsumerConfiguration combineConsumerConfigAndBuild(String name, ConfigurationOverride[] overrides) {
        if (name != null && getConsumerConfiguration(name) != null) {
            ConsumerConfiguration consumerConfiguration = getConsumerConfiguration(name);
            for (ConfigurationOverride override : overrides) {
                String key = override.key();
                String value = override.value();

                switch (key) {
                    case "deliverPolicy":
                        consumerConfiguration.setDeliverPolicy(DeliverPolicy.get(value));
                        break;
                    case "ackPolicy":
                        consumerConfiguration.setAckPolicy(AckPolicy.get(value));
                        break;
                    case "replayPolicy":
                        consumerConfiguration.setReplayPolicy(ReplayPolicy.get(value));
                        break;
                    case "description":
                        consumerConfiguration.setDescription(value);
                        break;
                    case "durable":
                        consumerConfiguration.setDurable(value);
                        break;
                    case "deliverSubject":
                        consumerConfiguration.setDeliverSubject(value);
                        break;
                    case "deliverGroup":
                        consumerConfiguration.setDeliverGroup(value);
                        break;
                    case "filterSubject":
                        consumerConfiguration.setFilterSubject(value);
                        break;
                    case "sampleFrequency":
                        consumerConfiguration.setSampleFrequency(value);
                        break;
                    case "startTime":
                        consumerConfiguration.setStartTime(ZonedDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME));
                        break;
                    case "ackWait":
                        consumerConfiguration.setAckWait(Duration.parse(value));
                        break;
                    case "idleHeartbeat":
                        consumerConfiguration.setIdleHeartbeat(Duration.parse(value));
                        break;
                    case "maxExpires":
                        consumerConfiguration.setMaxExpires(Duration.parse(value));
                        break;
                    case "inactiveThreshold":
                        consumerConfiguration.setInactiveThreshold(Duration.parse(value));
                        break;
                    case "startSeq":
                        consumerConfiguration.setStartSeq(Long.valueOf(value));
                        break;
                    case "maxDeliver":
                        consumerConfiguration.setMaxDeliver(Long.valueOf(value));
                        break;
                    case "rateLimit":
                        consumerConfiguration.setRateLimit(Long.valueOf(value));
                        break;
                    case "maxAckPending":
                        consumerConfiguration.setMaxAckPending(Long.valueOf(value));
                        break;
                    case "maxPullWaiting":
                        consumerConfiguration.setMaxPullWaiting(Long.valueOf(value));
                        break;
                    case "maxBatch":
                        consumerConfiguration.setMaxBatch(Long.valueOf(value));
                        break;
                    case "maxBytes":
                        consumerConfiguration.setMaxBytes(Long.valueOf(value));
                        break;
                    case "flowControl":
                        consumerConfiguration.setFlowControl(Boolean.valueOf(value));
                        break;
                    case "headersOnly":
                        consumerConfiguration.setHeadersOnly(Boolean.valueOf(value));
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
            if (consumerConfiguration.getDurable() != null) {
                builder.durable(consumerConfiguration.getDurable());
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
            if (consumerConfiguration.getFlowControl() == Boolean.TRUE) {
                builder.flowControl(consumerConfiguration.getIdleHeartbeat());
            }
            if (consumerConfiguration.getHeadersOnly() != null) {
                builder.headersOnly(consumerConfiguration.getHeadersOnly());
            }
            if (consumerConfiguration.getBackoff() != null) {
                builder.backoff(consumerConfiguration.getBackoff().toArray(new Duration[0]));
            }
            return builder.build();
        }
        return io.nats.client.api.ConsumerConfiguration.builder().build();
    }
}
