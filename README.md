<img src="https://github.com/edmcouncil/html-pages/raw/develop/general/assets/img/EDM-council-RGB_200w.png" width="200" align="right"/>

# onto-viewer

onto-viewer is an open-source Java application that provides a number of REST API endpoints to access the content of OWL ontologies. If run together with [html-pages frontend](https://github.com/edmcouncil/html-pages), it will visualise them as a web application.



# How to run onto-viewer

## How to customise your ontology to be properly displayed by onto-viewer

1. We require that all ontologies' IRIs end with '/' (e.g., https://www.omg.org/spec/Commons/Classifiers/) as per the recommendation from https://www.w3.org/TR/cooluris/.
1. We require that all ontologies are "registered" in the meta file(s), which store the relevant metadata, in particular allow for custom modularisation - see https://github.com/edmcouncil/idmp/blob/master/ISO/MetadataISO.rdf for an example of such file.
1. We recommend that all imported ontologies and locally cached and their local references are listed in catalog-v001.xml file. 

## Running locally

To run the onto-viewer locally: 

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


## Run with docker
Requirements:
- [git](https://git-scm.com/) ([install](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git))
- [docker](https://www.docker.com/) - install:
  * [Docker Desktop](https://docs.docker.com/desktop/) or ...
  * [Docker Engine](https://docs.docker.com/engine/) with [Docker Compose plugin](https://docs.docker.com/compose/)

### ... with FIBO

How to start:
Clone the [edmcouncil/onto-viewer](https://github.com/edmcouncil/onto-viewer) repository to the *onto-viewer* directory,
go to the *onto-viewer* directory (run all subsequent commands inside this directory),
then build the images (or pull from the registry if available) and run the containers:
```bash
# clone the repository
git clone https://github.com/edmcouncil/onto-viewer onto-viewer

# got to the onto-viewer directory
cd onto-viewer

# build images
docker compose build
# alternatively pull images from registry if available
#docker compose pull --ignore-pull-failures

# run the containers
docker compose up -d
```

After some time, check the status of running containers:
```
docker compose ps
```

if they work correctly, the following message will appear:
```
NAME                        IMAGE                           COMMAND                  SERVICE             CREATED             STATUS                   PORTS
onto-viewer-fibo-pages-1    edmcouncil/fibo-pages:latest    "docker-entrypoint.s…"   fibo-pages          7 minutes ago       Up 6 minutes (healthy)   
onto-viewer-fibo-strapi-1   edmcouncil/fibo-strapi:latest   "docker-entrypoint.s…"   fibo-strapi         7 minutes ago       Up 6 minutes (healthy)   
onto-viewer-fibo-viewer-1   edmcouncil/onto-viewer:latest   "sh entrypoint.sh"       fibo-viewer         7 minutes ago       Up 6 minutes (healthy)   
onto-viewer-spec-1          edmcouncil/spec:latest          "/docker-entrypoint.…"   spec                7 minutes ago       Up 6 minutes (healthy)   0.0.0.0:8080->80/tcp, :::8080->80/tcp

```

The services provide endpoints at the following URLs:
- [http://localhost:8080](http://localhost:8080) :- [html-pages home page](https://github.com/edmcouncil/html-pages/blob/develop/home/README.md)
- [http://localhost:8080/fibo](http://localhost:8080/fibo) :- [html-pages general template](https://github.com/edmcouncil/html-pages/tree/develop/general) for [FIBO](https://github.com/edmcouncil/fibo) ontology
- [http://localhost:8080/fibo/ontology](http://localhost:8080/fibo/ontology) :- onto-viewer for [FIBO](https://github.com/edmcouncil/fibo) ontology
- [http://localhost:8080/fibo/strapi/admin](http://localhost:8080/fibo/strapi/admin) :- [Strapi admin panel](https://docs.strapi.io/user-docs/intro#accessing-the-admin-panel) for for [FIBO](https://github.com/edmcouncil/fibo) ontology (Email: *edmc-strapi@dev.com*, Password: *devDBonly1*)

It is also possible to build (or pull from the registry, if available) Docker images
with tag names other than the default `develop` - use environment variables:
- `VIEWER_BRANCH` for `edmcouncil/onto-viewer`, e.g. for [edmcouncil/onto-viewer:build-dev](https://github.com/edmcouncil/onto-viewer/tree/build-dev):
  ```bash
  echo VIEWER_BRANCH=build-dev >> .env
  ```
- `HTML_BRANCH` for `edmcouncil/html-pages`, e.g. for [edmcouncil/html-pages:build-dev](https://github.com/edmcouncil/html-pages/tree/build-dev):
  ```bash
  echo HTML_BRANCH=build-dev >> .env
  ```

### ... with an ontology of your choice

It is possible to run containers with any ontology (instead of `FIBO`):
- place the ontology files of your choice in the `onto-viewer-web-app/ontologies` subdirectory
  and the config files in the `onto-viewer-web-app/config` subdirectory

- using the `docker-compose.dev.yaml` compose file (instead of the default `docker-compose.yaml`),
  build the images,then run the containers:
  ```bash
  echo COMPOSE_FILE=docker-compose.dev.yaml >> .env
  docker compose build
  docker compose up -d
  ```

  once all services are up and running, onto-viewer with your ontology will be available at `http://localhost:8080/dev/ontology`

If you want to see the logs use:
```bash
# to view continuous log output for <SERVICE>=dev-viewer
docker compose logs --follow dev-viewer

# to view *100* latest log lines for <SERVICE>=dev-viewer
docker compose logs --tail 100 dev-viewer
```

Stop the services with the command:
```bash
docker compose down
```

Remove all images and volumes with the command:
```bash
docker compose down --rmi all -v
```

# Contributing
Please read [CONTRIBUTING.md](CONTRIBUTING.md) and [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) for details on our code of conduct, and the process for submitting pull requests to us.


# Development

To run integration tests, use the following command:

```shell
mvn -P integration-tests verify
```


# License
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

