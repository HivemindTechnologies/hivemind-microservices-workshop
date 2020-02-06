package com.hivemindtechnologies.feeder.hocon

case class HttpSettings(hostname: String, port: Int)

case class KafkaSettings(bootstrapServers: String,
                         groupId: String,
                         outputTopic: String)

case class AppSettings(kafka: KafkaSettings, http: HttpSettings)
