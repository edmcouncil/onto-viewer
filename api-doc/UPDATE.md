# Update

 [Back to README](./README.md)

## Add a new update request to the queue 

### /api/update?ApiKey={API_KEY} (PUT)

### Description 
Add a new update request to the queue and return information about it. Request should provide an API key through a request param (ApiKey) or a request header (X-API-KEY).

 ### Example curl (linux)
  
```
curl --location --request PUT '<host_and_port>/ontology/api/update' \
--header 'Accept: application/json' \
--header 'X-API-Key: <api-key>'
```
  
```
curl --location --request PUT '<host_and_port>/api/update/?ApiKey=<api-key>' \
--header 'Accept: application/json' 
```

### Example Response

```json
{
   "id":"0",
   "status":"IN_PROGRESS",
   "msg":"",
   "startTimestamp":1594627080217
}

```

## Get update status

#### /api/update/<updateId,optional>?ApiKey=<apiKey,optional> (GET)

### Description

Returns an update status for the given `updateId`.  If the `updateId` is not present, the application returns the last
update object.  Request should provide an API key through a request param (`ApiKey`) or a request header (`X-API-KEY`).
Response returns one of the following statuses:

- WAITING
- CREATED
- IN_PROGRESS
- DONE
- ERROR
- INTERRUPT_IN_PROGRESS


 ### Example curl (linux)
  
```
curl --location --request GET '<host_and_port>' \
--header 'Accept: application/json' \
--header 'X-API-Key: <api-key>'
```

### Example Response

```json
{
    "id": "0",
    "status": "DONE",
    "msg": "",
    "startTimestamp": 1647849932026
}
```


#### /api/update?ApiKey=<apiKey,optional> (PUT)

### Description

Starts an update of the application, i.e. the application reloads ontologies.

 ### Example curl (linux)
  
```
curl --location --request PUT '<host_and_port>/api' \
--header 'Accept: application/json' \
--header 'X-API-Key: <api-key>'
```

### Example Response

```json
{
    "id": "1",
    "status": "IN_PROGRESS",
    "msg": "",
    "startTimestamp": 1647855626457
}
```
