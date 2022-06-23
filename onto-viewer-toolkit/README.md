# OntoViewer Toolkit

## Goals

OntoViewer Toolkit can be run with a few different goals that are described below.  For each goal, there are a few relevant arguments that can be passed to control the way a goal is run.  Some of these arguments are required, some of them are optional.   

| Name                | Description                                                                                                                                                                                                                                                              | Relevant argument                                                                                  |
|---------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|
| `consistency-check` | Check whether provided ontologies are consistent. The result of this goal is written to an `output` file with one of two values: `true`, `false`.                                                                                                                        | `data` (required), `output` (required)                                                             |
| `extract-data`      | Create a CSV file with data from provided ontologies. This is the default goal. `filter-pattern` is used to filter out all entities that doesn't contain the given pattern within their IRIs.  `ontology-mapping` is a file created in the standard XML Catalogs format. | `data` (required), `output` (required), `filter-pattern` (optional), `ontology-mapping` (optional) |
| `merge-imports`    | Merge ontologies with their imports and save them in RDF/XML format.  `ontology-iri` will be used as a new IRI for the merged ontology.                                                                                                                                  | `data` (required), `output` (required), `ontology-iri` (required), `ontology-mapping` (optional)   |


### Examples

#### `consistency-check`

```bash
java -jar onto-viewer-toolkit.jar \
    --goal consitency-check \
    --data my-ontology.rdf \
    --output consitency-check-result.txt
```


#### `extract-data`

```bash
java -jar onto-viewer-toolkit.jar \
    --goal extract-data \
    --data my-ontology1.rdf \
    --data my-ontology2.rdf \
    --filter-pattern foo \
    --ontology-mapping catalog-v001.xml \
    --output extracted-data.csv
```


#### `merge-imports`

```bash
java -jar onto-viewer-toolkit.jar \
    --goal merge-imports \
    --data my-ontology.rdf \
    --ontology-iri 'http://example.com/my-merged-ontology/' \
    --ontology-mapping catalog-v001.xml \
    --output my-merged-ontology.rdf
```