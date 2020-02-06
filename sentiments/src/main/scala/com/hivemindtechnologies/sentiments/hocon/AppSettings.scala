package com.hivemindtechnologies.sentiments.hocon

case class SentimentAnalyserSettings(host: String, port: Int, threshold: Double)

case class KafkaSettings(bootstrapServers: String,
                         groupId: String,
                         inputTopic: String,
                         outputTopic: String)

case class AppSettings(kafka: KafkaSettings,
                       sentimentAnalyser: SentimentAnalyserSettings)
