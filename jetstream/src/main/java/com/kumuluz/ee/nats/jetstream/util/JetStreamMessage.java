package com.kumuluz.ee.nats.jetstream.util;

import io.nats.client.Message;
import io.nats.client.impl.AckType;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsJetStreamMetaData;
import io.nats.client.support.Status;

/**
 * NATS message facade that provides metadata and enables inProgress() function that resets redelivery timer at the server.
 *
 * @author Matej Bizjak
 */

public class JetStreamMessage {  // new

    private final Message message;

    public JetStreamMessage(Message message) {
        this.message = message;
    }

    /**
     * @return the subject that this message was sent to
     */
    public String getSubject() {
        return message.getSubject();
    }


    /**
     * @return true if there are headers
     */
    public boolean hasHeaders() {
        return message.hasHeaders();
    }

    /**
     * @return the headers object the message
     */
    public Headers getHeaders() {
        return message.getHeaders();
    }

    /**
     * @return true if there is status
     */
    public boolean isStatusMessage() {
        return message.isStatusMessage();
    }

    /**
     * @return the status object message
     */
    public Status getStatus() {
        return message.getStatus();
    }


    /**
     * Gets the metadata associated with a JetStream message.
     *
     * @return metadata or null if the message is not a JetStream message.
     */
    public NatsJetStreamMetaData metaData() {
        return message.metaData();
    }

    /**
     * the last ack that was done with this message
     *
     * @return the last ack or null
     */
    public AckType lastAck() {
        return message.lastAck();
    }

    /**
     * Indicates that this message is being worked on and resets redelivery timer at the server.
     */
    public void inProgress() {
        message.inProgress();
    }
}
