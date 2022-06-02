# Logs

 [Back to README](./README.md)

#### /api/logs (GET)

#### Description

- When the application is run, a folder "logs" is created in onto-viewer-web-app.
- This folder contains the "viewer-logs" file with the current logs.
- Each day a new log file is generated. The previous day's file goes into the "archiverd" folder and is renamed to "viewer-logs-yyyy-mm-dd".
- If the current "viewer-logs" file has more than 1GB of data, a new file will be generated.
- To get the logs, make a REST request, e.g: 
<host_and_port>/api/logs?date=2022-01-21

## API key required in header
#### /api/logs (GET)

## API key required in path

#### /api/logs?ApiKey={API_KEY} (GET)

#### Example Response
```
[
2022-01-21 15:54:34,599 INFO org.edmcouncil.spec.ontoviewer.webapp.controller.HintController [http-nio-8080-exec-10] For hints: 'mon' (query time: '46' ms) result is:

[{iri=https://www.omg.org/spec/LCC/Languages/ISO639-2-LanguageCodes/mon, label=mon, relevancy=10.0}, {iri=https://www.omg.org/spec/LCC/Languages/ISO639-2-LanguageCodes/Hmong, label=Hmong, relevancy=6.0}, {iri=https://www.omg.org/spec/LCC/Languages/ISO639-2-LanguageCodes/Mongo, label=Mongo, relevancy=6.0},
]
```