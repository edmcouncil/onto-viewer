# Modules

 [Back to README](./README.md)

#### /api/module (GET)

#### Description

Return a list of modules and submodules from ontology.

#### Example Response

```json
[
   {
      "iri":"https://spec.edmcouncil.org/fibo/ontology/BE/MetadataBE/BEDomain",
      "label":"Business Entities",
      "subModule":[
         {
            "iri":"https://spec.edmcouncil.org/fibo/ontology/BE/Corporations/MetadataBECorporations/CorporationsModule",
            "label":"Corporations",
            "subModule":[
               {
                  "iri":"https://spec.edmcouncil.org/fibo/ontology/BE/Corporations/Corporations/",
                  "label":"Corporations Ontology",
                  "subModule":[
                     
                  ],
                  "maturityLevel":{
                     "label":"prod"
                  }
               }
            ],
            "maturityLevel":{
               "label":"prod"
            }
         }
      ]
   }
]
```
