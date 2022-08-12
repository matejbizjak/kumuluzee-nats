[//]: # (@formatter:off)
# KumuluzEE NATS Core

The extension is using a [NATS.java](https://github.com/nats-io/nats.java) Java client to communicate with NATS servers. 

## Usage:

NATS Core extension can be added via the following Maven dependency:
````xml
<dependency>
    <groupId>com.kumuluz.ee.nats</groupId>
    <artifactId>kumuluzee-nats-core</artifactId>
    <version>${nats.version}</version>
</dependency>
````

NATS Core extension can also be used simultaneously alongside NATS JetStream extension.

### Defining NATS client

To define a NATS client, we need to create an interface:

```java
@RegisterNatsClient(connection = "default")
public interface SimpleClient {

    @Subject(value = "simple1")
    void sendSimple(String value);

    @Subject(value = "simple2")
    String sendSimpleResponse(String value);
    
    String sendSimpleDynamicSubjectResponse(@Subject String subject, String value);
    
    @Subject(connection = "secure", responseTimeout = "5s")
    String sendSimpleDynamicSubjectResponse2(@Subject String subject, String value);
}
```

#### Annotation
We have to annotate an interface with `@RegisterNatsClient`, where we can specify the connection a client will use.

Each method must be annotated with `@Subject`, but there are two options:
- method annotation
- parameter annotation

The latter is used when we want to set the subject dynamically.
We can use both annotation types on the same method, which is useful when want to use dynamic subject while also specifying the connection or the response timeout. 

`@Subject` also has 2 optional parameters when used under `@RegisterNatsClient`:
- connection (overrides the connection from `@RegisterNatsClient`)
- responseTimeout (overrides the responseTimeout from the general configurations)

If those optional values are not set, NATS client will use the default values.

#### Return type

Method's return type specifies the response (message) object a client should receive. If the return type is `void` the client does not expect a response. 

> :warning: Please, make sure that the custom classes you use have a default constructor. If not, they will not be de/serialized successfully.

### Building a NATS client instance

The implementation of the NATS client interfaces is automatically generated during the runtime. We can simply inject a client to our service using `@Inject` and `@NatsClient`:

```java
@Inject
@NatsClient
private SimpleClient simpleClient;
```

> :warning: Please, make sure to set the `bean-discovery-mode` to `all` in the `beans.xml` file or the generated class will not be discovered. 

### Using a NATS client

After injecting a client to our service, we can call the methods from the interface.

```java
simpleClient.sendSimple("simple string");
```
```java
String msgResponse = simpleClient.sendSimpleResponse("another simple string");
```
```java
String msgResponse = simpleClient.sendSimpleDynamicSubjectResponse("dynamic", "simple string with dynamic subject");
```
```java
String msgResponse = simpleClient.sendSimpleDynamicSubjectResponse2("dynamic", "simple string with dynamic subject and overrided settings");
```

### Defining a NATS listener

```java
@NatsListener(connection = "default")
public class SimpleListener {

    @Subject(value = "simple1")
    public void receive(String value) {
        System.out.println(value);
    }

    @Subject(value = "simple2", queue = "group1")
    public String receiveAndReturn1(String value) {
        System.out.println(value);
        return value.toUpperCase();
    }

    @Subject(value = "simple2", queue = "group1")
    public String receiveAndReturn2(String value) {
        System.out.println(value);
        return value.toLowerCase();
    }

    @Subject(value = "dynamic")
    public String receiveDynamicSubject(String value) {
        System.out.println(value);
        return value.toUpperCase() + "_DYNAMIC_SUBJECT";
    }
}
```

To listen for the NATS messages we need to annotate a class with `@NatsListener` and its methods with `@Subject`. 

`@Subject` also has 2 optional parameters when used under `@NatsListener`:
- connection (overrides the connection from `@RegisterNatsClient`)
- queue (only one of the listeners in the same queue reveives the message)

If sender expects a response, the method can return **the expected** object as a response.

## Configuration

In order for a NATS client application to connect to the NATS service, and then subscribe or publish messages to
subjects, it needs to be able to be configured with the details of how to connect to the NATS service infrastructure and
of how to authenticate with it. However, the configuration is completely optional. If no values are set, the extension will use the default values.

The prefix of all following properties must be `kumuluzee.nats`.

[//]: # ()
[//]: # (#### Single connection)

[//]: # ()
[//]: # (Used when connecting to a single server.)

[//]: # ()
[//]: # (| Property                 | Type             | Description                                                                                        |)

[//]: # (|--------------------------|------------------|----------------------------------------------------------------------------------------------------|)

[//]: # (| enabled                  | boolean          | Enables/disables the extension                                                                     |)

[//]: # (| response-timeout         | int              | Timeout for the response of the message                                                            |)

[//]: # (| name                     | java.lang.String | The name of the connection to the server                                                           |)

[//]: # (| addresses                | java.util.List   | The list of the addresses                                                                          |)

[//]: # (| username                 | java.lang.String | The username                                                                                       |)

[//]: # (| password                 | java.lang.String | The password                                                                                       |)

[//]: # (| max-reconnect            | int              | Times to try reconnect                                                                             |)

[//]: # (| reconnect-wait           | int              | Number of seconds to wait before reconnecting                                                      |)

[//]: # (| connection-timeout       | int              | Timeout for the initial connection &#40;in seconds&#41;                                                    |)

[//]: # (| ping-interval            | int              | Time between server pings                                                                          |)

[//]: # (| reconnect-buffer-size    | long             | Size of the buffer &#40;in bytes&#41; used to store publish messages during reconnect                      |)

[//]: # (| inbox-prefix             | java.lang.String | Custom prefix for request/reply inboxes                                                            |)

[//]: # (| no-echo                  | boolean          | Enable or disable echo messages, messages that are sent by this connection back to this connection |)

[//]: # (| credentials              | java.lang.String | Path to the credentials file to use for the authentication with an account enabled server          |)

[//]: # (| tls.trust-store-path     | java.lang.String | Path to the trust store                                                                            |)

[//]: # (| tls.trust-store-password | java.lang.String | The password to unlock the trust store                                                             |)

[//]: # (| tls.certificate-path     | java.lang.String | Path to the server's certificate                                                                   |)

[//]: # (| tls.key-store-path       | java.lang.String | Path to the key store                                                                              |)

[//]: # (| tls.key-store-password   | java.lang.String | The password to unlock the key store                                                               |)

[//]: # ()
[//]: # ()
[//]: # (#### Cluster connection)

| Property                         | Type                | Description                                                                                        |
|----------------------------------|---------------------|----------------------------------------------------------------------------------------------------|
| enabled                          | boolean             | Enables/disables the extension                                                                     |
| response-timeout                 | java.time.Duration  | Timeout for the response of the message                                                            |
| servers                          | java.util.List      | The list of servers                                                                                |
| servers.name                     | java.lang.String    | The name of the connection to the server                                                           |
| servers.addresses                | java.util.List      | The list of the addresses                                                                          |
| servers.username                 | java.lang.String    | The username                                                                                       |
| servers.password                 | java.lang.String    | The password                                                                                       |
| servers.max-reconnect            | int                 | Times to try reconnect                                                                             |
| servers.reconnect-wait           | java.time.Duration  | Time to wait before reconnecting                                                                   |
| servers.connection-timeout       | java.time.Duration  | Timeout for the initial connection                                                                 |
| servers.ping-interval            | java.time.Duration  | Time between server pings                                                                          |
| servers.reconnect-buffer-size    | long                | Size of the buffer (in bytes) used to store publish messages during reconnect                      |
| servers.inbox-prefix             | java.lang.String    | Custom prefix for request/reply inboxes                                                            |
| servers.no-echo                  | boolean             | Enable or disable echo messages, messages that are sent by this connection back to this connection |
| servers.credentials              | java.lang.String    | Path to the credentials file to use for the authentication with an account enabled server          |
| servers.tls.trust-store-path     | java.lang.String    | Path to the trust store                                                                            |
| servers.tls.trust-store-password | java.lang.String    | The password to unlock the trust store                                                             |
| servers.tls.certificate-path     | java.lang.String    | Path to the server's certificate                                                                   |
| servers.tls.key-store-path       | java.lang.String    | Path to the key store                                                                              |
| servers.tls.key-store-password   | java.lang.String    | The password to unlock the key store                                                               |

### Clusters & Reconnecting

The Java client will automatically reconnect if it loses its connection the nats-server. If given a single server, the client will keep trying that one. If given a list of servers, the client will rotate between them. When the nats servers are in a cluster, they will tell the client about the other servers, so that in the simplest case a client could connect to one server, learn about the cluster and reconnect to another server if its initial one goes down.

### Default values

The extension is enabled by default.

- Server url: nats://localhost
- Server port: 4222

For other default values take a look [here](https://github.com/nats-io/nats.java/blob/main/src/main/java/io/nats/client/Options.java).

### Parsing format

| Type                                | Format               | Example                   |
|-------------------------------------|----------------------|---------------------------|
| java.lang.String                    |                      | example-string            |
| int                                 |                      | 10                        |
| long                                |                      | 10                        |
| boolean                             |                      | true                      |
| java.time.Duration                  | ISO-8601             | PT5S                      |

### Providing ObjectMapper

KumuluzEE NATS Core uses Jackson for de/serializing and can use a custom instance of `ObjectMapper` to perform the conversion. In order to supply
a custom instance implement the `NatsObjectMapperProvider` interface and register the implementation in a service file.
For example:

```java
public class NatsMapperProvider implements NatsObjectMapperProvider {
    
    @Override
    public ObjectMapper provideObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
```

Do not forget to register the implementation and add the required dependencies.
A Service Provider is configured and identified through a provider configuration file which we put in the resource directory META-INF/services. The file name is the fully-qualified name of the SPI and its content is the fully-qualified name of the SPI implementation.

[//]: # (In our example in the resource directory `META-INF/services` add)

[//]: # (a file `com.kumuluz.ee.nats.common.util.NatsObjectMapperProvider` with the content `si.matejbizjak.natscore.sample.api.NatsMapperProvider`.)

## Sample

Samples are available [here](https://github.com/matejbizjak/kumuluzee-nats-core-sample).

[//]: # (---)

[//]: # (## Notes)

[//]: # ()
[//]: # (### Poskus za asinhrono čakanje na odgovor)

[//]: # ()
[//]: # (NatsClientInvoker.java:)

[//]: # ()
[//]: # (```java)

[//]: # (...)

[//]: # (        else if&#40;returnType.equals&#40;Future.class&#41;&#41;{ // wait for response asynchronously)

[//]: # (        CompletableFuture<Object> completableFuture=new CompletableFuture<>&#40;&#41;;)

[//]: # (        // v0.2)

[//]: # (        Dispatcher dispatcher=connection.createDispatcher&#40;&#40;msg&#41;->{}&#41;;)

[//]: # (        // Problem je v naslednji vrstici, ker je message.getReplyTo&#40;&#41; == null in ga ne moreš uporabiti, ker ga uporablja NATS za interne zadeve.)

[//]: # (        // The Message object allows you to set a replyTo, but in requests, the replyTo is reserved for internal use as the address for the server to respond to the client with the consumer's reply.)

[//]: # (    Subscription subscription = dispatcher.subscribe&#40;message.getReplyTo&#40;&#41;, &#40;msg&#41; -> {)

[//]: # (        try {)

[//]: # (            Object receivedMsg = SerDes.deserialize&#40;msg.getData&#40;&#41;)

[//]: # (                    , &#40;Class<?>&#41; &#40;&#40;ParameterizedType&#41; method.getParameterTypes&#40;&#41;[0].getGenericSuperclass&#40;&#41;&#41;.getActualTypeArguments&#40;&#41;[0]&#41;;)

[//]: # (            completableFuture.complete&#40;receivedMsg&#41;;)

[//]: # (        } catch &#40;IOException e&#41; {)

[//]: # (            throw new NatsListenerException&#40;String.format&#40;"Cannot deserialize the message as class %s.")

[//]: # (                    , method.getParameterTypes&#40;&#41;[0].getName&#40;&#41;&#41;, e&#41;;)

[//]: # (        })

[//]: # (    }&#41;;)

[//]: # (    connection.publish&#40;message&#41;;)

[//]: # (    dispatcher.unsubscribe&#40;subscription&#41;;  // TODO)

[//]: # (    return completableFuture;)

[//]: # (})

[//]: # (```)