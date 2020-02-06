#!/usr/bin/env bash

curl -X POST -H "Content-Type: application/json" \
http://kafka-connect:8083/connectors \
  -d '{
 "name": "tweets-to-es",
  "config": {
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "tasks.max": "1",
    "topics": "tweets",
    "key.ignore": "true",
    "topic.schema.ignore":"true",
    "schema.ignore":"true",
    "connection.url": "http://elasticsearch:9200",
    "type.name": "kafka-connect",
    "name": "tweets-to-es"
  }
}'

curl -X POST -H "Content-Type: application/json" \
http://kafka-connect:8083/connectors \
  -d '{
 "name": "sentiments-to-es",
  "config": {
    "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
    "tasks.max": "1",
    "topics": "sentiments",
    "key.ignore": "true",
    "topic.schema.ignore":"true",
    "schema.ignore":"true",
    "connection.url": "http://elasticsearch:9200",
    "type.name": "kafka-connect",
    "name": "sentiments-to-es"
  }
}'