package com.kumuluz.ee.nats.jetstream.consumer.subscriber;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.kumuluz.ee.nats.common.annotations.ConsumerConfig;
import com.kumuluz.ee.nats.common.connection.config.ConfigLoader;
import com.kumuluz.ee.nats.common.connection.config.GeneralConfig;
import com.kumuluz.ee.nats.jetstream.JetStreamExtension;
import com.kumuluz.ee.nats.jetstream.annotations.JetStreamSubscriber;
import com.kumuluz.ee.nats.jetstream.context.ContextFactory;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamSubscription;
import io.nats.client.PullSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;

import java.io.IOException;
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

    private static final Table<JetStream, String, JetStreamSubscription> subscriptions = HashBasedTable.create();

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
        if (jetStreamSubscriberAnnotation.durable().equals("")) {
            LOG.severe(String.format("Durable must be set for pull based subscriptions. Cannot create a JetStream subscription for a connection %s context %s and subject %s."
                    , jetStreamSubscriberAnnotation.connection(), jetStreamSubscriberAnnotation.context(), jetStreamSubscriberAnnotation.subject()));
            return null;
        }
        GeneralConfig generalConfig = ConfigLoader.getInstance().getGeneralConfig();
        ConsumerConfiguration consumerConfiguration;
        if (consumerConfigAnnotation == null) {
            consumerConfiguration = generalConfig.combineConsumerConfigAndBuild(null, null);
        } else {
            consumerConfiguration = generalConfig.combineConsumerConfigAndBuild(consumerConfigAnnotation.name(), consumerConfigAnnotation.configOverrides());
        }
        PullSubscribeOptions pullSubscribeOptions = PullSubscribeOptions
                .builder()
                .stream(jetStreamSubscriberAnnotation.stream())
                .configuration(consumerConfiguration)
                .durable(jetStreamSubscriberAnnotation.durable())
                .bind(jetStreamSubscriberAnnotation.bind())
                .build();
//        StreamManagement.addOrUpdateConsumer(jetStreamSubscriberAnnotation.connection(), jetStreamSubscriberAnnotation.stream(), consumerConfiguration);
        try {
            jetStreamSubscription = jetStream.subscribe(jetStreamSubscriberAnnotation.subject(), pullSubscribeOptions);
            LOG.info(String.format("JetStream subscription for a connection %s context %s and subject %s was created successfully."
                    , jetStreamSubscriberAnnotation.connection(), jetStreamSubscriberAnnotation.context(), jetStreamSubscriberAnnotation.subject()));
        } catch (IOException | JetStreamApiException e) {
            LOG.log(Level.SEVERE, String.format("Cannot create a JetStream subscription for a connection %s context %s and subject %s."
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
        if (!subscriptions.contains(jetStream, jetStreamSubscriberAnnotation.subject())) {
            JetStreamSubscription jetStreamSubscription = createSubscription(jetStreamSubscriberAnnotation
                    , consumerConfigAnnotation, jetStream);
            if (jetStreamSubscription != null) {
                subscriptions.put(jetStream, jetStreamSubscriberAnnotation.subject(), jetStreamSubscription);
            }
        }
        return subscriptions.get(jetStream, jetStreamSubscriberAnnotation.subject());
    }
}
