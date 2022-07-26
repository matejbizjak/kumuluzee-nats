package com.kumuluz.ee.nats.tests.beans.listener;

import com.kumuluz.ee.nats.core.annotations.NatsListener;
import com.kumuluz.ee.nats.core.annotations.Subject;
import com.kumuluz.ee.nats.tests.beans.dto.Product;

/**
 * @author Matej Bizjak
 */

@NatsListener
public class ProductListener {

    @Subject(value = "product2")
    public String receiveAndReturnString(Product product) {
        return product.getName().toLowerCase();
    }
}
