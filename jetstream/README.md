[//]: # (@formatter:off)
# KumuluzEE Nats JetStream

The extension is using a [NATS.java](https://github.com/nats-io/nats.java) Java client to communicate with NATS servers.

Publishing and subscribing to JetStream enabled servers is straightforward. A JetStream enabled application will connect
to a server, establish a JetStream context, and then publish or subscribe. This can be mixed and matched with standard
NATS subject, and JetStream subscribers, depending on configuration, receive messages from both streams and directly
from other NATS producers.

## Usage:

NATS JetStream extension can be added via the following Maven dependency:

```xml
<dependency>
    <groupId>com.kumuluz.ee.nats</groupId>
    <artifactId>kumuluzee-nats-jetstream</artifactId>
    <version>${nats.version}</version>
</dependency>
```

NATS JetStream extension can also be used simultaneously alongside NATS Core extension. 

If you would like to collect NATS related logs through the KumuluzEE Logs, you have to include the `kumuluzee-logs`
implementation and slf4j-log4j adapter dependencies:

```xml
<dependency>
    <artifactId>kumuluzee-logs-log4j2</artifactId>
    <groupId>com.kumuluz.ee.logs</groupId>
    <version>${kumuluzee-logs.version}</version>
</dependency>

<dependency>
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-slf4j-impl</artifactId>
    <version>${log4j-slf4j-impl.version}</version>
</dependency>
```

You also need to include a Log4j2 configuration, which should be in a file named `log4j2.xml`, located in
`src/main/resources`. For more information about KumuluzEE Logs visit the
[KumuluzEE Logs Github page](https://github.com/kumuluz/kumuluzee-logs).

> If you are unfamiliar with the NATS JetStream, please check the [documentation](https://docs.nats.io/nats-concepts/jetstream) or read the [Configuration](#configuration) section first.

### Publishing messages

For injecting a JetStream context, the KumuluzEE NATS JetStream provides the `@JetStreamProducer` annotation, which will
inject the producer reference.
We have to use it in conjunction with the `@Inject` annotation, as shown in the example below.

```java
@Inject
@JetStreamProducer
private JetStream jetStream;
```

`@JetStreamProducer` has two optional parameters:

- connection (name of the connection)
- context (name of the JetStream context)

If parameter values are not set, the default connection and JetStream context will be used.

Now we can use the injected JetStream reference to publish the message, as shown in the example below:

```java
Message message = NatsMessage.builder()
        .subject("subject")
        .data(SerDes.serialize("message"))
        .build();

PublishAck publishAck = jetStream.publish(message);
```

We can also use `publishAsync()` to publish asynchronously.

### Consuming messages

Consumers can either be **push** based where JetStream will deliver the messages as fast as possible (while adhering to the
rate limit policy) to a subject of
your choice, or **pull** to have control by asking the server for messages.

#### Push consumers

To specify a push consumer, we need to annotate a method with the `@JetStreamListener` annotation.
Server will push the messages to the client, which we can retrieve by the first function parameter.
Make sure to match the object type to the type of the producer.

There is another **optional** annotation available, which allows us to select a consumer configuration with an option to
override the configurations.
That annotation is `@ConsumerConfig`.

In the following example push consumer is listening to the subject `subject` on the default connection and default JetStream
context.
It uses custom consumer configuration named `custom` but overrides the `deliverPolicy` parameter.
It expects the message of the String data type.

```java
@JetStreamListener(subject = "subject")
@ConsumerConfig(name = "custom", configOverrides = {@ConfigurationOverride(key = "deliver-policy", value = "new")})
public void receive(String value) {
    System.out.println(value);
}
```

`@JetStreamListener` has the following parameters:

- connection
- context
- subject (required)
- stream (The stream to attach to. If not supplied the stream will be looked up by subject)
- queue (queue group to join)
- doubleAck (for double-acking, see [Exactly once delivery](#exactly-once-delivery))
- bind (whether this subscription is expected to bind to an existing stream and durable consumer)
- durable (consumer durable name, overrides the durable name from consumer configurations)
- ordered (whether this subscription is expected to ensure messages come in order)

> Durable means the server will remember where we are if we use that name.

##### Reseting redelivery timer for long operations

A push consumer's method may also contain a second parameter of the type `JetStreamMessage`.
It provides us a metadata of the message, and it allows us to call function `inProgress()`, which indicates that this message is being worked on and resets redelivery timer at the server.
It is useful when it takes longer to process a message.

```java
@JetStreamListener(subject = "subject")
@ConsumerConfig(name = "custom", configOverrides = {@ConfigurationOverride(key = "deliver-policy", value = "new")})
public void receive(String value, JetStreamMessage msg) {
    try {
        // long processing of the message
        msg.inProgress();
        // long processing of the message
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }
}
```

#### Pull consumers

To be able to manually pull messages from a server we need to inject `JetStreamSubscription` reference with the help
of the `@JetStreamSubscriber` annotation.
We have to use it in conjunction with the `@Inject` annotation, as shown in the example below. 

> :warning: Durable must be set for pull based consumers!

There is another **optional** annotation available, which allows us to select a consumer configuration with an option to
override the configurations.
That annotation is `@ConsumerConfig`.

In the following example the pull consumer is listening to the subject `subject` on the default connection and default
context.
Durable name is set to `somethingNew`.
It uses custom consumer configuration named `custom` but overrides the `deliverPolicy` parameter.
It fetches up to 10 messages with a timeout of 1 second. It expects the data to be of String data type.

```java
@Inject
@JetStreamSubscriber(subject = "subject", durable = "somethingNew")
@ConsumerConfig(name = "custom", configOverrides = {@ConfigurationOverride(key = "deliver-policy", value = "new")})
private JetStreamSubscription jetStreamSubscription;

public void pullMsg() {
    if (jetStreamSubscription != null) {
        List<Message> messages = jetStreamSubscription.fetch(10, Duration.ofSeconds(1));
        for (Message message : messages) {
            try {
                System.out.println(SerDes.deserialize(message.getData(), String.class));
                message.ack();
            } catch (IOException e) {
                ...
            }
        }
    }
}
```

`@JetStreamSubscriber` has the following parameters:
- connection
- context
- stream (The stream to attach to. If not supplied the stream will be looked up by subject)
- subject (required)
- durable (required)
- bind (whether this subscription is expected to bind to an existing stream and durable consumer)

### Exactly Once Delivery

JetStream supports exactly-once delivery guarantees by combining Message Deduplication and double acks.

#### Producer

On the publishing side you can avoid duplicate message ingestion using the Message Deduplication feature.

JetStream support idempotent message writes by ignoring duplicate messages as indicated by the `Nats-Msg-Id header`.
This tells JetStream to ensure we do not have duplicates of this message - we only consult the message ID not the body.

```java
String uniqueID = UUID.randomUUID().toString();
Headers headers = new Headers().add("Nats-Msg-Id", uniqueID);

Message message=NatsMessage.builder()
        .subject("subject")
        .data(SerDes.serialize("message"))
        .headers(headers)
        .build();

PublishAck publishAck = jetStream.publish(message);
```

#### Consumer

Consumers can be 100% sure a message was correctly processed by requesting the server Acknowledge having received your acknowledgement (sometimes referred to as double-acking) by calling the message's `AckSync()` (rather than `Ack()`) function which sets a reply subject on the Ack and waits for a response from the server on the reception and processing of the acknowledgement. If the response received from the server indicates success you can be sure that the message will never be re-delivered by the consumer (due to a loss of your acknowledgement).

##### Push consumer

To enable double-acking for push based subscribers, set the `doubleAck`  in `@JetStreamListener` annotation to `true`.

##### Pull consumer

For pull based subscribers, use `AckSync()` function instead of `Ack()`.

## NATS Administration

Streams and durable consumers can be defined administratively outside the application (typically using the [NATS CLI Tool](https://docs.nats.io/using-nats/nats-tools/nats_cli)) in which case the application only needs to know about the well-known names of the durable consumers it wants to use.
But with KumuluzEE JetStream you can manage streams and consumers programmatically, simply by specifying them in the configurations.

> :warning: You cannot update a consumer (change its configuration) once created.

> :warning: You cannot delete a stream or consumer directly with KumuluzEE JetStream. You can do this manually typically using the [NATS CLI Tool](https://docs.nats.io/using-nats/nats-tools/nats_cli).

NATS supports a number of [other tools](https://docs.nats.io/running-a-nats-service/configuration/resource_management/configuration_mgmt) to assist with configuration management, if you decide not to use KumuluzEE JetStream for this purpose.

## Configuration

NATS JetStream is very flexible and powerful system which requires a lot of configuration, however most of it is
completely optional.
Configurations are read from KumuluzEE configuration file (`resources/config.yml`). Alternatively, they can also be stored in
a configuration server, such as etcd or Consul (for which the KumuluzEE Config project is required). For more details
see the [KumuluzEE configuration wiki page](https://github.com/kumuluz/kumuluzee/wiki/Configuration) and
[KumuluzEE Config](https://github.com/kumuluz/kumuluzee-config).

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

The configuration is split into more parts for easier understanding.

### Common

Prefix: `kumuluzee.nats`

[//]: # (TODO napiši v tabelo še default vrednosti?)

| Property                 | Type               | Description                                                                                              |
|--------------------------|--------------------|----------------------------------------------------------------------------------------------------------|
| enabled                  | boolean            | Enables/disables both NATS extensions                                                                    |
| jetstream                | boolean            | Enables/disables NATS JetStream extension                                                                |
| ack-confirmation-timeout | java.time.Duration | Timeout for a server's acknowledgment confirmation (double-acking)                                       |
| ack-confirmation-retries | int                | Maximum number of retries a consumer asks the server for the acknowledgment confirmation (double-acking) |
| servers                  | java.util.List     | The list of servers                                                                                      |
| consumer-configuration   | java.util.List     | The list of consumer configurations                                                                      |

### Servers

Prefix: `kumuluzee.nats.servers`

In order for a NATS client application to connect to the NATS service, and then subscribe or publish messages to
subjects, it needs to be able to be configured with the details of how to connect to the NATS service infrastructure and
of how to authenticate with it.

More info [here](https://docs.nats.io/using-nats/developer/connecting).

| Property                 | Type                | Description                                                                                        |
|--------------------------|---------------------|----------------------------------------------------------------------------------------------------|
| name                     | java.lang.String    | The name of the connection to the server                                                           |
| addresses                | java.util.List      | The list of the addresses                                                                          |
| username                 | java.lang.String    | The username                                                                                       |
| password                 | java.lang.String    | The password                                                                                       |
| max-reconnect            | int                 | Times to try reconnect                                                                             |
| reconnect-wait           | java.time.Duration  | Time to wait before reconnecting                                                                   |
| connection-timeout       | java.time.Duration  | Timeout for the initial connection                                                                 |
| ping-interval            | java.time.Duration  | Time between server pings                                                                          |
| reconnect-buffer-size    | long                | Size of the buffer (in bytes) used to store publish messages during reconnect                      |
| inbox-prefix             | java.lang.String    | Custom prefix for request/reply inboxes                                                            |
| no-echo                  | boolean             | Enable or disable echo messages, messages that are sent by this connection back to this connection |
| credentials              | java.lang.String    | Path to the credentials file to use for the authentication with an account enabled server          |
| tls.trust-store-path     | java.lang.String    | Path to the trust store                                                                            |
| tls.trust-store-password | java.lang.String    | The password to unlock the trust store                                                             |
| tls.certificate-path     | java.lang.String    | Path to the server's certificate                                                                   |
| tls.key-store-path       | java.lang.String    | Path to the key store                                                                              |
| tls.key-store-password   | java.lang.String    | The password to unlock the key store                                                               |
| streams                  | java.util.List      | The list of streams                                                                                |
| jetstream-contexts       | java.util.List      | The list of JetStream contexts                                                                     |

### Streams

Prefix: `kumuluzee.nats.servers.streams`

Streams are 'message stores', each stream defines how messages are stored and what the limits (duration, size, interest)
of the retention are. Streams consume normal NATS subjects, any message published on those subjects will be captured in
the defined storage system.

More info [here](https://docs.nats.io/nats-concepts/jetstream/streams).

| Property         | Type                               | Description                                                                                                                                             |
|------------------|------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| name             | java.lang.String                   | A name for the Stream that may not have spaces, tabs, period (.), greater than (>) or asterisk (*).                                                     |
| subjects         | java.util.List<java.lang.String>   | A list of subjects to consume, supports wildcards                                                                                                       |
| description      | java.lang.String                   | Description                                                                                                                                             |
| retention-policy | io.nats.client.api.RetentionPolicy | How message retention is considered, LimitsPolicy (default), InterestPolicy or WorkQueuePolicy                                                          |
| max-consumers    | long                               | How many Consumers can be defined for a given Stream, -1 for unlimited                                                                                  |
| max-bytes        | long                               | How many bytes the Stream may contain. Adheres to Discard Policy, removing oldest or refusing new messages if the Stream exceeds this size              |
| max-age          | java.time.Duration                 | Maximum age of any message in the Stream, expressed in nanoseconds                                                                                      |
| max-msgs         | long                               | How many messages may be in a Stream. Adheres to Discard Policy, removing oldest or refusing new messages if the Stream exceeds this number of messages |
| max-msg-size     | long                               | The largest message that will be accepted by the Stream                                                                                                 |
| storage-type     | io.nats.client.api.StorageType     | The type of storage backend, File and Memory                                                                                                            |
| replicas         | int                                | How many replicas to keep for each message in a clustered JetStream, maximum 5                                                                          |
| no-ack           | boolean                            | Disables acknowledging messages that are received by the Stream                                                                                         |
| template-owner   | java.lang.String                   |                                                                                                                                                         |
| discard-policy   | io.nats.client.api.DiscardPolicy   | When a Stream reaches it's limits either, DiscardNew refuses new messages while DiscardOld (default) deletes old messages                               |
| duplicate-window | java.time.Duration                 | The window within which to track duplicate messages                                                                                                     |

### JetStream Contexts

Prefix: `kumuluzee.nats.servers.jetstream-contexts`

You can pass options to configure the JetStream client, although the defaults should suffice for most users.

There is no limit to the number of contexts used, although normally one would only require a single context.
Contexts may be prefixed to be used in conjunction with NATS authorization.

| Property        | Type                             | Description                                                                                                                                                                                                                                                                   |
|-----------------|----------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| name            | java.lang.String                 | A name of the JetStream context.                                                                                                                                                                                                                                              |
| domain          | java.util.List<java.lang.String> | Sets the domain for JetStream subjects, creating a standard prefix from that domain in the form $JS.(domain).API. A domain can be used in conjunction with user permissions to restrict access to certain JetStream instances. This must match the domain used in the server. |
| prefix          | java.lang.String                 | Sets the prefix for JetStream subjects. A prefix can be used in conjunction with user permissions to restrict access to certain JetStream instances. This must match the prefix used in the server.                                                                           |
| publish-no-ack  | boolean                          | Sets whether the streams in use by contexts created with these options are no-ack streams.                                                                                                                                                                                    |
| request-timeout | java.time.Duration               | Sets the request timeout for JetStream API calls                                                                                                                                                                                                                              |

### Consumer configuration

Prefix: `kumuluzee.nats.consumer-configuration`

Consumers can be conceived as 'views' into a stream, with their own 'cursor'. Consumers iterate or consume over all or a
subset of the messages stored in the stream, according to their 'subject filter' and 'replay policy', and can be used by
one or multiple client applications. It's ok to define thousands of these pointing at the same Stream.

Consumers can either be push based where JetStream will deliver the messages as fast as possible (while adhering to the
rate limit policy) to a subject of your choice or pull to have control by asking the server for messages. The choice of
what kind of consumer to use depends on the use-case but typically in the case of a client application that needs to get
their own individual replay of messages from a stream you would use an 'ordered push consumer', while in the case of
scaling horizontally the processing of messages from a stream you would use a 'pull consumer'.

More info [here](https://docs.nats.io/nats-concepts/jetstream/consumers).

| Property           | Type                                    | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
|--------------------|-----------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| name               | java.lang.String                        | The name (identifier) of the consumer configuration                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| deliver-policy     | io.nats.client.api.DeliverPolicy        | When a consumer is first created, it can specify where in the stream it wants to start receiving messages.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| ack-policy         | io.nats.client.api.AckPolicy            | How messages should be acknowledged. If an ack is required but is not received within the AckWait window, the message will be redelivered.                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| replay-policy      | io.nats.client.api.ReplayPolicy         | The replay policy applies when the DeliverPolicy is DeliverAll, DeliverByStartSequence or DeliverByStartTime since those deliver policies begin reading the stream at a position other than the end. If the policy is ReplayOriginal, the messages in the stream will be pushed to the client at the same rate that they were originally received, simulating the original timing of messages. If the policy is ReplayInstant (the default), the messages will be pushed to the client as fast as possible while adhering to the Ack Policy, Max Ack Pending and the client's ability to consume those messages. |
| description        | java.lang.String                        | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| durable            | java.lang.String                        | The name of the Consumer, which the server will track, allowing resuming consumption where left off. By default, a consumer is ephemeral. To make the consumer durable, set the name.                                                                                                                                                                                                                                                                                                                                                                                                                            |
| deliver-subject    | java.lang.String                        | The subject to deliver observed messages, specifying a delivery subject makes the consumer a 'push consumer' as 'pull consumers' do not need a static delivery subject.                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| deliver-group      | java.lang.String                        | If you want to distribute the messages between the subscribers to the consumer.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| filter-subject     | java.lang.String                        | When consuming from a stream with a wildcard subject, this allows you to select a subset of the full wildcard subject to receive messages from.                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| sample-frequency   | java.lang.String                        | Sets the percentage of acknowledgements that should be sampled for observability, 0-100 This value is a string and for example allows both 30 and 30% as valid values                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| start-time         | java.time.ZonedDateTime (ISO_DATE_TIME) | Necessary when deliverPolicy is set to DeliverByStartTime. When first consuming messages, start with messages on or after this time.                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| ack-wait           | java.time.Duration                      | Ack Wait is the time in nanoseconds that the server will wait for an ack for any individual message once it has been delivered to a consumer. If an ack is not received in time, the message will be redelivered.                                                                                                                                                                                                                                                                                                                                                                                                |
| idle-heartbeat     | java.time.Duration                      | If the idle heartbeat period is set, the server will regularly send a status message to the client (i.e. when the period has elapsed) while there are no new messages to send. This lets the client know that the JetStream service is still up and running, even when there is no activity on the stream. The message status header will have a code of 100. Unlike FlowControl, it will have no reply to address. It may have a description like "Idle Heartbeat".                                                                                                                                             |
| max-expires        | java.time.Duration                      |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| inactive-threshold | java.time.Duration                      |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| start-seq          | long                                    | Necessary when deliverPolicy is set to DeliverByStartSequence. When first consuming messages, start at this particular message in the set.                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| max-deliver        | int                                     | The maximum number of times a specific message will be delivered. Applies to any message that is re-sent due to ack policy.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| rate-limit         | long                                    | Used to throttle the delivery of messages to the consumer, in bits per second.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| max-ack-pending    | int                                     | Implements a simple form of one-to-many flow control. It sets the maximum number of messages without an acknowledgement that can be outstanding, once this limit is reached message delivery will be suspended. It cannot be used with AckNone ack policy. This maximum number of pending acks applies for all of the consumer's subscriber processes. A value of -1 means there can be any number of pending acks (i.e. no flow control).                                                                                                                                                                       |
| max-pull-waiting   | int                                     | Maximum waiting to pull a batch of messages???                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| max-batch          | int                                     | Maximum batch size for pull subscribers???                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| max-bytes          | int                                     | Maximum message size???                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| flow-control       | boolean                                 | This flow control setting is to enable or not another form of flow control in parallel to MaxAckPending. But unlike MaxAckPending it is a one-to-one flow control that operates independently for each individual subscriber to the consumer. It uses a sliding-window flow-control protocol whose attributes (e.g. size of the window) are not user adjustable.                                                                                                                                                                                                                                                 |
| headers-only       | boolean                                 |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| backoff            | java.util.List<java.time.Duration>      | https://github.com/nats-io/nats-server/pull/2812                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |

### Default values

The extension is enabled by default.

- Server url: nats://localhost
- Server port: 4222

Other default values:

- [Options](https://github.com/nats-io/nats.java/blob/main/src/main/java/io/nats/client/Options.java)
- [ConsumerConfiguration](https://github.com/nats-io/nats.java/blob/main/src/main/java/io/nats/client/api/ConsumerConfiguration.java)
- [JetStreamOptions](https://github.com/nats-io/nats.java/blob/main/src/main/java/io/nats/client/JetStreamOptions.java)
- [StreamConfiguration](https://github.com/nats-io/nats.java/blob/main/src/main/java/io/nats/client/api/StreamConfiguration.java)

## Sample

Samples are available [here](https://github.com/matejbizjak/kumuluzee-nats-jetstream-sample).