# Missing Imports

[Back to README](./README.md)

###  /api/missingImports (GET)

### Description

Return the iri and the cause of the unimported ontology.

 ### Example curl (linux)
  
```
curl --location --request GET '<host_and_port/api/missingImports' \
--header 'Accept: application/json'
```

### Example Response

```json
[
   {
   "iri": "https://www.omg.org/spec/LCC/Countries/ISO3166-1-CountryCodes/",
   "cause": "OWLOntologyCreationIOException: java.io.FileNotFoundException: https://www.omg.org/spec/LCC/Countries/ISO3166-1-CountryCodes/"
   },
   {
   "iri": "https://www.omg.org/spec/LCC/Languages/ISO639-1-LanguageCodes/",
   "cause": "OWLOntologyCreationIOException: java.io.FileNotFoundException: https://www.omg.org/spec/LCC/Languages/ISO639-1-LanguageCodes/"
   }
]
```