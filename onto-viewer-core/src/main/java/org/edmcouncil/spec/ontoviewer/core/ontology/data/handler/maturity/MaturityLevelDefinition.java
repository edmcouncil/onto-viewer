package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity;

import java.util.Optional;

public enum MaturityLevelDefinition {

  RELEASE("https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/Release",
      "Release"),
  PROVISIONAL("https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/Provisional",
      "Provisional"),
  INFORMATIVE("https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/Informative",
      "Informative"),
  MIXED("https://spec.edmcouncil.org/ontoviewer/Mixed", "Mixed"),
  NOT_SET("https://spec.edmcouncil.org/ontoviewer/NotSet", "Not Set");

  private final String iri;
  private final String label;

  MaturityLevelDefinition(String iri, String label) {
    this.iri = iri;
    this.label = label;
  }

  public String getIri() {
    return iri;
  }

  public String getLabel() {
    return label;
  }

  public static Optional<MaturityLevelDefinition> getByIri(String maturityLevelIri) {
    for (MaturityLevelDefinition value : values()) {
      if (value.iri.equals(maturityLevelIri)) {
        return Optional.of(value);
      }
    }
    return Optional.empty();
  }
}
