package com.kumuluz.ee.nats.jetstream.consumer.subscriber;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.kumuluz.ee.nats.jetstream.NatsJetStreamExtension;
import com.kumuluz.ee.nats.jetstream.annotations.JetStreamSubscriber;
import com.kumuluz.ee.nats.jetstream.context.JetStreamContextFactory;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamSubscription;
import io.nats.client.PullSubscribeOptions;

import java.io.IOException;
import java.util.logging.Logger;

/**
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

    private JetStreamSubscription createSubscription(JetStreamSubscriber annotation, JetStream jetStream) {
        JetStreamSubscription jetStreamSubscription = null;
        if (annotation.durable().equals("")) {
            LOG.severe("Durable must not be empty for a subscription");
        }
        PullSubscribeOptions pullSubscribeOptions = PullSubscribeOptions
                .builder()
                .durable(annotation.durable())
                .build();
        try {
            jetStreamSubscription = jetStream.subscribe(annotation.subject(), pullSubscribeOptions);
            LOG.info(String.format("JetStream subscription for a connection %s context %s and subject %s was created successfully"
                    , annotation.connection(), annotation.context(), annotation.subject()));
        } catch (IOException | JetStreamApiException e) {
            LOG.severe(String.format("Cannot create a JetStream subscription for a connection %s context %s and subject %s"
                    , annotation.connection(), annotation.context(), annotation.subject()));
        }
        return jetStreamSubscription;
    }

    public JetStreamSubscription getSubscription(JetStreamSubscriber annotation) {
        if (!NatsJetStreamExtension.isExtensionEnabled()) {
            return null;
        }

        JetStream jetStream = JetStreamContextFactory.getInstance().getContext(annotation.connection(), annotation.context());
        if (!subscriptions.contains(jetStream, annotation.subject())) {
            JetStreamSubscription jetStreamSubscription = createSubscription(annotation, jetStream);
            subscriptions.put(jetStream, annotation.subject(), jetStreamSubscription);
        }
        return subscriptions.get(jetStream, annotation.subject());
    }

//    public Map<String, JetStreamSubscription> getSubscriptions(JetStreamSubscriberManager annotation) {
//        if (!NatsJetStreamExtension.isExtensionEnabled()) {
//            return null;
//        }
//
//        JetStream jetStream = JetStreamContextFactory.getInstance().getContext(annotation.connection(), annotation.context());
//        if (!subscriptions.row(jetStream)) {
//            JetStreamSubscription jetStreamSubscription = createSubscription(annotation, jetStream);
//            subscriptions.put(jetStream, annotation.subject(), jetStreamSubscription);
//        }
//        return subscriptions.get(jetStream, annotation.subject());
//    }
}
