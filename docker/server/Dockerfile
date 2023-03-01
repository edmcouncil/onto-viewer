FROM maven:3.8.1-jdk-11-openj9

SHELL ["/bin/bash", "-c"]

RUN apt-get update
RUN apt-get -y install curl \
    unzip \
    zip

RUN mkdir -p /opt/develop
WORKDIR /opt/develop

CMD ["sh", "init-onto-viewer.sh"]