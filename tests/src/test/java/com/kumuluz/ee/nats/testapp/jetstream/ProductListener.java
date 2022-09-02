package com.kumuluz.ee.nats.testapp.jetstream;

import com.kumuluz.ee.nats.jetstream.annotations.JetStreamListener;
import com.kumuluz.ee.nats.jetstream.wrappers.JetStreamMessage;
import com.kumuluz.ee.nats.testapp.common.Product;

/**
 * @author Matej Bizjak
 */

public class ProductListener {


    @JetStreamListener(connection = "secure", subject = "product.*")
//    @ConsumerConfig(name = "custom1", configOverrides = {@ConfigurationOverride(key = "deliver-policy", value = "new")})
    public void receive(Product product, JetStreamMessage msg) {
    }
}
