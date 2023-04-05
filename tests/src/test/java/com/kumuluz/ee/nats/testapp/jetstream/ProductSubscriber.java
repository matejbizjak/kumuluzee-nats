package com.kumuluz.ee.nats.testapp.jetstream;

import com.kumuluz.ee.nats.common.util.SerDes;
import com.kumuluz.ee.nats.jetstream.annotations.JetStreamSubscriber;
import com.kumuluz.ee.nats.testapp.common.Product;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.time.Duration;

/**
 * JetStream pull subscriber.
 *
 * @author Matej Bizjak
 */

@ApplicationScoped
public class ProductSubscriber {

    @Inject
    @JetStreamSubscriber(connection = "secure", stream = "stream2", subject = "product.corn", durable = "newCorn")
    private JetStreamSubscription jetStreamSubscription;

    public Product pullCorn() {
        Product corn = null;
        if (jetStreamSubscription != null) {
            Message message = jetStreamSubscription.fetch(1, Duration.ofSeconds(1)).get(0);
            try {
                corn = SerDes.deserialize(message.getData(), Product.class);
                message.ack();
            } catch (IOException e) {
                message.nak();
                throw new RuntimeException(e);
            }
        }
        return corn;
    }
}
