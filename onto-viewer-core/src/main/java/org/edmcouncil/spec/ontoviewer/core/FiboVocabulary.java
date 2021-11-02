package org.edmcouncil.spec.ontoviewer.core;

import org.semanticweb.owlapi.model.IRI;

public enum FiboVocabulary {

  HAS_MATURITY_LEVEL("https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/hasMaturityLevel");

  private final String iriString;
  private final IRI iri;

  FiboVocabulary(String iriString) {
    this.iriString = iriString;
    this.iri = IRI.create(iriString);
  }

  public String getIriString() {
    return iriString;
  }

  public IRI getIri() {
    return iri;
  }
}
