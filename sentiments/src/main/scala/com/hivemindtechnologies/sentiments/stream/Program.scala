package com.hivemindtechnologies.sentiments.stream

import cats.data.EitherT
import cats.{Applicative, Monad}
import com.hivemindtechnologies.sentiments.analyser.{Analyser, Analysis}
import com.hivemindtechnologies.sentiments.hocon.KafkaSettings
import io.chrisdavenport.log4cats.Logger
import cats.implicits._
import com.hivemindtechnologies.sentiments.model.Tweet
import io.circe.generic.auto._
import io.circe.parser._
import cats.implicits._
import io.circe.syntax._
import com.hivemindtechnologies.sentiments.analyser.TweetSentiment

object Program {

  def run[F[_]: Monad: Applicative](kafkaSettings: KafkaSettings,
                                    kafka: Kafka[F],
                                    analyser: Analyser[F],
                                    logger: Logger[F]): F[Unit] =
    for {
      msg <- kafka.pull(Topic(kafkaSettings.inputTopic))
      _ <- logger.info("Received message")
      result <- processMessage(kafkaSettings, kafka, analyser, msg)
      _ <- result match {
        case Left(err) => logger.error(err)(s"Failed to process message.")
        case Right(_)  => logger.info("Successfully processed messages")
      }
    } yield ()

  private def processMessage[F[_]: Monad: Applicative](
    kafkaSettings: KafkaSettings,
    kafka: Kafka[F],
    analyser: Analyser[F],
    msg: String
  ): F[Either[Throwable, List[Analysis]]] = {
    val eitherT = for {
      tweet <- EitherT.fromEither[F](
        decode[Tweet](msg).leftMap(_.fillInStackTrace())
      )
      analyses <- EitherT(analyser.analyse(tweet))
      _ <- EitherT.right[Throwable](
        analyses.traverse(
          analysis =>
            kafka.push(
              Topic(kafkaSettings.outputTopic),
              Key("sentiments"),
              Content(TweetSentiment(timestamp = tweet.timestamp, analysis = analysis).asJson.noSpaces)
          )
        )
      )
    } yield analyses
    eitherT.value
  }
}
