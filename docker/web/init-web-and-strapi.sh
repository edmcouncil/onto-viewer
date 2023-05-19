#!/bin/bash
echo "[INFO] Clone html pages"
git clone https://github.com/edmcouncil/html-pages.git

cd /opt/html-pages/general/strapi

echo "[INFO] Install strapi without run."
npx create-strapi-app@latest strapi-dashboard --quickstart --no-run

echo "[INFO] Copy database."
mkdir ./strapi-dashboard/.tmp
cp ./.tmp/${ONTPUB_FAMILY:-data}.db ./strapi-dashboard/.tmp/data.db

echo "[INFO] Copy structures."
cp -R ./src/ ./strapi-dashboard

echo "[INFO] Run Strapi."
cd strapi-dashboard
npm run develop &

echo "[INFO] Install and run general web pages"
cd /opt/html-pages/general
rm nuxt.config.js
cp ../../nuxt.config.js .
npm install
npm run build
npm run dev
