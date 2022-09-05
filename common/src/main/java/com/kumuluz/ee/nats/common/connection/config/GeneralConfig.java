package com.kumuluz.ee.nats.common.connection.config;

import java.time.Duration;

/**
 * @author Matej Bizjak
 */

public class GeneralConfig {

    public static class Builder {
        private Duration responseTimeout = Duration.ofSeconds(5);
        private Duration ackConfirmationTimeout = Duration.ofSeconds(5);
        private int ackConfirmationRetries = 5;
        private Duration drainTimeout = Duration.ofSeconds(10);

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

        public GeneralConfig build() {
            GeneralConfig generalConfig = new GeneralConfig();
            generalConfig.responseTimeout = responseTimeout;
            generalConfig.ackConfirmationTimeout = ackConfirmationTimeout;
            generalConfig.ackConfirmationRetries = ackConfirmationRetries;
            generalConfig.drainTimeout = drainTimeout;
            return generalConfig;
        }
    }

    private Duration responseTimeout;
    private Duration ackConfirmationTimeout;
    private int ackConfirmationRetries;
    private Duration drainTimeout;

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

    public static Builder builder() {
        return new Builder();
    }
}
