FROM node:16-alpine

# create destination directory
RUN mkdir -p /usr/src/nuxt-app
WORKDIR /usr/src/nuxt-app

# update and install dependency
RUN apk update && apk upgrade
RUN apk add git

# copy the app, note .dockerignore
COPY . /usr/src/nuxt-app/
RUN npm install

# build necessary, even if no static files are needed,
# since it builds the server as well
 RUN npm run build

# expose 3000 on container
EXPOSE 3000

# set app serving to permissive / assigned
ENV PUBLIC_URL=0.0.0.0

# set app port
ENV NUXT_PORT=3000

ENV ONTPUB_FAMILY=dev

# start the app
CMD [ "npm", "run" ,"dev" ]
