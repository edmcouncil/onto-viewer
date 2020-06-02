

## 1. Hint

 - **Request type:** POST
 - **Endpoint**:
	 - /hint 
	 - /hint/max/**{max}**
 - **Description**: Return hint list for given text 
 - **Data**: 
	 - ***Request body  (required)*** – to this text will be returned hints, 
	 - **max (optional)** – max hint count, default is set to 20 
 - **Returned value(json)**:

>  [{   
> 	 "iri": "https://www (...) Codes-BA/Entity",   
> 	 "label": "entity",   "relevancy": 6.6 },
> 	  (…) ]


   ## 2. Search Details

 - **Request type:** POST
 - **Endpoint**:
	 - /search/json 
 - **Description**: Return a details about resource from given iri
 - **Data**: 
	 - ***Request body (required)*** – resource iri 
 - **Returned value(json)**:

> {  "type": "details",
>  "result": { "label": "FinancialContextAndProcess", 
>  "iri": "https://spec.edmcouncil.org/fibo/ontology/BP/Process/FinancialContextAndProcess/",
> "type": null, 
> "qName": "QName: NONE:", 
> "taxonomy": [**(...)**],
> "locationInModules":[
> "https://spec.edmcouncil.org/fibo/ontology/BP/MetadataBP/BPDomain",
> "https://spec.edmcouncil.org/fibo/ontology/BP/Process/MetadataBPProcess/ProcessModule",
> "https://spec.edmcouncil.org/fibo/ontology/BP/Process/FinancialContextAndProcess/"],
> "graph": null, 
> "maturityLevel": { "label": "provisional", "iri":"https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/Provisional"},  
> "properties": { **(…)** } 
> }
>  }

## 3. Text search

- **Request type:** POST
 - **Endpoint**:
	 - /search/json
	 - /json/max/**{max}**
	 - /json/page/**{page}**
	 - /json/max/**{max}**/page/**{page}** 
 - **Description**: Return a search results as list of sorted elements for given text from request body 
 - **Data**: 
	 - **Request body (required)** – text
	 - **max (optional)** – max result count, default 100
	 - **page (optional)** – page numer, default 1
 - **Returned value(json)**:

> {
> **"type": "list",**
> **"result":** [{ "iri": "https://spec **(...)** /LegalRight", 
> "label": "legal right", 
> "description": "a contingent right or privilege **(…)** statutes, regulations,...", 
> "relevancy": 4.546 },  
> **(…)**  ],
> "**query**": "swap", 
> "**page**": 1, 
> "**hasMore**": true,
> "**maxPage**": 15 }


## 4. Modules

- **Request type:** GET
 - **Endpoint**:
	 - /module/json
 - **Description**: Return a list of Fibo modules and all submodules from fibo ontology 
 - **Data**: 
	 - none
 - **Returned value(json)**:

> [{   "**iri**": "https://spec.edmcouncil.org/fibo/ontology/BE/MetadataBE/BEDomain",  
> "**label**": "Business Entities",   
> "**subModule**": [(next list of modules..)],
>     "**maturityLevel**": {
>       "label": "prod"
>     }   },
>        (...)   ]

