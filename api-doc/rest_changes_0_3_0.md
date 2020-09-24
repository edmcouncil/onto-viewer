# Changes in Rest API in version 0_3_0

## Changes in endpoint mapping: 

 - **Hint** 

    - /hint **->** /api/hint
    - /hint/max/**{max}** **->** /api/hint/max/**{max}**

 - **Search Details**

    - /search/json **->**  /api/search

 - **Text Search**

    - /search/json **->** /api/search
	- /search/json/max/**{max}** **->** /api/search/max/**{max}**
	- /search/json/page/**{page}** **->** /api/search/page/**{page}**
	- /search/json/max/**{max}**/page/**{page}**  **->** /api/search/max/**{max}**/page/**{page}**

 - **Modules**

    - /module/json **->** /api/module

- **Update API**

    - /update/ontology **->** /api/update
        - API key required in header 
            > X-API-Key: apiKeyString
        - on get update status required updateId in path
            > /api/update?updateId={UPDATE_ID}
    
    - /update/ontology **->** /api/update?ApiKey=**{ApiKey}**
        - API key required in path
            > /api/update?ApiKey={API_KEY}
        - on get update status required updateId in path
            > /api/update?ApiKey={API_KEY}&updateId={UPDATE_ID}
    
    - request type
        - Add a new update request to the queue: **POST** -> **PUT** 
    - the new value in the status field in the returned json
        > INTERRUPT_IN_PROGRESS



