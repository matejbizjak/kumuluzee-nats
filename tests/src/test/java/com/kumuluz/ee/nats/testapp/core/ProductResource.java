package com.kumuluz.ee.nats.testapp.core;

import com.kumuluz.ee.nats.core.annotations.NatsClient;
import com.kumuluz.ee.nats.testapp.common.Product;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.CompletionStage;

/**
 * REST endpoint which calls NATS Core client and returns the received message.
 *
 * @author Matej Bizjak
 */

@Path("/product/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class ProductResource {

    @Inject
    @NatsClient
    private ProductClient productClient;

    @POST
    @Path("/withResponseProduct")
    public CompletionStage<Response> postProductResponseProduct(Product product) {
        return productClient.sendProductResponseProduct(product)
                .thenApply(response -> Response.ok(String.format("The product was sent. Even more, I also received a product as response asynchronously. Its name is %s"
                        , response.getName())).build())
                .exceptionally(e -> Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error processing response").build());
    }
}
