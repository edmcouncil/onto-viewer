<img src="https://spec.edmcouncil.org/fibo/htmlpages/develop/latest/img/logo.66a988fe.png" width="150" align="right"/>

# FIBO Viewer

FIBO Viewer is an open-source project that is hosted by EDM Council. The project started in May 2019. FIBO Viewer is a Java application that is specifically designed to access both the FIBO structure and its content in the easiest possible way. FIBO Viewer servers both as a web application and REST API.


## FIBO website
FIBO Viewer is integrated with FIBO website. It resolves the FIBO IRIs. See e.g.:

* https://spec.edmcouncil.org/fibo/ontology/BE/LegalEntities/LegalPersons/LegalEntity


# How to run FIBO Viewer

## Running locally

To run the FIBO Viewer locally: 

* Download the file named "fibo\_viewer\_relase.zip" from the [latest release](https://github.com/edmcouncil/fibo-viewer/releases). 
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

Assuming you have installed both Docker and docker-compose, to run the FIBO Viewer application together with its front-end on your local machine, please make sure that you have the following folder setup:

```
root-dir/
    onto-viewer/                 (https://github.com/edmcouncil/onto-viewer)
        docker/
            runtime/
                server/
                    config/      <- config files go here
                        ...
                    ontologies/  <- ontologies go here
                        ...
        docker-compose.yaml
        ...
    html-pages/                  (https://github.com/edmcouncil/html-pages)
        ...
```

where `root-dir` is any folder on your local machine.  In the `onto-viewer/docker/runtime/server/` folder, you must put configuration and ontologies files.  You can find samples of these files in the `onto-viewer/onto-viewer-web-app` folder.  Note that the `onto-viewer/docker/runtime/` folder is excluded from Git, so you can freely put there any file you want.

Then, from the `onto-viewer/` folder run the following command to start the applications:

```
docker-compose up -d
```
To list applications that have started, use ```docker-compose ps```.

Please note that it takes a while to for all services to start depending on how many ontologies you provided.

After all ontologies are loaded, the FIBO viewer will be accessible from http://localhost:6201/fibo/ontology. To see an alternative view that is served by the module [onto-viewer-web-app](https://github.com/edmcouncil/onto-viewer/tree/develop/onto-viewer-web-app) (and not by [html-pages app](https://github.com/edmcouncil/html-pages)) go to http://localhost:6101/fibo/ontology.

To stop the applications run:

```
docker-compose down
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


# Release notes

Please read [CHANGELOG.md](CHANGELOG.md) for details.
