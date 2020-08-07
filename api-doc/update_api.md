
## 1. Start ontology update

 - **Request type:** POST
 - **Header:** Content-Type: application/json
 - **Endpoint**:
	 - /update/ontology 
 - **Description**: Will create an update task and return information about it
 - **Data**: 
	 - ***Request body  (required)*** :
		> {"apiKey":"API_KEY"} 
		
 - **Returned value(json)**:

	>{
	 > "id": "0",
	 > "status": "IN_PROGRESS",
	  >"msg": "",
	  >"startTimestamp": 1594627080217
	>}

## 2. Get ontology update status

 - **Request type:** GET 
 - **Header:** Content-Type: application/json
 - **Endpoint**:
	 - /update/ontology 
 - **Description**: Return ontology update status for given update id 
 - **Data**: 
	 - ***Request body  (required)*** :
		> {"apiKey":"API_KEY", "updateId":"0"} 
		
 - **Returned value(json)**:

	>{
	 > "id": "0",
	 > "status": "DONE",
	  >"msg": "",
	  >"startTimestamp": 1594627080217
	>}
	
- **Possible status field values:**
	- WAITING, 
	- CREATED, 
	- IN_PROGRESS, 
	- DONE, 
	- ERROR

