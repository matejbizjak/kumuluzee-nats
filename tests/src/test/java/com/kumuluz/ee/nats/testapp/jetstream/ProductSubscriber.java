package com.kumuluz.ee.nats.testapp.jetstream;

import com.kumuluz.ee.nats.common.annotations.ConfigurationOverride;
import com.kumuluz.ee.nats.common.annotations.ConsumerConfig;
import com.kumuluz.ee.nats.common.util.SerDes;
import com.kumuluz.ee.nats.jetstream.annotations.JetStreamSubscriber;
import com.kumuluz.ee.nats.testapp.common.Product;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * @author Matej Bizjak
 */

@ApplicationScoped
public class ProductSubscriber {

    @Inject
    @JetStreamSubscriber(connection = "secure", subject = "product.corn", durable = "newCorn")
    @ConsumerConfig(name = "custom1", configOverrides = {@ConfigurationOverride(key = "deliver-policy", value = "new")})
    private JetStreamSubscription jetStreamSubscription;

    public void pullCorn() {
        if (jetStreamSubscription != null) {
            List<Message> messages = jetStreamSubscription.fetch(3, Duration.ofSeconds(1));
            for (Message message : messages) {
                try {
                    Product corn = SerDes.deserialize(message.getData(), Product.class);
                    message.ack();
                } catch (IOException e) {
                    message.nak();
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
