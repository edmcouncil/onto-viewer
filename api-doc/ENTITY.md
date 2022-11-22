# Entity 

 [Back to README](./README.md)

 `/api/entity?iri=<iri>`

### Description

Returns a named entity of any type (class, individual, ontology etc.) from ontologies with the given `iri`.  If an entity is not present, `404` is returned.

### Example curl (linux)

```
curl --location --request GET '<host_and_port>/api/entity?iri=http://www.w3.org/2000/01/rdf-schema%23label' \
--header 'Accept: application/json'
```

### Example Response

(Some parts of the response were removed for the brevity sake.)

```json
{
    "type": "details",
    "result": {
        "label": "label",
        "iri": "http://www.w3.org/2000/01/rdf-schema#label",
        "qName": null,
        "taxonomy": {
            "value": [
                [
                    {
                        "iri": "http://www.w3.org/1999/02/22-rdf-syntax-ns#Property",
                        "label": "Property"
                    },
                    {
                        "iri": "http://www.w3.org/2000/01/rdf-schema#label",
                        "label": "label"
                    }
                ]
            ]
        },
        "locationInModules": [
            "https://spec.edmcouncil.org/fibo/ontology/FND/MetadataFND/FNDDomain",
            "https://spec.edmcouncil.org/fibo/ontology/FND/Places/MetadataFNDPlaces/PlacesModule",
            "https://spec.edmcouncil.org/fibo/ontology/FND/Places/VirtualPlaces/"
        ],
        "graph": null,
        "maturityLevel": {
            "label": "Not Set",
            "iri": "https://spec.edmcouncil.org/ontoviewer/NotSet"
        },
        "properties": {
            "Glossary": {
                "generated description": [
                    {
                        "type": "STRING",
                        "value": "Own descriptions:\n- Label is a kind of Property."
                    }
                ]
            }
        }
    }
}
```