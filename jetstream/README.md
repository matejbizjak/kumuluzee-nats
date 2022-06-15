# KumuluzEE Nats JetStream

Pazi pri JetStreamSubscriber:
ConsumerConfiguration je vezana durable.
Ko enkrat narediš durable z določeno konfiguracijo, ne moreš configuracije spreminjati.
Lahko jo z Nats CLI, ampak ne preko extensiona. Več si
preberi [tukaj](https://docs.nats.io/nats-concepts/jetstream/consumers).
