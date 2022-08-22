package com.kumuluz.ee.nats.testapp.jetstream;


import com.kumuluz.ee.nats.common.annotations.ConfigurationOverride;
import com.kumuluz.ee.nats.common.annotations.ConsumerConfig;
import com.kumuluz.ee.nats.jetstream.annotations.JetStreamListener;

/**
 * @author Matej Bizjak
 */

public class TextListener {


    @JetStreamListener(context = "context1", subject = "subject1")
    @ConsumerConfig(name = "custom1", configOverrides = {@ConfigurationOverride(key = "deliver-policy", value = "new")})
    public void receive(String value) {
    }
}
