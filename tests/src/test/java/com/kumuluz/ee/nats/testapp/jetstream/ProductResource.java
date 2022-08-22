package com.kumuluz.ee.nats.testapp.jetstream;

import com.kumuluz.ee.nats.common.util.SerDes;
import com.kumuluz.ee.nats.jetstream.annotations.JetStreamProducer;
import com.kumuluz.ee.nats.testapp.common.Product;
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

@Path("/product/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class ProductResource {

    @Inject
    private ProductSubscriber productSubscriber;
    @Inject
    @JetStreamProducer(connection = "secure")
    private JetStream jetStream;

    @POST
    @Path("/corn")
    public Response postCorn(Product corn) {
        if (jetStream == null) {
            return Response.serverError().build();
        }
        try {
            String uniqueID = UUID.randomUUID().toString();
            Headers headers = new Headers().add("Nats-Msg-Id", uniqueID);

            Message message = NatsMessage.builder()
                    .subject("product.corn")
                    .data(SerDes.serialize(corn))
                    .headers(headers)
                    .build();

            CompletableFuture<PublishAck> future = jetStream.publishAsync(message);
            PublishAck publishAck = future.get();
            return Response.ok(String.format("Message has been sent to stream %s", publishAck.getStream())).build();
        } catch (IOException | ExecutionException | InterruptedException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e).build();
        }
    }

    @POST
    @Path("/apple")
    public Response postApple(Product apple) {
        if (jetStream == null) {
            return Response.serverError().build();
        }
        try {
            Message message = NatsMessage.builder()
                    .subject("product.apple")
                    .data(SerDes.serialize(apple))
                    .build();

            PublishAck publishAck = jetStream.publish(message);
            return Response.ok(String.format("Message has been sent to stream %s", publishAck.getStream())).build();
        } catch (IOException | JetStreamApiException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e).build();
        }
    }

    @GET
    @Path("/pullCorn")
    public Response pullCorn() {
        productSubscriber.pullCorn();
        return Response.ok().build();
    }
}
