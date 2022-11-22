# Stats

[Back to README](./README.md)

### /api/stats (GET)

### Description

Return stats of ontologies read in the application.

 ### Example curl (linux)
  
```
curl --location --request GET '<host_and_port>/api/stats' \
--header 'Accept: application/json'
```

### Example Response

```json
{
   "stats":{
      "@viewer.stats.numberOfDomain":10,
      "@viewer.stats.numberOfModule":9,
      "@viewer.stats.numberOfClass":2186,
      "@viewer.stats.numberOfObjectProperty":995,
      "@viewer.stats.numberOfDataProperty":247,
      "@viewer.stats.numberOfAnnotationProperty":106,
      "@viewer.stats.numberOfIndividuals":1867,
      "@viewer.stats.numberOfAxiom":39656,
      "@viewer.stats.numberOfDatatype":21,
      "@viewer.stats.numberOfOntologies":146
   },
   "labels":{
      "@viewer.stats.numberOfDomain":"number of domains",
      "@viewer.stats.numberOfModule":"number of modules",
      "@viewer.stats.numberOfClass":"number of classes",
      "@viewer.stats.numberOfObjectProperty":"number of object properties",
      "@viewer.stats.numberOfDataProperty":"number of data properties",
      "@viewer.stats.numberOfAnnotationProperty":"number of annotation properties",
      "@viewer.stats.numberOfIndividuals":"number of individuals",
      "@viewer.stats.numberOfAxiom":"number of axioms",
      "@viewer.stats.numberOfDatatype":"number of datatypes",
      "@viewer.stats.numberOfOntologies":"number of ontologies"
   }
}
```
