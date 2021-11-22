#!/bin/bash

echo "[INFO] Starting 'init-onto-viewer' installer..."

INIT_DIR=$(pwd)
INSTALL_DIR="$INIT_DIR/install_dir"

cp -r src/ "$INSTALL_DIR"
if [ -d "$INSTALL_DIR" ]; then
  cd "$INSTALL_DIR"
  mvn clean package
else
  echo "$INSTALL_DIR hasn't been created. Exiting..."
  exit 1
fi

echo "[INFO] Moving to $INIT_DIR..."
cd "$INIT_DIR"
cp "$INSTALL_DIR"/onto-viewer-web-app/target/onto-viewer-web-app-*.war app.war

echo "[INFO] Starting app.war..."
java -jar app.war --server.servlet.context-path=/fibo/ontology --app.defaultHomePath="$INIT_DIR/app_home"
