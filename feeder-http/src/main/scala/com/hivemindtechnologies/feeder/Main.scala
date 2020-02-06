package com.hivemindtechnologies.feeder

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import cats.effect.{ExitCode, IO, IOApp}
import com.hivemindtechnologies.feeder.hocon.{AppSettings, HttpSettings}
import com.hivemindtechnologies.feeder.http.HttpHandler
import com.hivemindtechnologies.feeder.producer.MonixKafkaProducer
import com.typesafe.config.ConfigFactory
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import org.http4s.HttpRoutes
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.module.catseffect._
import pureconfig.ConfigSource
import org.http4s.server.middleware._

import scala.concurrent.duration._

object Main extends IOApp {

  implicit val system: ActorSystem = ActorSystem("system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  private def startHttpServer(httpSettings: HttpSettings,
                              service: HttpRoutes[IO]): IO[Unit] = {
    val methodConfig = CORSConfig(
      anyOrigin = true,
      anyMethod = false,
      allowedMethods = Some(Set("GET", "POST")),
      allowCredentials = true,
      maxAge = 1.day.toSeconds
    )

    val app = Router(s"/api" -> CORS(service, methodConfig)).orNotFound
    BlazeServerBuilder[IO]
      .bindHttp(httpSettings.port, httpSettings.hostname)
      .withHttpApp(app)
      .serve
      .compile
      .drain
  }
  override def run(args: List[String]): IO[ExitCode] =
    for {
      app <- ConfigSource.default
        .at("app")
        .loadF[IO, AppSettings]
      logger <- Slf4jLogger.create[IO]

      producerConfig <- IO(
        ConfigFactory.load().getConfig("akka.kafka.producer")
      )
      producer = MonixKafkaProducer
        .fromConfig(producerConfig, app.kafka)

      service = new HttpHandler[IO](logger, producer).routes

      _ <- startHttpServer(app.http, service)
    } yield ExitCode.Success
}
