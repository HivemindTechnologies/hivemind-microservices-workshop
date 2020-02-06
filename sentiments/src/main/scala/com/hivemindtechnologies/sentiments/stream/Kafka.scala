package com.hivemindtechnologies.sentiments.stream

import cats.tagless.autoFunctorK

case class Topic(unwrap: String)
case class Key(unwrap: String)
case class Content(unwrap: String)

@autoFunctorK
trait Kafka[F[_]] {
  def push(topic: Topic, key: Key, value: Content): F[Unit]
  def pull(topic: Topic): F[String]
}
