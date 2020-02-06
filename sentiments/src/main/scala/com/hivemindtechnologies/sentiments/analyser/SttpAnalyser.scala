package com.hivemindtechnologies.sentiments.analyser

import cats.Monad
import cats.implicits._
import com.hivemindtechnologies.sentiments.hocon.SentimentAnalyserSettings
import com.hivemindtechnologies.sentiments.model.Tweet
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import sttp.client.asynchttpclient.WebSocketHandler
import sttp.client.{SttpBackend, _}
import sttp.model.Uri

private[analyser] case class SentimentRequest(threshold: Double,
                                              sentences: List[String])

class SttpAnalyser[F[_]: Monad](uri: Uri, threshold: Double)(
  implicit backend: SttpBackend[F, Nothing, WebSocketHandler]
) extends Analyser[F] {

  override def analyse(tweet: Tweet): F[Either[Throwable, List[Analysis]]] = {

    val requestJson =
      SentimentRequest(threshold, List(tweet.content)).asJson.noSpaces

    val responseF = basicRequest
      .body(requestJson)
      .contentType("application/json")
      .post(uri.path("toxicity"))
      .send()

    responseF
      .map(_.body)
      .map(body => 
        body
          .leftMap(new Throwable(_))
          .flatMap(
            json => decode[List[Analysis]](json).leftMap(_.fillInStackTrace())
          )
      )

  }
}

object SttpAnalyser {

  def fromConfig[F[_]: Monad](config: SentimentAnalyserSettings)(
    implicit backend: SttpBackend[F, Nothing, WebSocketHandler]
  ): Either[Throwable, SttpAnalyser[F]] =
    Uri
      .safeApply(config.host, config.port)
      .bimap(
        new Throwable(_),
        uri => new SttpAnalyser[F](uri, config.threshold)
      )
}
