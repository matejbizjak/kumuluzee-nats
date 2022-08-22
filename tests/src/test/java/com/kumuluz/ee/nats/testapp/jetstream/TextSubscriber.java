package com.kumuluz.ee.nats.testapp.jetstream;

import com.kumuluz.ee.nats.common.annotations.ConfigurationOverride;
import com.kumuluz.ee.nats.common.annotations.ConsumerConfig;
import com.kumuluz.ee.nats.common.util.SerDes;
import com.kumuluz.ee.nats.jetstream.annotations.JetStreamSubscriber;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.time.Duration;

/**
 * @author Matej Bizjak
 */

@ApplicationScoped
public class TextSubscriber {


    @Inject
    @JetStreamSubscriber(context = "context1", stream = "stream1", subject = "subject2", durable = "somethingNew")
    @ConsumerConfig(name = "custom1", configOverrides = {@ConfigurationOverride(key = "deliver-policy", value = "new")})
    private JetStreamSubscription jetStreamSubscription;

    public String pullMsg() {
        if (jetStreamSubscription != null) {
            Message message = jetStreamSubscription.fetch(1, Duration.ofSeconds(1)).get(0);
            try {
                String value = SerDes.deserialize(message.getData(), String.class);
                message.ack();
                return value;
            } catch (IOException e) {
                message.nak();
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
