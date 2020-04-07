FROM node:13-buster AS builder

COPY . /app

WORKDIR /app

RUN apt update && apt install libncurses5 -y
