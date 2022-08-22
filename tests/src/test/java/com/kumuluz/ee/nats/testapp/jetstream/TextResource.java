package com.kumuluz.ee.nats.testapp.jetstream;

import com.kumuluz.ee.nats.common.util.SerDes;
import com.kumuluz.ee.nats.jetstream.annotations.JetStreamProducer;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.Message;
import io.nats.client.api.PublishAck;
import io.nats.client.impl.Headers;
import io.nats.client.impl.NatsMessage;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author Matej Bizjak
 */

@Path("/text/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class TextResource {

    @Inject
    private TextSubscriber textSubscriber;

    @Inject
    @JetStreamProducer(context = "context1")
    private JetStream jetStream;

    @POST
    @Path("/uniqueSync/{subject}")
    public Response postSub1(@PathParam("subject") String subject, String messageText) {
        if (jetStream == null) {
            return Response.serverError().build();
        }
        try {
            String uniqueID = UUID.randomUUID().toString();
            Headers headers = new Headers().add("Nats-Msg-Id", uniqueID);

            Message message = NatsMessage.builder()
                    .subject(subject)
                    .data(SerDes.serialize(messageText))
                    .headers(headers)
                    .build();

            PublishAck publishAck = jetStream.publish(message);
            return Response.ok(String.format("Message has been sent to subject %s in stream %s.", message.getSubject()
                    , publishAck.getStream())).build();
        } catch (IOException | JetStreamApiException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e).build();
        }
    }

    @POST
    @Path("/async/{subject}")
    public Response postSub2(@PathParam("subject") String subject, String messageText) {
        if (jetStream == null) {
            return Response.serverError().build();
        }
        try {
            Message message = NatsMessage.builder()
                    .subject(subject)
                    .data(SerDes.serialize(messageText))
                    .build();

            CompletableFuture<PublishAck> futureAck = jetStream.publishAsync(message);
            return Response.ok(String.format("Message has been sent to subject %s in stream %s.", message.getSubject()
                    , futureAck.get().getStream())).build();
        } catch (IOException | ExecutionException | InterruptedException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e).build();
        }
    }

    @GET
    @Path("/pull")
    public Response pullText() {
        String message = textSubscriber.pullMsg();
        return Response.ok(message).build();
    }
}
