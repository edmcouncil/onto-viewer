Json is used directly in VisNetwork.
Each value in the list contains iri and optionally a label.
Each iri is an identifier and is unique.
The iri and label values ​​are in directed subclasses, taxonomies and all class axioms.
ValueA and ValueB have been replaced with iri and label. The code is easier to read in this way.

“taxonomy”: {
“value”: [
[
{
    “iri”: “http://www.w3.org/2002/07/owl#Thing”,
    “label”: “Thing”
},
{
    “iri”: “https://spec.edmcouncil.org/fibo/ontology/FND/AgentsAndPeople/Agents/AutonomousAgent”,
    “label”: “autonomous agent”
},
{
    “iri”: “https://spec.edmcouncil.org/fibo/ontology/FND/Organizations/Organizations/Organization”,
    “label”: “organization”
}
]
]
}

    “nodes”: [
{
    “iri”: “https://www.omg.org/spec/LCC/Countries/CountryRepresentation/GeopoliticalEntity”,
    “label”: “geopolitical entity”,
    “font”: {
    “size”: 15
}
}
]

“Instances”: [
{
    “type”: “INSTANCES”,
    “value”: {
    “label”: “Wells Fargo Bank, National Association, US”,
    “iri”: “https://spec.edmcouncil.org/fibo/ontology/FBC/FunctionalEntities/NorthAmericanEntities/USExampleIndividuals/WellsFargoBankNationalAssociation-US”
}
}
]