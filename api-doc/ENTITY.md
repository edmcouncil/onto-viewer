# Entity 

 [Back to README](./README.md)

#### /api/entity?iri=<iri>

#### Description

Returns a named entity of any type (class, individual, ontology etc.) from ontologies with the given `iri`.  If an entity is not present, `404` is returned.

#### Example Response

(Some parts of the response were removed for the brevity sake.)

```json
"type":"details",
   "result":{
      "label":"mortgage",
      "iri":"https://spec.edmcouncil.org/fibo/ontology/LOAN/LoanTypes/MortgageLoans/Mortgage",
      "qName":"",
      "taxonomy":{
         "value":[
            [
               {
                  "iri":"http://www.w3.org/2002/07/owl#Thing",
                  "label":"Thing"
               },
               {
                  "iri":"https://spec.edmcouncil.org/fibo/ontology/LOAN/LoanContracts/LoanCore/CollateralizedSecuredLoan",
                  "label":"CollateralizedSecuredLoan"
               },
               {
                  "iri":"https://spec.edmcouncil.org/fibo/ontology/LOAN/LoanTypes/MortgageLoans/Mortgage",
                  "label":"mortgage"
               }
            ]
         ]
      },
      "locationInModules":[
         "https://spec.edmcouncil.org/fibo/ontology/LOAN/LoanTypes/MortgageLoans/"
      ],
      "graph":{
         "lastId":0,
         "nodes":[
            
         ],
         "edges":[
            
         ],
         "root":null
      },
      "maturityLevel":{
         "label":"provisional",
         "iri":"https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/Provisional",
         "icon":"develop"
      },
      "properties":{
         "Glossary":{
            "label":[
               {
                  "type":"STRING",
                  "value":"mortgage"
               }
            ],
            "definition":[
               {
                  "type":"STRING",
                  "value":"a loan contract that is secured by real property"
               }
            ],
            "editorial note":[
               
            ],
            "generated description":[
               
            ]
         },
         "Ontological characteristic":{
            "Direct subclasses":[
               
            ],
            "Equivalent classes (necessary and sufficient criteria)":[
               
            ],
            "IS-A restrictions":[
               
            ],
            "IS-A restrictions inherited from superclasses":[
               
            ],
            "Usage":[
               
            ]
         },
         "Meta-information":{
            "is defined by":[
               {
                  "type":"IRI",
                  "value":{
                     "iri":"https://spec.edmcouncil.org/fibo/ontology/LOAN/LoanTypes/MortgageLoans/",
                     "label":"MortgageLoans"
                  }
               }
            ]
         },
         "other":{
            "adapted from":[
               {
                  "type":"STRING",
                  "value":"the Cambridge Business English Dictionary"
               }
            ]
         }
      }
   }
}
```