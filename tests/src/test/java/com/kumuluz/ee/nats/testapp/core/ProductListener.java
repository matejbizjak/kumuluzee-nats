package com.kumuluz.ee.nats.testapp.core;

import com.kumuluz.ee.nats.core.annotations.NatsListener;
import com.kumuluz.ee.nats.core.annotations.Subject;
import com.kumuluz.ee.nats.testapp.common.Product;

/**
 * NATS Core listener which is listening for new NATS Core messages and returns a response.
 *
 * @author Matej Bizjak
 */

@NatsListener
public class ProductListener {

    @Subject(value = "product3", connection = "secure")
    public Product receiveAndReturnProduct(Product product) {
        product.setName(product.getName().toUpperCase());
        return product;
    }
}
