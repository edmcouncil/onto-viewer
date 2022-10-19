# Graph

 [Back to README](./README.md)

#### /api/graph (GET)

#### Request Parameters
- **iri** - required; resource iri.
- **nodeId** - optional (defult: 0); id of element that will be root node. 
- **lastId** - optional (defult: 0); last element from the previous graph.

#### Description
  Return graph for specific given `iri`. It can be used to expand graph on website, some data like `nodeId` and `lastId` must be given from existing/previous graph.

#### Example Response
```
{
    "lastId": 1003,
    "nodes": [
        {
            "iri": "https://spec.edmcouncil.org/fibo/ontology/BE/LegalEntities/LegalPersons/ReligiousObjective",
            "label": "religious objective",
            "font": {
                "size": 15
            },
            "color": "rgb(255,168,7)",
            "shape": "box",
            "id": 500,
            "optional": false,
            "type": "MAIN"
        },
        {
            "iri": "https://spec.edmcouncil.org/fibo/ontology/FND/DatesAndTimes/FinancialDates/DatePeriod",
            "label": "date period",
            "font": {
                "size": 15
            },
            "color": null,
            "shape": "box",
            "id": 1001,
            "optional": false,
            "type": "EXTERNAL"
        }
    ],
    "edges": [
        {
            "from": 500,
            "to": 1001,
            "arrows": "to",
            "label": "has date period (1..*)",
            "color": {
                "color": "black"
            },
            "dashes": false,
            "optional": "non_optional",
            "type": "external",
            "iri": "https://spec.edmcouncil.org/fibo/ontology/FND/DatesAndTimes/FinancialDates/hasDatePeriod",
            "equivalentTo": false
        }
    ],
    "root": null
}
```