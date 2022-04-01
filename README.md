# KumuluzEE NATS Core

TODO

The extension is using a [NATS.java](https://github.com/nats-io/nats.java) Java client to communicate with NATS servers. 

## Usage:

NATS Core extension can be added via the following Maven dependency:
````xml
<dependency>
    <groupId>com.kumuluz.ee.nats-core</groupId>
    <artifactId>kumuluzee-nats-core</artifactId>
    <version>${nats-core.version}</version>
</dependency>
````

### Defining NATS client

To define a NATS client, we need to create an interface:

```java
@RegisterNatsClient(connection = "default")
public interface SimpleClient {

    @Subject(value = "simple1")
    void sendSimple(String value);

    String sendSimpleDynamicSubjectResponse(@Subject String subject, String value);

    @Subject(value = "simple2")
    String sendSimpleResponse(String value);
}
```

#### Annotation
We have to annotate the interface with `@RegisterNatsClient`, where we can specify the connection the client will use.

Each method must be annotated with `@Subject`, but there are two options:
- method annotation
- parameter annotation

The latter is used when we want to set the subject dynamically.

`@Subject` also has 1 optional parameter when used under `@RegisterNatsClient`:
- connection (overrides the connection from `@RegisterNatsClient`)

If those optional values are not set, NATS client will use the default values.

#### Return type

The return type of the methods specifies the response (message) object the client should receive. If the return type is `void` the client does not expect a response. 

Please, make sure that the custom classes you use have a default constructor. If not, they will not be de/serialized successfully. 

### Building a NATS client instance

The implementation of the NATS client interfaces is automatically generated during the runtime. We can simply inject the client to our service:

```java
@Inject
@NatsClient
private SimpleClient simpleClient;
```

### Using a NATS client

After injecting the client to our service, we can call the methods in the interface.

```java
simpleClient.sendSimple("simple string");
```
```java
String msgResponse = simpleClient.sendSimpleDynamicSubjectResponse("dynamic", "simple string with dynamic subject");
```
```java
String msgResponse = simpleClient.sendSimpleResponse("another simple string");
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
- queue

If the sender expects a response, the method can return an expected object as a response.

## Configuration

The configuration is completely optional. If no values are set, the extension will use the default values.

The prefix of all following properties must be `kumuluzee.nats-core`.

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

| Property                         | Type             | Description                                                                                        |
|----------------------------------|------------------|----------------------------------------------------------------------------------------------------|
| enabled                          | boolean          | Enables/disables the extension                                                                     |
| response-timeout                 | int              | Timeout for the response of the message                                                            |
| servers                          | java.util.List   | The list of servers                                                                                |
| servers.name                     | java.lang.String | The name of the connection to the server                                                           |
| servers.addresses                | java.util.List   | The list of the addresses                                                                          |
| servers.username                 | java.lang.String | The username                                                                                       |
| servers.password                 | java.lang.String | The password                                                                                       |
| servers.max-reconnect            | int              | Times to try reconnect                                                                             |
| servers.reconnect-wait           | int              | Number of seconds to wait before reconnecting                                                      |
| servers.connection-timeout       | int              | Timeout for the initial connection (in seconds)                                                    |
| servers.ping-interval            | int              | Time between server pings                                                                          |
| servers.reconnect-buffer-size    | long             | Size of the buffer (in bytes) used to store publish messages during reconnect                      |
| servers.inbox-prefix             | java.lang.String | Custom prefix for request/reply inboxes                                                            |
| servers.no-echo                  | boolean          | Enable or disable echo messages, messages that are sent by this connection back to this connection |
| servers.credentials              | java.lang.String | Path to the credentials file to use for the authentication with an account enabled server          |
| servers.tls.trust-store-path     | java.lang.String | Path to the trust store                                                                            |
| servers.tls.trust-store-password | java.lang.String | The password to unlock the trust store                                                             |
| servers.tls.certificate-path     | java.lang.String | Path to the server's certificate                                                                   |
| servers.tls.key-store-path       | java.lang.String | Path to the key store                                                                              |
| servers.tls.key-store-password   | java.lang.String | The password to unlock the key store                                                               |

### Default values

The extension is enabled by default.

- Server url: nats://localhost:4222
- Server port: 4222

For other default values take a look [here](https://github.com/nats-io/nats.java/blob/main/src/main/java/io/nats/client/Options.java).

### Examples

#### Default server connection with a custom response timeout
```xml
kumuluzee:
  nats-core:
    response-timeout: 5
```

#### TLS with a single address

```xml
kumuluzee:
  nats-core:
    response-timeout: 5
    servers:
      - name: secure-unverified-client
        addresses:
          - tls://localhost:4223
        tls:
#          trust-store-path: C:\Users\Matej\IdeaProjects\Nats Core Sample\src\main\resources\certs\truststore.jks
#          trust-store-password: password2
          certificate-path: C:\Users\Matej\IdeaProjects\Nats Core Sample\src\main\resources\certs\server-cert.pem
```

You can either specify the trust store and password, or the server's certificate path. 

#### Mutual TLS with a single address

```xml
kumuluzee:
  nats-core:
    servers:
      - name: secure
        addresses:
          - tls://localhost:4224
        tls:
          trust-store-path: C:\Users\Matej\IdeaProjects\Nats Core Sample\src\main\resources\certs\truststore.jks
          trust-store-password: password2
#          certificate-path: C:\Users\Matej\IdeaProjects\Nats Core Sample\src\main\resources\certs\server-cert.pem
          key-store-path: C:\Users\Matej\IdeaProjects\Nats Core Sample\src\main\resources\certs\keystore.jks
          key-store-password: password
```

For Mutual TLS you also need to specify a key store.