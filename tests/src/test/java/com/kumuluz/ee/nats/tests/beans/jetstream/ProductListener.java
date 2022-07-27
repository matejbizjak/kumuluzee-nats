package com.kumuluz.ee.nats.tests.beans.jetstream;

import com.kumuluz.ee.nats.jetstream.annotations.JetStreamListener;
import com.kumuluz.ee.nats.jetstream.util.JetStreamMessage;
import com.kumuluz.ee.nats.tests.beans.common.Product;

/**
 * @author Matej Bizjak
 */

public class ProductListener {


    @JetStreamListener(connection = "secure", subject = "product.*")
//    @ConsumerConfig(name = "custom1", configOverrides = {@ConfigurationOverride(key = "deliver-policy", value = "new")})
    public void receive(Product product, JetStreamMessage msg) {
    }
}
