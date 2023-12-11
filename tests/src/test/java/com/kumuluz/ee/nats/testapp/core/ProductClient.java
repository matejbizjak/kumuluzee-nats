package com.kumuluz.ee.nats.testapp.core;

import com.kumuluz.ee.nats.core.annotations.RegisterNatsClient;
import com.kumuluz.ee.nats.core.annotations.Subject;
import com.kumuluz.ee.nats.testapp.common.Product;

import java.util.concurrent.CompletableFuture;

/**
 * NATS Core client interface.
 *
 * @author Matej Bizjak
 */

@RegisterNatsClient
public interface ProductClient {

    @Subject(value = "product1")
    void sendProduct(Product product);

    @Subject(value = "product2")
    String sendProductResponse(Product product);

    @Subject(value = "product3", connection = "secure")
    CompletableFuture<Product> sendProductResponseProduct(Product product);
}
