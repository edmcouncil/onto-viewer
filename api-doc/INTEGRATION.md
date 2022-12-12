# Integration

[Back to README](./README.md)

### /api/integration/dwDescribe (GET)

#### Request Parameters
  - **iri** - required, IRI of an entity for which the SPARQL `DESCRIBE` will be provided

#### Description

Return a result of `DESCRIBE` from data.world SPARQL endpoint with the given.  The data.world endpoint URL should be set
by the `integration_config.yaml` configuration.

#### Example Response

```xml
<rdf:RDF
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:owl="http://www.w3.org/2002/07/owl#"
    xmlns:j.0="https://www.omg.org/spec/Commons/AnnotationVocabulary/"
    xmlns:j.1="http://www.w3.org/2004/02/skos/core#"
    xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
    xmlns:fn="http://www.w3.org/2005/xpath-functions#"
    xmlns:foaf="http://xmlns.com/foaf/0.1/"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema#">
  <owl:AnnotationProperty rdf:about="https://www.omg.org/spec/Commons/AnnotationVocabulary/abbreviation">
    <j.0:explanatoryNote>The symbols for quantities are generally single letters of the Latin or Greek alphabet, sometimes with subscripts or other modifying signs. These letters, including those that are members of the Greek alphabet are not symbols for the purposes of this ontology, however, they are abbreviations. Expressions of chemical formulae may, however, include a combination of abbreviations and symbols, as needed to define a given quantity.</j.0:explanatoryNote>
    <j.0:adaptedFrom>ISO 31-0 Quantities and units - General principles</j.0:adaptedFrom>
    <j.0:adaptedFrom>ISO 1087 Terminology work and terminology science - Vocabulary, Second edition, 2019-09</j.0:adaptedFrom>
    <j.1:note>Abbreviations can be created by removing individual words, or can be acronyms, initialisms, or clipped terms.</j.1:note>
    <j.1:example>Chemical Symbols: H, O, Mg; Units of Measure: Km, Kg, G</j.1:example>
    <j.1:definition>designation formed by omitting parts from the full form of a term that denotes the same concept</j.1:definition>
    <rdfs:subPropertyOf rdf:resource="https://www.omg.org/spec/Commons/AnnotationVocabulary/synonym"/>
    <rdfs:label>abbreviation</rdfs:label>
  </owl:AnnotationProperty>
</rdf:RDF>
```