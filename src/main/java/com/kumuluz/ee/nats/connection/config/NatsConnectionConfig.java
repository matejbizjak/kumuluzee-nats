package com.kumuluz.ee.nats.connection.config;

import io.nats.client.Nats;
import io.nats.client.Options;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.nats.client.Options.*;

/**
 * @author Matej Bizjak
 */

public abstract class NatsConnectionConfig {

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

    public NatsConnectionConfig(String name) {
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

    /**
     * @return NATS options builder based on this set of properties
     * @throws IOException if there is a problem reading a file or setting up the SSL context
     * @throws GeneralSecurityException if there is a problem setting up the SSL context
     */
    public Builder toOptionsBuilder() throws IOException, GeneralSecurityException {
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

        private SSLContext createTlsContext() throws IOException, GeneralSecurityException {
            SSLContext ctx = SSLContext.getInstance(DEFAULT_SSL_PROTOCOL);

            TrustManagerFactory factory =
                    TrustManagerFactory.getInstance(Optional.ofNullable(trustStoreType).orElse("SunX509"));
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            if (trustStorePath != null && !trustStorePath.isEmpty()) {
                try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(trustStorePath))) {
                    ks.load(in, Optional.ofNullable(trustStorePassword).map(String::toCharArray).orElse(new char[0]));
                }
            } else {
                ks.load(null);
            }
            if (certificatePath != null && !certificatePath.isEmpty()) {
                try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(certificatePath))) {
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    X509Certificate cert = (X509Certificate) cf.generateCertificate(in);
                    ks.setCertificateEntry("nats", cert);
                }
            }
            factory.init(ks);
            ctx.init(null, factory.getTrustManagers(), new SecureRandom());

            return ctx;
        }
    }
}
