# Actuator

 [Back to README](./README.md)

## Health 
#### /actuator/health (GET)

### Description

Return custom health check with additional information about initialization and update.

Detailed fields description:

- "INITIALIZATION_DONE" - `true` means that the application is initialized and ready to serve data.
- "UPDATE_ONTOLOGY_IN_PROGRESS" - `true` means that ontologies are being updated right now and the application may work incorrectly until the update is done.
- "BLOCKED" - `true` means that the resources are being replaced with new ones and the application won't serve data right now.
- MISSING_IMPORTS

### Example curl (linux)

```
curl --location --request GET '<host_and_port>/actuator/health' \
--header 'Accept: application/json'
```

#### Example Response

```json
{
  "status": "UP",
  "components": {
    "custom": {
      "status": "UP",
      "details": {
        "BLOCKED": false,
        "UPDATE_ONTOLOGY_IN_PROGRESS": false,
        "INITIALIZATION_DONE": true
      }
    }
  }
}
```

## Info

#### /actuator/info (GET)

### Description

Returns detailed information about the version of the application and other related metadata.

### Example curl (linux)

```
curl --location --request GET '<host_and_port>/actuator/info' \
--header 'Accept: application/json'
```

### Example Response

```json
{
  "git": {
    "branch": "develop",  
    "commit": {
      "id": "7e87636",
      "time": "2022-02-04T13:53:19Z"
    }
  },
  "build": {
    "artifact": "onto-viewer-web-app",
    "name": "Onto Viewer Web App",
    "time": "2022-02-01T10:00:00.000Z",
    "version": "0.4.0",
    "group": "org.edmcouncil.spec"
  }
}
```

