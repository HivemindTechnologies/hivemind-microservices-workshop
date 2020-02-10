package com.hivemindtechnologies.sentiments.stream

import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.hivemindtechnologies.sentiments.hocon.KafkaSettings
import com.typesafe.config.Config
import monix.reactive.Observable
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{
  StringDeserializer,
  StringSerializer
}
import org.reactivestreams.Publisher

class MonixKafka(
  producerSettings: ProducerSettings[String, String],
  consumerSettings: ConsumerSettings[String, String]
)(implicit mat: Materializer)
    extends Kafka[Observable] {

  override def push(topic: Topic, key: Key, value: Content): Observable[Unit] =
    Observable
      .from(
        Source
          .single(new ProducerRecord(topic.unwrap, key.unwrap, value.unwrap))
          .runWith(Producer.plainSink(producerSettings))
      )
      .map(_ => ())

  override def pull(topic: Topic): Observable[String] = {
    val publisher: Publisher[ConsumerRecord[String, String]] =
      Consumer
        .plainSource(consumerSettings, Subscriptions.topics(topic.unwrap))
        .runWith(
          Sink.asPublisher[ConsumerRecord[String, String]](fanout = false)
        )

    Observable.fromReactivePublisher(publisher).map(_.value())
  }
}

object MonixKafka {

  def fromConfig[F[_]](
    producerConfig: Config,
    consumerConfig: Config,
    kafkaSettings: KafkaSettings
  )(implicit materializer: Materializer): MonixKafka = {
    val producerSettings = ProducerSettings(
      producerConfig,
      new StringSerializer,
      new StringSerializer
    ).withBootstrapServers(kafkaSettings.bootstrapServers)

    val consumerSettings = ConsumerSettings(
      consumerConfig,
      new StringDeserializer,
      new StringDeserializer
    ).withBootstrapServers(kafkaSettings.bootstrapServers)
      .withGroupId(kafkaSettings.groupId)
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    new MonixKafka(producerSettings, consumerSettings)
  }

}
