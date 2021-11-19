package org.edmcouncil.spec.ontoviewer.core.ontology.data.extractor;

import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class OwlDataExtractor {

  public OwlType extractAnnotationType(OWLAnnotationAssertionAxiom next) {
    if (next.getValue().isIRI()) {
      return OwlType.IRI;
    } else if (next.getValue().isLiteral()) {
      String datatype = next.getValue().asLiteral().get().getDatatype().toString();
      //TODO: move this strings to list and use contains
      if (datatype.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")
          || datatype.equals("http://www.w3.org/2001/XMLSchema#string")) {
        return OwlType.STRING;
      } else if (datatype.equals("xsd:anyURI")) {
        return OwlType.ANY_URI;
      }
    }
    return OwlType.OTHER;
  }

  /**
   * This method is used to extract annotation type for ontology.
   *
   * @param next
   * @return
   */
  public OwlType extractAnnotationType(OWLAnnotation next) {
    if (next.getValue().isIRI()) {
      return OwlType.IRI;
    } else if (next.getValue().isLiteral()) {
      String datatype = next.getValue().asLiteral().get().getDatatype().toString();
      //TODO: move this strings to list and use contains
      if (datatype.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")
          || datatype.equals("http://www.w3.org/2001/XMLSchema#string")) {
        return OwlType.STRING;
      } else if (datatype.equals("xsd:anyURI")) {
        return OwlType.ANY_URI;
      }
    }
    return OwlType.OTHER;
  }

  public String extractAnyUriToString(String anyUri) {
    String uriString = anyUri.replaceFirst("\"", "");
    uriString = uriString.substring(0, uriString.length() - 13);
    return uriString;
  }

  
  public static String extractAxiomPropertyIri(OWLRestriction someValuesFromAxiom) {
    String propertyIri = null;
    for (OWLEntity oWLEntity : someValuesFromAxiom.getProperty().signature().collect(Collectors.toList())) {
      propertyIri = oWLEntity.toStringID();
    }
    return propertyIri;
  }

}
