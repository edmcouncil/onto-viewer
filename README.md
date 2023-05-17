<img src="https://github.com/edmcouncil/html-pages/raw/develop/general/assets/img/EDM-council-RGB_200w.png" width="200" align="right"/>

# Onto Viewer

Onto Viewer is an open-source project that is hosted by EDM Council. The project started in May 2019. Onto Viewer is a Java application that is specifically designed to access both the ontology structure and its content in the easiest possible way. Onto Viewer servers REST API.

## Onto website
Onto viewer is used in the ontology view in html-pages.

* https://github.com/edmcouncil/html-pages


# How to run Onto Viewer

## Running locally

To run the Onto Viewer locally: 

* Download the file named "onto-viewer.zip" from the [latest release](https://github.com/edmcouncil/onto-viewer/releases). 
* Unzip the file. 
* In the command prompt of your operating system run the following command in the folder with the last release: 

```
java -jar app-v-LAST_VERSION_NUMBER.war
```
e.g.,

```
java -jar app-v-0.1.0.war
```



 ## Running in Docker


To run the application using Docker you have to install Docker and docker-compose on your local computer.  To install Docker see [here](https://docs.docker.com/get-docker/) and to install docker-compose see [here](https://docs.docker.com/compose/install/). 

 In the `onto-viewer/docker/runtime/server/` folder, you must put configuration and ontologies files. You can find samples of these files in the `onto-viewer-config-loader/src/main/resources`. Note that the `onto-viewer/docker/runtime/ `folder is excluded from Git, so you can freely put there any file you want.

Then, from the `onto-viewer/` folder run the following command to start the applications:

```
docker-compose up --build -d
```

Please note that it takes a while to for all services to start depending on how many ontologies you provided.

After all ontologies are loaded, the Onto viewer will be accessible from http://localhost:3000/dev/ontology. 


You can see all running containers and their status:

```
$ docker compose ps

NAME                   IMAGE                COMMAND                  SERVICE             CREATED             STATUS              PORTS
onto-viewer-server-1   onto-viewer-server   "docker-entrypoint.s…"   onto-viewer-server  20 seconds ago      Up 17 seconds       0.0.0.0:8080->6101/tcp
web-with-strapi-1      web-with-strapi      "docker-entrypoint.s…"   web-with-strapi     4 hours ago         Up 17 seconds       0.0.0.0:1337->1337/tcp
```

If you want to see logs from one container use:

```
# to view continuous log output
$ docker logs --follow <container name>

# to view specific amount of logs
$ docker logs --tail <amount> <container name>
```

To stop the applications run:

```
docker-compose down
```

You could also run docker-compose without detached mode(without -d). If so, you'll just use '^C' to kill all containers.


# Contributing
Please read [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) for details on our code of conduct, and the process for submitting pull requests to us.


# Development

To run integration tests, use the following command:

```shell
mvn -P integration-tests verify
```


# License
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)


<!--
 # Release notes

Please read [CHANGELOG.md](CHANGELOG.md) for details.
 -->

