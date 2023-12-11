package com.kumuluz.ee.nats.testapp.jetstream;

import com.kumuluz.ee.nats.jetstream.annotations.JetStreamSubject;
import com.kumuluz.ee.nats.jetstream.annotations.RegisterJetStreamClient;
import com.kumuluz.ee.nats.testapp.common.Product;
import io.nats.client.api.PublishAck;

@RegisterJetStreamClient(connection = "secure")
public interface ProductClient {

    PublishAck sendAnyProduct(@JetStreamSubject String productSubject, Product product);

}
