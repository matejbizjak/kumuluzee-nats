package com.kumuluz.ee.nats.common.connection.config;

import com.kumuluz.ee.nats.common.exception.ConfigurationException;
import io.nats.client.JetStreamOptions;
import io.nats.client.Nats;
import io.nats.client.Options;

import javax.net.ssl.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.nats.client.Options.*;

/**
 * Class for NATS Connection settings.
 *
 * @author Matej Bizjak
 */

public abstract class ConnectionConfig {

    private final String name;
    private List<String> addresses = Collections.singletonList(DEFAULT_URL);
    private int maxReconnect = DEFAULT_MAX_RECONNECT;
    private Duration reconnectWait = DEFAULT_RECONNECT_WAIT;
    private Duration connectionTimeout = Options.DEFAULT_CONNECTION_TIMEOUT;
    private Duration pingInterval = DEFAULT_PING_INTERVAL;
    private long reconnectBufferSize = DEFAULT_RECONNECT_BUF_SIZE;
    private String inboxPrefix = DEFAULT_INBOX_PREFIX;
    private boolean noEcho;
    private String username;
    private String password;
    private String credentials;
    private TLS tls;
    private List<StreamConsumerConfiguration> streamConsumerConfigurations;
    private Map<String, JetStreamOptions> jetStreamContextOptions;

    public ConnectionConfig(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = addresses;
    }

    public int getMaxReconnect() {
        return maxReconnect;
    }

    public void setMaxReconnect(int maxReconnect) {
        this.maxReconnect = maxReconnect;
    }

    public Duration getReconnectWait() {
        return reconnectWait;
    }

    public void setReconnectWait(Duration reconnectWait) {
        this.reconnectWait = reconnectWait;
    }

    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Duration getPingInterval() {
        return pingInterval;
    }

    public void setPingInterval(Duration pingInterval) {
        this.pingInterval = pingInterval;
    }

    public long getReconnectBufferSize() {
        return reconnectBufferSize;
    }

    public void setReconnectBufferSize(long reconnectBufferSize) {
        this.reconnectBufferSize = reconnectBufferSize;
    }

    public String getInboxPrefix() {
        return inboxPrefix;
    }

    public void setInboxPrefix(String inboxPrefix) {
        this.inboxPrefix = inboxPrefix;
    }

    public boolean isNoEcho() {
        return noEcho;
    }

