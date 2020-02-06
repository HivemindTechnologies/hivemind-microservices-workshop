FROM hseeberger/scala-sbt:8u242_1.3.7_2.13.1

RUN apt-get update && \
    apt-get install sudo && \
    apt-get install -y sudo && \
    apt-get install -y libicu57 && \
    apt-get install -y kafkacat && \
    apt-get install -y httpie && \
    apt-get install -y jq

# ARG USERNAME=scala
# ARG USER_UID=1000
# ARG USER_GID=$USER_UID

# Create scala user
# RUN groupadd --gid $USER_GID $USERNAME \
#     && useradd --uid $USER_UID --gid $USER_GID -m $USERNAME \
#     && apt-get install -y sudo \
#     && echo $USERNAME ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/$USERNAME \
#     && chmod 0440 /etc/sudoers.d/$USERNAME

ARG USERNAME=sbtuser
RUN echo $USERNAME ALL=\(root\) NOPASSWD:ALL > /etc/sudoers.d/$USERNAME \
    && chmod 0440 /etc/sudoers.d/$USERNAME

USER $USERNAME

WORKDIR /app
