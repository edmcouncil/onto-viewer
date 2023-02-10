<img src="https://spec.edmcouncil.org/fibo/htmlpages/develop/latest/img/logo.66a988fe.png" width="150" align="right"/>

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

Assuming you have installed both Docker and docker-compose, to run the Onto Viewer application together with its front-end on your local machine, please make sure that you have the following folder setup:

```
+---onto-viewer
|   |   .gitignore
|   |   CHANGELOG.md
|   |   CODE_OF_CONDUCT.md
|   |   CONTRIBUTING.md
|   |   DCO
|   |   docker-compose.yaml
|   |   LICENSE
|   |   pom.xml
|   |   README.md   
|   +---api-doc
|   +---docker
|   |   |   init-onto-viewer.sh
|   |   +---runtime
|   |   |   \---server  
|   |   |       +---config
|   |   |       |       application_config.yaml
|   |   |       |       groups_config.yaml
|   |   |       |       label_config.yaml
|   |   |       |       ontology_config.yaml
|   |   |       |       search_config.yaml     
|   |   |       |       
|   |   |       \---ontologies
|   |   +---server
|   |   |       Dockerfile      
|   |   \---web
|   |           nuxt.config.js         
|   +---onto-viewer-config-loader
|   +---onto-viewer-core
|   +---onto-viewer-toolkit
|   +---onto-viewer-web-app
|   \---style          
+---onto-viewer-web
|   |   .dockerignore
|   |   ...
|   |   Dockerfile
|   |   jsconfig.json
|   |   nuxt.config.js
|   |   ...
|   +---api
|	...
|   +---store
|   \---strapi
\---strapi-dashboard
    |   .dockerignore
    |	...
    |   Dockerfile
    |   favicon.png
    |   package-lock.json
    |   package.json
    |   README.md
    |   yarn.lock
    +---.tmp    
    +---config
    +---database
    +---public
    \---src
```
where `root-dir` is any folder on your local machine.  In the `onto-viewer/docker/runtime/server/` folder, you must put configuration and ontologies files.  You can find samples of these files in the [onto-viewer-config-loader/src/main/resources](https://github.com/edmcouncil/onto-viewer/tree/update-readme-and-docker-files/onto-viewer-config-loader/src/main/resources).  Note that the `onto-viewer/docker/runtime/` folder is excluded from Git, so you can freely put there any file you want.

Install Strapi with the development database in the `strapi-dashboard` directory and the general folder in `onto-viewer-web` according to the instructions [README.md](https://github.com/edmcouncil/html-pages/blob/develop/general/README.md). Configure Strapi without running it.
Replace `nuxt.config.js` from `onto-viewer-web` to `\onto-viewer\docker\web`.

Then, from the `onto-viewer/` folder run the following command to start the applications:

```
docker-compose up --build -d
```
To list applications that have started, use ```docker-compose ps```.

Please note that it takes a while to for all services to start depending on how many ontologies you provided.

After all ontologies are loaded, the Onto viewer will be accessible from http://localhost:3000/dev/ontology. 

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


<!--
 # Release notes

Please read [CHANGELOG.md](CHANGELOG.md) for details.
 -->

