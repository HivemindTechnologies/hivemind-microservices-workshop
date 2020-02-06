package com.hivemindtechnologies.sentiments.analyser

import cats.tagless.autoFunctorK
import com.hivemindtechnologies.sentiments.model.Tweet

case class SentimentResult(`match`: Option[Boolean],
                           probability0: Double,
                           probability1: Double)

case class Sentiment(identityAttack: SentimentResult,
                     insult: SentimentResult,
                     obscene: SentimentResult,
                     severeToxicity: SentimentResult,
                     sexualExplicit: SentimentResult,
                     threat: SentimentResult,
                     toxicity: SentimentResult)

case class Analysis(label: String, sentiment: Sentiment)

case class TweetSentiment(timestamp: String, analysis: Analysis)

@autoFunctorK
trait Analyser[F[_]] {
  def analyse(tweet: Tweet): F[Either[Throwable, List[Analysis]]]
}
