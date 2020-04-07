FROM hseeberger/scala-sbt:11.0.6_1.3.9_2.13.1 as builder

COPY . /app

WORKDIR /app

RUN sbt assembly

FROM adoptopenjdk/openjdk11:alpine-jre

COPY --from=builder /app/target /app

WORKDIR /app

ENTRYPOINT [ "java" , "-jar", "sentiments.jar"]