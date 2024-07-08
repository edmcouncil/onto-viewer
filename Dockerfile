ARG	ONTPUB_FAMILY=dev
ARG	UPDATE_URL

FROM	maven:3-eclipse-temurin-21-alpine AS build-onto-viewer
RUN	apk --no-cache add --upgrade bash curl unzip zip && \
	mkdir -p /opt/develop
WORKDIR /opt/develop
COPY .	/opt/develop/
RUN	mvn -ntp clean package && cp -av onto-viewer-web-app/target/onto-viewer-web-app-*.war /onto-viewer-web-app.war

FROM	eclipse-temurin:21-jdk-alpine
ARG	ONTPUB_FAMILY UPDATE_URL
ENV	ONTPUB_FAMILY="${ONTPUB_FAMILY}" UPDATE_URL="${UPDATE_URL}"
RUN	apk --no-cache add --upgrade bash curl jq && \
	install -d /opt/viewer/config
WORKDIR	/opt/viewer
COPY	./onto-viewer-web-app/entrypoint.sh			/opt/viewer/entrypoint.sh
COPY	./onto-viewer-web-app/integration_tests/ontologies	/opt/viewer/ontologies
COPY	--from=build-onto-viewer /onto-viewer-web-app.war	/opt/viewer/onto-viewer.war
CMD	["sh", "entrypoint.sh"]
HEALTHCHECK --start-period=60s --interval=15s --timeout=1s --retries=60 CMD test "$(curl -q http://localhost/${ONTPUB_FAMILY}/ontology/actuator/health | jq -e ".components.custom.details.INITIALIZATION_DONE")" = "true"