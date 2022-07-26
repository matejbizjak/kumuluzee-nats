package com.kumuluz.ee.nats.tests.beans.client;

import com.kumuluz.ee.nats.core.annotations.RegisterNatsClient;
import com.kumuluz.ee.nats.core.annotations.Subject;
import com.kumuluz.ee.nats.tests.beans.dto.Product;

/**
 * @author Matej Bizjak
 */

@RegisterNatsClient
public interface ProductClient {

    @Subject(value = "product1")
    void sendProduct(Product product);

    @Subject(value = "product2")
    String sendProductResponse(Product product);

    @Subject(value = "product3", connection = "secure")
    Product sendProductResponseProduct(Product product);
}
