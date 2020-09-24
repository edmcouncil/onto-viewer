


**

## To run the Onto-Viewer application locally compile from sources:

**

 1. From the previously created "fork" get the code from the master branch.
 2. The structure of the application should contain three folders config-loader, viewer-core and web-app.
	 - viewer-core contains java files of the module with functions that load the ontology and operate on the data contained in the ontology (preparation for display, preparation of search results, etc.)   
	 - the web-app contains the java files of the main module that runs the application. There is also the ontologies directory, which should contain the ontologies selected for display, and the configuration file,  where it is possible to define what is to be displayed in the application.
	 - config-loader contains java files of the configuration loader that are used in other modules, e.g. grouping is specified in the configuration file processed by this module.

 3. Add the selected ontology in the format .ttl, .rdf, .owl in \fibo-viewer\web-app\fiboMapper.

 4. In the \ fibo-viewer\web-app\config\ontology_config.xml configuration file, add a URL between the `<ontologyURL>` `</ontologyURL>` tags (where the ontology will be downloaded from) and add a URI between the `<scopeIri>` `</scopeIri>` tags, which will be treated as an IRI.

 5. Then run the code in any program that provides effective programming tools, e.g. NetBeans, IntelliJ IDEA, etc.

 6. After the project is loaded into the IDE, it is important to rebuild it first and then run it. The program uses Maven, the use of which will download all the necessary libraries, without which the application will not work properly.

 7. The main page of the application is available on the local address with port 8080 (https: // localhost: 8080). After entering this address, the application interface should be displayed.

**

## To run the Onto-Viewer application locally using public release:

**
 1. Download the file "fibo_viewer_relase.zip" from the latest version.
 2. Unpack the file.
 3. Add the selected ontology in .ttl, .rdf, .owl format in the ontologies directory.
 4. In the ontology_config.xml configuration file, add a URL between the <ontologyURL></ontologyURL> tags (where the ontology will be downloaded from) and add a URI between the <scopeIri> </scopeIri> tags, which will be treated as an IRI.
 5. At the operating system command prompt, run the following command in the folder with the latest version:
     **java -jar app-v-LAST_VERSION_NUMBER.war** for example **java -jar app-v-0.1.0.war.**

