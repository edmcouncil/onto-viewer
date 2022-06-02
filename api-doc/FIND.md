# Find search

[Back to README](./README.md)

#### /api/find (GET)

#### Request Parameters
  - **term** - required; term to find entities by
  - **mode** - optional; permissible values: [`basic`, `advance`], default: `basic`
  - **findProperties** - optional; list of properties to search within
  - **useHighlighting** - optional, true by default; if true, returns results with highlightings

#### Description

Return a list of search results that match the `term`.  There are two modes: `basic` and `advance`.

- In the `basic` mode `term` is looked for within RDFS label annotation property and all its subannotations.  You don't have to specify the `mode` parameter (but you may, if you want), because the `basic` mode is the default one.

  Here is a sample basic request: `<host_and_port>/api/find?term=check`

- In the `advance`  mode a client sends a list of find properties that are then used to search for entities.  These properties are sent with the `findProperties` request parameter, and their identifiers should be delimited with a dot (`.`).  For example, this is a correct list of properties that may be sent by a client: `rdfs_label.skos_definition.purl_description`.  The list of find properties can be obtained from `/api/find/properties` (see below).

  Here is a sample advance request: `<host_and_port>/api/find?term=check&mode=advance&findProperties=rdfs_label.skos_definition`

#### Example Response

```json
[
  {
    "iri": "https://spec.edmcouncil.org/fibo/ontology/LOAN/LoanTypes/MortgageLoans/Mortgage",
    "type": "CLASS",
    "label": "mortgage",
    "highlight": "<B>mortgage</B>",
    "score": 2.9687047004699707
  },
  {
    "iri": "https://spec.edmcouncil.org/fibo/ontology/LOAN/LoanTypes/MortgageLoans/ReverseMortgage",
    "type": "CLASS",
    "label": "reverse mortgage",
    "highlight": "A reverse <B>mortgage</B> and an open end loan both have a credit limit.",
    "score": 2.458970069885254
  },
  {
    "iri": "https://spec.edmcouncil.org/fibo/ontology/LOAN/LoanTypes/MortgageLoans/MortgageLoanPurpose-MortgageModification",
    "type": "INDIVIDUAL",
    "label": "mortgage modification",
    "highlight": "<B>mortgage</B> modification",
    "score": 2.3639402389526367
  }
]
```
#### /api/find/properties (GET)

#### Request Parameters

None

#### Description

Return the list of find properties supported by the application.  These properties may be used in the advance mode for searching specifically within them.

#### Example Response

```json
[
  {
    "label": "RDFS Label",
    "identifier": "rdfs_label",
    "iri": "http://www.w3.org/2000/01/rdf-schema#label"
  },
  {
    "label": "SKOS Definition",
    "identifier": "skos_definition",
    "iri": "http://www.w3.org/2004/02/skos/core#definition"
  },
  {
    "label": "FIBO Explanatory Note",
    "identifier": "fibo_explanatoryNote",
    "iri": "https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/explanatoryNote"
  }
]