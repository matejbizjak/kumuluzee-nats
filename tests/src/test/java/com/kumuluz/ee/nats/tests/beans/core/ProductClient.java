package com.kumuluz.ee.nats.tests.beans.core;

import com.kumuluz.ee.nats.core.annotations.RegisterNatsClient;
import com.kumuluz.ee.nats.core.annotations.Subject;
import com.kumuluz.ee.nats.tests.beans.common.Product;

/**
 * @author Matej Bizjak
 */

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
