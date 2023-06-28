#!/bin/bash

echo "[INFO] Starting app.war..."
exec java -server -Xmx4G -XX:-UseGCOverheadLimit -Dserver.address=0.0.0.0 -Dserver.port=80 -Dserver.forward-headers-strategy=native -Dserver.tomcat.redirect-context-root=false \
	${UPDATE_URL:+ -Dapp.config.ontologies.download_directory=download -Dapp.config.updateUrl=${UPDATE_URL}} -jar onto-viewer.war --server.servlet.context-path=/${ONTPUB_FAMILY:-dev}/ontology
