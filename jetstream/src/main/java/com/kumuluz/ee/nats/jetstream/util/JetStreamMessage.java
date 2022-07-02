package com.kumuluz.ee.nats.jetstream.util;

import io.nats.client.Message;

/**
 * NATS message facade that enables inProgress() function that resets redelivery timer at the server
 *
 * @author Matej Bizjak
 */

public class JetStreamMessage {

    private final Message message;

    public JetStreamMessage(Message message) {
        this.message = message;
    }

    /**
     * Indicates that this message is being worked on and reset redelivery timer in the server.
     */
    public void inProgress() {
        message.inProgress();
    }
}
