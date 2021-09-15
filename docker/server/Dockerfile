FROM adoptopenjdk:11-jdk-hotspot

SHELL ["/bin/bash", "-c"]

RUN apt-get update
RUN apt-get -y install curl \
    unzip \
    zip

RUN addgroup --system fibo && adduser --system fibo --ingroup fibo --home /opt/fibo
USER fibo:fibo
RUN mkdir -p /opt/fibo
WORKDIR /opt/fibo

RUN curl -s https://get.sdkman.io | bash
RUN chmod a+x "$HOME/.sdkman/bin/sdkman-init.sh"
RUN source "$HOME/.sdkman/bin/sdkman-init.sh" \
    && sdk env init \
    && echo "'sdkman_auto_answer=false' > .sdkmanrc" \
    && sdk install maven 3.8.2 \
    && sdk default maven 3.8.2
ENV PATH=/opt/fibo/.sdkman/candidates/maven/current/bin:$PATH

CMD ["sh", "init-onto-viewer.sh"]