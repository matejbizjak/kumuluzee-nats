package com.kumuluz.ee.nats.tests.tests;

import com.kumuluz.ee.nats.tests.beans.common.DemoApplication;
import com.kumuluz.ee.nats.tests.beans.common.Product;
import com.kumuluz.ee.nats.tests.beans.core.ProductClient;
import com.kumuluz.ee.nats.tests.beans.core.ProductListener;
import com.kumuluz.ee.nats.tests.beans.core.ProductResource;
import com.kumuluz.ee.nats.tests.beans.jetstream.TextListener;
import com.kumuluz.ee.nats.tests.beans.jetstream.TextResource;
import com.kumuluz.ee.nats.tests.beans.jetstream.TextSubscriber;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
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
 * @author Matej Bizjak
 */

public class NatsTest extends Arquillian {

    private static final int NATS_PORT = 4222;

    private static final GenericContainer<?> NATS = new GenericContainer<>("nats:latest")
            .withExposedPorts(NATS_PORT)
            .withCommand("-c /etc/nats/simple.conf")
            .withClasspathResourceMapping("./config/", "/etc/nats", BindMode.READ_ONLY);

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
                    .replace("<nats_port>", "" + NATS.getMappedPort(NATS_PORT));
        } catch (IOException e) {
            throw new IllegalStateException("Could not load config.yml", e);
        }

        return ShrinkWrap.create(JavaArchive.class)
                .addClass(ProductClient.class)
                .addClass(Product.class)
                .addClass(ProductListener.class)
                .addClass(ProductResource.class)
                .addClass(TextListener.class)
                .addClass(TextResource.class)
                .addClass(TextSubscriber.class)
                .addClass(DemoApplication.class)
                .addAsResource(new StringAsset(config), "config.yml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
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
                        , new BigDecimal("1.2"), 132, null, null))
                .contentType(ContentType.JSON)
                .when()
                .post("/v1/product/withResponse")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(equalTo("The product was sent. Even more, I also received a String as response: apple"));
    }

    @Test
    @RunAsClient
    public void validateJetStreamPublish1() {
        given()
                .body("simple message")
                .pathParam("subject", "subject1")
                .contentType(ContentType.JSON)
                .when()
                .post("/v1/text/uniqueSync/{subject}")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(equalTo("Message has been sent to subject subject1 in stream stream1."));
    }

    @Test
    @RunAsClient
    public void validateJetStreamPublish2() {
        given()
                .body("simple message to be pulled manually")
                .pathParam("subject", "subject2")
                .contentType(ContentType.JSON)
                .when()
                .post("/v1/text/async/{subject}")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(equalTo("Message has been sent to subject subject2 in stream stream1."));
    }

    @Test(dependsOnMethods = "validateJetStreamPublish2")
    @RunAsClient
    public void validateJetStreamPull() {
        when()
                .get("/v1/text/pull")
                .then()
                .assertThat()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(equalTo("simple message to be pulled manually"));
    }
}
