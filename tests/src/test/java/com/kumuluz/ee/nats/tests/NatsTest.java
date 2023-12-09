package com.kumuluz.ee.nats.tests;

import com.kumuluz.ee.nats.common.util.NatsObjectMapperProvider;
import com.kumuluz.ee.nats.testapp.common.NatsMapperProvider;
import com.kumuluz.ee.nats.testapp.common.Product;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Starts a Docker container with the latest Nats server and runs several tests to ensure the correctness of the KumuluzEE NATS extension.
 *
 * @author Matej Bizjak
 */

public class NatsTest extends Arquillian {

    private static final int NATS_PORT = 4224;

    private static final Product CORN_PRODUCT = new Product(1, "Corn", "Corn for popcorn - 1 kg"
            , new BigDecimal("3.2"), 12, null, Instant.EPOCH);

    private static final GenericContainer<?> NATS = new GenericContainer<>("nats:latest")
            .withExposedPorts(NATS_PORT)
            .withCommand("-c /etc/nats/tlsverify.conf")
            .withClasspathResourceMapping("./config/", "/etc/nats", BindMode.READ_ONLY)
            .withClasspathResourceMapping("./certs/", "/etc/nats/certs", BindMode.READ_ONLY)
            .waitingFor(Wait.forLogMessage(".*Server is ready.*\\n", 1));

    @Deployment
    public static JavaArchive createDeployment() {
        NATS.start();

        RestAssured.baseURI = "http://localhost:8080";
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        String config;
        try {
            config = new String(Objects.requireNonNull(
                    NatsTest.class.getClassLoader().getResourceAsStream("config.yml"),
                    "Could not load config.yml"
            ).readAllBytes(), StandardCharsets.UTF_8)
                    .replace("<nats_port>", String.valueOf(NATS.getMappedPort(NATS_PORT)));
        } catch (IOException e) {
            throw new IllegalStateException("Could not load config.yml", e);
        }

        return ShrinkWrap.create(JavaArchive.class)
                .addPackages(true, "com.kumuluz.ee.nats.testapp")
                .addAsServiceProvider(NatsObjectMapperProvider.class, NatsMapperProvider.class)
                .addAsResource(new StringAsset(config), "config.yml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource("certs/keystore.jks", "certs/keystore.jks")
                .addAsResource("certs/truststore.jks", "certs/truststore.jks");
    }

    @AfterClass
    @RunAsClient
    public static void stopContainer() {
        System.out.println("------ NATS LOGS: ------");
        System.out.println(NATS.getLogs());
        NATS.stop();
    }

    @Test
    @RunAsClient
    public void validateCorePublishResponse() {
        given()
                .body(new Product(2, "Apple", "Fuji Apple - 1 kg"
                        , new BigDecimal("1.2"), 132, null, Instant.now()))
                .contentType(ContentType.JSON)
                .when()
                .post("/v1/product/withResponseProduct")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(equalTo("The product was sent. Even more, I also received a product as response. Its name is APPLE"));
    }

    @Test
    @RunAsClient
    public void validateJetStreamPublish1() {
        given()
                .body(CORN_PRODUCT)
                .contentType(ContentType.JSON)
                .when()
                .post("/v1/product/corn")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(equalTo("Message has been sent to stream stream2"));
    }

    @Test
    @RunAsClient
    public void validateJetStreamPublish2() {
        given()
                .body(new Product(1, "Apple", "Fuji Apple - 1 kg"
                        , new BigDecimal("1.2"), 132, null, Instant.EPOCH))
                .contentType(ContentType.JSON)
                .when()
                .post("/v1/product/apple")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(equalTo("Message has been sent to stream stream2"));
    }

    @Test
    @RunAsClient
    public void validateJetStreamPublish3() {
        given()
                .body(new Product(1, "Lemon", "Shiny lemon - 250g"
                        , new BigDecimal("3.2"), 12, null, Instant.EPOCH))
                .contentType(ContentType.JSON)
                .when()
                .post("/v1/product/any-product")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(equalTo("Message has been sent to stream stream2"));
    }

    @Test(dependsOnMethods = "validateJetStreamPublish1")
    @RunAsClient
    public void validateJetStreamPull() {
        Response response = when()
                .get("/v1/product/pullCorn")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .extract().response();
        Product product = response.getBody().as(Product.class);
        Assert.assertEquals(product.getId(), CORN_PRODUCT.getId());
        Assert.assertEquals(product.getDescription(), CORN_PRODUCT.getDescription());
        Assert.assertEquals(product.getName(), CORN_PRODUCT.getName());
        Assert.assertEquals(product.getStock(), CORN_PRODUCT.getStock());
        Assert.assertEquals(product.getImage(), CORN_PRODUCT.getImage());
        Assert.assertEquals(product.getPrice(), CORN_PRODUCT.getPrice());
        Assert.assertEquals(product.getAddedDate(), CORN_PRODUCT.getAddedDate());
    }
}
