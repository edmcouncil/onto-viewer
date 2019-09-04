package org.edmcouncil.spec.fibo.weasel.ontology.data;

import org.edmcouncil.spec.fibo.weasel.model.WeaselOwlType;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class OwlDataExtractor {

  public WeaselOwlType extractAnnotationType(OWLAnnotationAssertionAxiom next) {
    if (next.getValue().isIRI()) {
      return WeaselOwlType.IRI;
    } else if (next.getValue().isLiteral()) {
      String datatype = next.getValue().asLiteral().get().getDatatype().toString();
      //TODO: move this strings to list and use contains
      if (datatype.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")
          || datatype.equals("http://www.w3.org/2001/XMLSchema#string")) {
        return WeaselOwlType.STRING;
      } else if (datatype.equals("xsd:anyURI")) {
        return WeaselOwlType.ANY_URI;
      }
    }
    return WeaselOwlType.OTHER;
  }

  public String extractAnyUriToString(String anyUri) {
 
    String uriString = anyUri.replaceFirst("\"", "").replaceFirst("\"^^xsd:anyURI", "");

    return uriString;
  }

}
