FROM hseeberger/scala-sbt:11.0.6_1.3.9_2.13.1

RUN apt-get update && \
    apt-get install sudo && \
    apt-get install -y sudo && \
    apt-get install -y libicu57 && \
    apt-get install -y kafkacat && \
    apt-get install -y httpie && \
    apt-get install -y jq

ARG USERNAME=sbtuser
RUN echo $USERNAME ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/$USERNAME \
    && chmod 0440 /etc/sudoers.d/$USERNAME

USER $USERNAME

WORKDIR /app
