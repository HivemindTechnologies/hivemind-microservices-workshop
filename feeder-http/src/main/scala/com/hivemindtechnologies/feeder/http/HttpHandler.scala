package com.hivemindtechnologies.feeder.http

import cats.effect.Sync
import cats.implicits._
import com.hivemindtechnologies.feeder.model.Tweet
import com.hivemindtechnologies.feeder.producer.{Content, Key, Producer}
import io.chrisdavenport.log4cats.Logger
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.`Content-Type`
import io.circe.syntax._

class HttpHandler[F[_]](logger: Logger[F], producer: Producer[F])(
  implicit S: Sync[F]
) extends Http4sDsl[F] {

  implicit private val jsonDecoder: EntityDecoder[F, Tweet] = jsonOf

  val mkJson: Response[F] => Response[F] =
    _.withContentType(`Content-Type`(MediaType.application.json))

  val routes: HttpRoutes[F] = HttpRoutes.of {
    {
      case req @ POST -> Root / "tweet" =>
        for {
          _ <- logger.info("Received POST tweet")
          tweet <- req.as[Tweet]
          _ <- producer.push(Key("feeder"), Content(tweet.asJson.noSpaces))
          resp <- Ok("""{"status":"done"}""").map(mkJson)
        } yield resp

      case GET -> Root / "health" =>
        logger.info("Received GET health") *> Ok("""{"status":"healthy"}""")
          .map(mkJson)

    }
  }

}
