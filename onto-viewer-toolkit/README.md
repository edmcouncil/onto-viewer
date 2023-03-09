# OntoViewer Toolkit

## Goals

OntoViewer Toolkit can be run with a few different goals that are described below.  For each goal, there are a few relevant arguments that can be passed to control the way a goal is run.  Some of these arguments are required, some of them are optional.   

| Name                | Description                                                                                                                                                                                                                                                                                        | Relevant argument                                                                                                                  |
|---------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------|
| `consistency-check` | Check whether provided ontologies are consistent. The result of this goal is written to an `output` file with one of two values: `true`, `false`.                                                                                                                                                  | `data` (required), `output` (required)                                                                                             |
| `extract-data`      | Create a CSV file with data from provided ontologies. This is the default goal. `filter-pattern` is used to filter out all entities that doesn't contain the given pattern within their IRIs.  `ontology-mapping` is a file created in the standard XML Catalogs format.                           | `data` (required), `output` (required), `filter-pattern` (optional), `ontology-mapping` (optional)                                 |
| `merge-imports`    | Merge ontologies with their imports and save them in RDF/XML format.  `ontology-iri` will be used as a new IRI for the merged ontology.  When `ontology-version-iri` option is provided, it is used as the new version IRI.  If it is not provided, the `ontology-iri` is used as the version IRI. | `data` (required), `output` (required), `ontology-iri` (required), `ontology-version-iri` (optional), `ontology-mapping` (optional) |


## Options

| Name                  | Description                                                                                                                                             | Example                                                                    |
|-----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------|
| `data`                | Path to the input ontology                                                                                                                              | `ontology/example.rdf`                                                     |
| `extract-data-column` | Used for specifying property IRI for specific column in extract-data output. Supported values: definition, example, explanatoryNote, synonym, usageNote | `synonym=http://example.com/synonym`                                       |
| `filter-pattern`  | String that should be within entity's IRI to include it                                                                                                 | `test`                                                                     |
| `goal`    | Specify which goal should be executed by the toolkit                                                                                                    | `extract-data`                                                             |
| `maturity-level` | Override default maturity levels; should have format 'maturityLevelIri=label'                                                                           | `maturity-level https://example.com/Provisional=Provisional` |
| `ontology-iri` | New IRI for merged ontology                                                                                                                             | `http://example.com/ontology` |
| `ontology-mapping` | Path to the catalog file with ontology mapping                                                                                                          | `catalog-v001.xml` |
| `ontology-version-iri` | New version IRI for merged ontology                                                                                                                     | `http://example.com/ontology1/v1` |
| `output` | Path where the result will be saved                                                                                                                     | `output.rdf` |






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


#### `extract-data with maturity-level`

```bash
java -jar onto-viewer-toolkit.jar \
    --goal extract-data \
    --data my-ontology.rdf \
    --maturity-level https://spec.industrialontologies.org/ontology/core/meta/AnnotationVocabulary/Provisional=Provisional \
    --output extracted-data.csv
```

#### `extract-data with extract-data-column`

```bash
java -jar onto-viewer-toolkit.jar \
    --goal extract-data \
    --data my-ontology.rdf \
    --extract-data-column synonym=https://spec.industrialontologies.org/ontology/core/meta/AnnotationVocabulary/synonym,https://www.omg.org/spec/Commons/AnnotationVocabulary/synonym,https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/synonym \
    --extract-data-column definition=https://spec.industrialontologies.org/ontology/core/meta/AnnotationVocabulary/naturalLanguageDefinition \
    --extract-data-column example=http://www.w3.org/2004/02/skos/core#example \
    --extract-data-column explanatoryNote=https://spec.industrialontologies.org/ontology/core/meta/AnnotationVocabulary/explanatoryNote \
    --extract-data-column usageNote=https://spec.industrialontologies.org/ontology/core/meta/AnnotationVocabulary/usageNote \
    --output extracted-data.csv
```


#### `merge-imports`

Without new version IRI:

```bash
java -jar onto-viewer-toolkit.jar \
    --goal merge-imports \
    --data my-ontology.rdf \
    --ontology-iri http://example.com/my-merged-ontology/ \
    --ontology-mapping catalog-v001.xml \
    --output my-merged-ontology.rdf
```

With new version IRI:

```bash
java -jar onto-viewer-toolkit.jar \
    --goal merge-imports \
    --data my-ontology.rdf \
    --ontology-iri http://example.com/my-merged-ontology/ \
    --ontology-version-iri http://example.com/my-merged-ontology/version1/ \
    --ontology-mapping catalog-v001.xml \
    --output my-merged-ontology.rdf
```

