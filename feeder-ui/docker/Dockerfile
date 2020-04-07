FROM node:13-buster

COPY . /app

WORKDIR /app

RUN apt update && apt install libncurses5 -y

RUN rm -rf node_modules/ && \
    npm install 

RUN npx spago bundle-app --to output/Main.js && \
    npx parcel build -d public --bundle-node-modules output/Main.js index.html styles.css

ENTRYPOINT [ "npx" , "http-server", "-p", "9000"]
