package com.hivemindtechnologies.feeder.producer

case class Topic(unwrap: String)
case class Key(unwrap: String)
case class Content(unwrap: String)

trait Producer[F[_]] {
  def push(key: Key, value: Content): F[Either[Throwable, Unit]]
}