    public void setNoEcho(boolean noEcho) {
        this.noEcho = noEcho;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCredentials() {
        return credentials;
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
    }

    public TLS getTls() {
        return tls;
    }

    public void setTls(TLS tls) {
        this.tls = tls;
    }

    public List<StreamConsumerConfiguration> getStreamConsumerConfigurations() {
        return streamConsumerConfigurations;
    }

    public StreamConsumerConfiguration getStreamConsumerConfiguration(String streamName) {
        return streamConsumerConfigurations.stream().filter(x -> x.getStreamName().equals(streamName)).findFirst().orElse(null);
    }

    public void setStreamConsumerConfigurations(List<StreamConsumerConfiguration> streamConsumerConfigurations) {
        this.streamConsumerConfigurations = streamConsumerConfigurations;
    }

    public Map<String, JetStreamOptions> getJetStreamContextOptions() {
        return jetStreamContextOptions;
    }

    public void setJetStreamContextOptions(Map<String, JetStreamOptions> jetStreamContextOptions) {
        this.jetStreamContextOptions = jetStreamContextOptions;
    }

    /**
     * @return NATS options builder based on this set of properties
     * @throws IOException              if there is a problem reading a file or setting up the SSL context
     * @throws GeneralSecurityException if there is a problem setting up the SSL context
     */
    public Builder toOptionsBuilder() throws Exception {
        Builder builder = new Builder();

        builder = builder.servers(this.addresses.toArray(new String[0]));
        builder = builder.maxReconnects(this.maxReconnect);
        builder = builder.reconnectWait(this.reconnectWait);
        builder = builder.connectionTimeout(this.connectionTimeout);
        builder = builder.connectionName(this.name);
        builder = builder.pingInterval(this.pingInterval);
        builder = builder.reconnectBufferSize(this.reconnectBufferSize);
        builder = builder.inboxPrefix(this.inboxPrefix);

        if (this.noEcho) {
            builder = builder.noEcho();
        }

        if (this.credentials != null && !this.credentials.isEmpty()) {
            builder = builder.authHandler(Nats.credentials(this.credentials));
        } else if (this.username != null && !this.username.isEmpty()) {
            builder = builder.userInfo(this.username, this.password);
        }

        if (this.tls != null) {
            builder.sslContext(this.tls.createTlsContext());
        }

        return builder;
    }

    /**
     * TLS Configuration.
     */
    public static class TLS {

        private String trustStorePath;

        private String trustStorePassword;

        private String trustStoreType;

        private String certificatePath;

        private String keyStorePath;

        private String keyStorePassword;

        private String keyStoreType;

        public String getTrustStorePath() {
            return trustStorePath;
        }

        public void setTrustStorePath(String trustStorePath) {
            this.trustStorePath = trustStorePath;
        }

        public String getTrustStorePassword() {
            return trustStorePassword;
        }

        public void setTrustStorePassword(String trustStorePassword) {
            this.trustStorePassword = trustStorePassword;
        }

        public String getTrustStoreType() {
            return trustStoreType;
        }

        public void setTrustStoreType(String trustStoreType) {
            this.trustStoreType = trustStoreType;
        }

        public String getCertificatePath() {
            return certificatePath;
        }

        public void setCertificatePath(String certificatePath) {
            this.certificatePath = certificatePath;
        }

        public String getKeyStorePath() {
            return keyStorePath;
        }

        public void setKeyStorePath(String keyStorePath) {
            this.keyStorePath = keyStorePath;
        }

        public String getKeyStorePassword() {
            return keyStorePassword;
        }

        public void setKeyStorePassword(String keyStorePassword) {
            this.keyStorePassword = keyStorePassword;
        }

        public String getKeyStoreType() {
            return keyStoreType;
        }

        public void setKeyStoreType(String keyStoreType) {
            this.keyStoreType = keyStoreType;
        }

        private SSLContext createTlsContext() throws Exception {
            SSLContext ctx = SSLContext.getInstance(DEFAULT_SSL_PROTOCOL);
            ctx.init(createKeyManagers(), createTrustManagers(), new SecureRandom());
            return ctx;
        }

        private KeyManager[] createKeyManagers() throws Exception {
            KeyStore store = loadStore(keyStorePath, keyStorePassword);
            KeyManagerFactory factory = KeyManagerFactory.getInstance(Optional.ofNullable(keyStoreType).orElse("SunX509"));
            factory.init(store, Optional.ofNullable(keyStorePassword).map(String::toCharArray).orElse(new char[0]));
            return factory.getKeyManagers();
        }

        private TrustManager[] createTrustManagers() throws Exception {
            KeyStore store = loadStore(trustStorePath, trustStorePassword);
            TrustManagerFactory factory = TrustManagerFactory.getInstance(Optional.ofNullable(trustStoreType).orElse("SunX509"));
            factory.init(store);
            return factory.getTrustManagers();
        }

        private KeyStore loadStore(String path, String password) throws Exception {
            KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
            if (path != null && !path.isEmpty()) {
                BufferedInputStream in = null;
                try {
                    // full path
                    in = new BufferedInputStream(Files.newInputStream(Paths.get(path)));
                } catch (IOException e) {
                    // resources
                    URL resource = getClass().getClassLoader().getResource(path);
                    if (resource != null) {
                        in = new BufferedInputStream(resource.openStream());
                    } else {
                        throw new ConfigurationException(String.format("Keystore or truststore wasn't found at path %s, please check the configuration.", path));
                    }
                }
                store.load(in, Optional.ofNullable(password).map(String::toCharArray).orElse(new char[0]));
            } else {
                store.load(null);
            }
            return store;
        }
    }
}
