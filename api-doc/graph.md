Json is used directly in VisNetwork.
VisNet uses label, font, color, shape, id, dashes. 
Optional and type are fields that are used to display properly on the page and type and are only used in the programmer code.
Each node's id is unique.
Each node and relation contains an appropriate iri and label.
Iri is only needed to move around the resources. For example:
After double-clicking on a given node or relation, it will be redirected to that given resource.
To change the broadly understood styles, please do so in the source code. 
Type such as, INTERNAL, EXTERNAL are set in VisNodeConverter and VisRelationConverter. 
Each type is assigned the appropriate color.        

     "graph": {
          "lastId": 0,
          "nodes": [
            {
              "iri": "https://spec.edmcouncil.org/fibo/ontology/BE/LegalEntities/LegalPersons/LegalEntity",
              "label": "legal entity",
              "font": {
                "size": 15
              },
              "color": "rgb(255,168,7)",
              "shape": "box",
              "id": 1,
              "optional": false,
              "type": "MAIN"
            },
    ]
          {
              "iri": "https://spec.edmcouncil.org/fibo/ontology/BE/LegalEntities/FormalBusinessOrganizations/RegisteredAddress",
              "label": "registered address",
              "font": {
                "size": 15
              },
              "color": "#C2FABC",
              "shape": "box",
              "id": 2,
              "optional": false,
              "type": "INTERNAL"
            },
            {
              "iri": "https://spec.edmcouncil.org/fibo/ontology/FND/DatesAndTimes/FinancialDates/CombinedDateTime",
              "label": "combined date time",
              "font": {
                "size": 15
              },
              "color": "#C2FABC",
              "shape": "box",
              "id": 4,
              "optional": false,
              "type": "INTERNAL"
            },
          ],
          "edges": [
            {
              "from": 1,
              "to": 2,
              "arrows": "to",
              "label": "has address of legal formation",
              "color": {
                "color": "black"
              },
              "dashes": true,
              "optional": "non_optional",
              "type": "internal",
              "iri": "https://spec.edmcouncil.org/fibo/ontology/BE/LegalEntities/LEIEntities/hasAddressOfLegalFormation"
            },
     {
              "from": 1,
              "to": 4,
              "arrows": "to",
              "label": "has entity expiration date",
              "color": {
                "color": "black"
              },
              "dashes": true,
              "optional": "optional",
              "type": "internal",
              "iri": "https://spec.edmcouncil.org/fibo/ontology/FBC/FunctionalEntities/BusinessRegistries/hasEntityExpirationDate"
            },
    {
              "from": 1,
              "to": 6,
              "arrows": "to",
              "label": "has entity expiration reason",
              "color": {
                "color": "black"
              },
              "dashes": true,
              "optional": "optional",
              "type": "internal",
              "iri": "https://spec.edmcouncil.org/fibo/ontology/FBC/FunctionalEntities/BusinessRegistries/hasEntityExpirationReason"
            },
             {
              "from": 1,
              "to": 8,
              "arrows": "to",
              "label": "is classified by",
              "color": {
                "color": "black"
              },
              "dashes": true,
              "optional": "optional",
              "type": "internal",
              "iri": "https://spec.edmcouncil.org/fibo/ontology/FND/Relations/Relations/isClassifiedBy"
            }
},
          ]
