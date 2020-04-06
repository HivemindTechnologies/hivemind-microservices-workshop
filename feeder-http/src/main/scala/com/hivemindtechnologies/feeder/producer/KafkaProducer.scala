package com.hivemindtechnologies.feeder.producer

import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import cats.effect.IO
import com.hivemindtechnologies.feeder.hocon.KafkaSettings
import com.typesafe.config.Config
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

class KafkaProducer(
                          producerSettings: ProducerSettings[String, String],
                          topic: Topic
                        )(implicit mat: Materializer)
  extends Producer[IO] {

  override def push(key: Key, value: Content): IO[Either[Throwable, Unit]] = {
    import cats.implicits._
    implicit val ctxShift = IO.contextShift(scala.concurrent.ExecutionContext.global)

    IO.fromFuture(IO(Source
      .single(new ProducerRecord(topic.unwrap, key.unwrap, value.unwrap))
      .runWith(Producer.plainSink(producerSettings)))).void.attempt

  }
}

object KafkaProducer {

  def fromConfig[F[_]](producerConfig: Config, kafkaSettings: KafkaSettings)(
    implicit materializer: Materializer
  ): KafkaProducer = {
    val producerSettings = ProducerSettings(
      producerConfig,
      new StringSerializer,
      new StringSerializer
    ).withBootstrapServers(kafkaSettings.bootstrapServers)
    new KafkaProducer(producerSettings, Topic(kafkaSettings.outputTopic))
  }

}
