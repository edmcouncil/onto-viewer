FROM node:16-alpine
# Installing libvips-dev for sharp Compatibility
RUN apk update && apk add --no-cache build-base gcc autoconf automake zlib-dev libpng-dev nasm bash vips-dev git
ARG NODE_ENV=development
ENV NODE_ENV=${NODE_ENV}
WORKDIR /opt/
COPY ./ .
EXPOSE 1337
EXPOSE 3000
CMD ["sh", "init-web-and-strapi.sh"]
# ENV HOST=0.0.0.0

