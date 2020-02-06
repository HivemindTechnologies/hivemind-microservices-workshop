FROM hseeberger/scala-sbt:8u242_1.3.7_2.13.1 as builder

COPY . /app

WORKDIR /app

RUN sbt assembly

FROM openjdk:8-jre-alpine

COPY --from=builder /app/target /app

WORKDIR /app

ENTRYPOINT [ "java" , "-jar", "sentiments.jar"]