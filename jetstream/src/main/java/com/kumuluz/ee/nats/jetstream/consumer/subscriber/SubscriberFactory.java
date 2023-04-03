package com.kumuluz.ee.nats.jetstream.consumer.subscriber;

import com.kumuluz.ee.nats.common.annotations.ConsumerConfig;
import com.kumuluz.ee.nats.common.connection.NatsConnection;
import com.kumuluz.ee.nats.common.connection.config.ConnectionConfig;
import com.kumuluz.ee.nats.common.connection.config.NatsConfigLoader;
import com.kumuluz.ee.nats.common.connection.config.StreamConsumerConfiguration;
import com.kumuluz.ee.nats.common.exception.DefinitionException;
import com.kumuluz.ee.nats.common.management.StreamManagement;
import com.kumuluz.ee.nats.jetstream.JetStreamExtension;
import com.kumuluz.ee.nats.jetstream.annotations.JetStreamSubscriber;
import com.kumuluz.ee.nats.jetstream.context.ContextFactory;
import com.kumuluz.ee.nats.jetstream.util.TwoKeyTable;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamSubscription;
import io.nats.client.PullSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.StreamInfo;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for JetStream subscriptions (pull consumers).
 *
 * @author Matej Bizjak
 */

public class SubscriberFactory {
    private static final Logger LOG = Logger.getLogger(SubscriberFactory.class.getName());

    private static SubscriberFactory instance;

    private static final TwoKeyTable<JetStream, String, JetStreamSubscription> SUBSCRIPTIONS = new TwoKeyTable<>();

    public SubscriberFactory() {
    }

    private static synchronized void init() {
        if (instance == null) {
            instance = new SubscriberFactory();
        }
    }

    public static SubscriberFactory getInstance() {
        if (instance == null) {
            init();
        }
        return instance;
    }

    private JetStreamSubscription createSubscription(JetStreamSubscriber jetStreamSubscriberAnnotation, ConsumerConfig consumerConfigAnnotation, JetStream jetStream) {
        JetStreamSubscription jetStreamSubscription = null;

        //region Validation
        if (jetStreamSubscriberAnnotation.subject().isEmpty()) {
            throw new DefinitionException(String.format("Subject was not specified at JetStream subscription for connection %s context %s."
                    , jetStreamSubscriberAnnotation.connection(), jetStreamSubscriberAnnotation.context()));
        }
        if (jetStreamSubscriberAnnotation.stream().isEmpty()) {
            throw new DefinitionException(String.format("Stream was not specified at JetStream subscription for connection %s context %s and subject %s."
                    , jetStreamSubscriberAnnotation.connection(), jetStreamSubscriberAnnotation.context(), jetStreamSubscriberAnnotation.subject()));
        }
        if (jetStreamSubscriberAnnotation.durable().isEmpty()) {
            throw new DefinitionException(String.format("Durable must be set for pull based subscriptions. Cannot create a JetStream subscription for connection %s context %s and subject %s."
                    , jetStreamSubscriberAnnotation.connection(), jetStreamSubscriberAnnotation.context(), jetStreamSubscriberAnnotation.subject()));
        }
        //endregion

        //region Configuration
        PullSubscribeOptions.Builder builder = PullSubscribeOptions.builder()
                .stream(jetStreamSubscriberAnnotation.stream())
                .durable(jetStreamSubscriberAnnotation.durable())
                .bind(jetStreamSubscriberAnnotation.bind());
        ConnectionConfig connectionConfig = NatsConfigLoader.getInstance().getConfigForConnection(jetStreamSubscriberAnnotation.connection());
        StreamConsumerConfiguration streamConsumerConfiguration = connectionConfig
                .getStreamConsumerConfiguration(jetStreamSubscriberAnnotation.stream());
        if (streamConsumerConfiguration == null) {  // stream not specified in configuration
            StreamInfo streamInfo = null;
            try {
                streamInfo = StreamManagement.getStreamInfoOrNullWhenNotExist(NatsConnection
                        .getConnection(jetStreamSubscriberAnnotation.connection()), jetStreamSubscriberAnnotation.stream());
            } catch (JetStreamApiException | IOException e) {
                LOG.log(Level.SEVERE, String.format("There was a problem obtaining stream info for a connection %s context %s and subject %s."
                        , jetStreamSubscriberAnnotation.connection(), jetStreamSubscriberAnnotation.context(), jetStreamSubscriberAnnotation.subject()), e);
            }
            if (streamInfo == null) {  // check if stream already exists (was created previously)
                throw new DefinitionException(String.format("Stream must be set and valid for for connection %s context %s and subject %s."
                        , jetStreamSubscriberAnnotation.connection(), jetStreamSubscriberAnnotation.context(), jetStreamSubscriberAnnotation.subject()));
            }
        } else {  // stream specified in configuration
            Optional<ConsumerConfiguration> consumerConfiguration = streamConsumerConfiguration
                    .getAndCombineConsumerConfig(NatsConnection.getConnection(jetStreamSubscriberAnnotation.connection())
                            , jetStreamSubscriberAnnotation.durable(), consumerConfigAnnotation);
            consumerConfiguration.ifPresent(builder::configuration);
        }
        PullSubscribeOptions pullSubscribeOptions = builder.build();
        //endregion

        try {
            jetStreamSubscription = jetStream.subscribe(jetStreamSubscriberAnnotation.subject(), pullSubscribeOptions);
            LOG.info(String.format("JetStream subscription for connection %s context %s and subject %s was created successfully."
                    , jetStreamSubscriberAnnotation.connection(), jetStreamSubscriberAnnotation.context(), jetStreamSubscriberAnnotation.subject()));
        } catch (IOException | JetStreamApiException e) {
            LOG.log(Level.SEVERE, String.format("Cannot create JetStream subscription for a connection %s context %s and subject %s."
                    , jetStreamSubscriberAnnotation.connection(), jetStreamSubscriberAnnotation.context(), jetStreamSubscriberAnnotation.subject()), e);
        }
        return jetStreamSubscription;
    }

    public JetStreamSubscription getSubscription(JetStreamSubscriber jetStreamSubscriberAnnotation, ConsumerConfig consumerConfigAnnotation) {
        if (!JetStreamExtension.isExtensionEnabled()) {
            return null;
        }

        JetStream jetStream = ContextFactory.getInstance().getContext(jetStreamSubscriberAnnotation.connection()
                , jetStreamSubscriberAnnotation.context());
        if (jetStream == null) {
            return null;
        }
        if (!SUBSCRIPTIONS.contains(jetStream, jetStreamSubscriberAnnotation.subject())) {
            JetStreamSubscription jetStreamSubscription = createSubscription(jetStreamSubscriberAnnotation
                    , consumerConfigAnnotation, jetStream);
            if (jetStreamSubscription != null) {
                SUBSCRIPTIONS.put(jetStream, jetStreamSubscriberAnnotation.subject(), jetStreamSubscription);
            }
        }
        return SUBSCRIPTIONS.get(jetStream, jetStreamSubscriberAnnotation.subject());
    }
}
