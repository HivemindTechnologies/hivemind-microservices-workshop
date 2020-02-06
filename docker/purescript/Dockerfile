FROM node:12-buster AS builder

COPY . /app

WORKDIR /app

RUN apt update && apt install libncurses5 -y
