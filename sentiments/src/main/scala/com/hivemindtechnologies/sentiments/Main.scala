package com.hivemindtechnologies.sentiments

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.Monad
import cats.effect.{ExitCode, IO, IOApp}
import cats.tagless.implicits._
import com.hivemindtechnologies.sentiments.analyser.{Analyser, SttpAnalyser}
import com.hivemindtechnologies.sentiments.hocon.AppSettings
import com.hivemindtechnologies.sentiments.stream.{MonixKafka, Program}
import com.typesafe.config.ConfigFactory
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import monix.execution.Scheduler.Implicits.global
import monix.reactive.Observable
import pureconfig.generic.auto._
import pureconfig.module.catseffect._
import pureconfig.ConfigSource
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend

object Main extends IOApp {

  implicit val system: ActorSystem = ActorSystem("system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  override def run(args: List[String]): IO[ExitCode] =
    for {
      app <- ConfigSource.default
        .at("app")
        .loadF[IO, AppSettings]

      logger <- Slf4jLogger.create[IO]

      producerConfig <- IO(
        ConfigFactory.load().getConfig("akka.kafka.producer")
      )
      consumerConfig <- IO(
        ConfigFactory.load().getConfig("akka.kafka.consumer")
      )
      kafka = MonixKafka.fromConfig(producerConfig, consumerConfig, app.kafka)

      backend <- AsyncHttpClientCatsBackend[IO]()
      analyser <- IO.fromEither(
        SttpAnalyser.fromConfig[IO](app.sentimentAnalyser)(Monad[IO], backend)
      ): IO[Analyser[IO]]

      _ <- Program
        .run[Observable](
          app.kafka,
          kafka,
          analyser.mapK(Observable.liftFrom[IO]),
          logger.mapK(Observable.liftFrom[IO])
        )
        .consumeWith(monix.reactive.Consumer.foreach(identity))
        .to[IO]

    } yield ExitCode.Success
}
