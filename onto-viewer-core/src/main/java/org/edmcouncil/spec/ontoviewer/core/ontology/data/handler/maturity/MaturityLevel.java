package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity;

import java.util.Objects;
import java.util.StringJoiner;

public class MaturityLevel {

  private String label;
  private String iri;

  public MaturityLevel(String label, String iri) {
    this.label = label;
    this.iri = iri;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getIri() {
    return iri;
  }

  public void setIri(String iri) {
    this.iri = iri;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", MaturityLevel.class.getSimpleName() + "[", "]")
        .add("label='" + label + "'")
        .add("iri='" + iri + "'")
        .toString();
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 83 * hash + Objects.hashCode(this.label);
    hash = 83 * hash + Objects.hashCode(this.iri);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MaturityLevel other = (MaturityLevel) obj;
    if (!Objects.equals(this.label, other.label)) {
      return false;
    }
    return Objects.equals(this.iri, other.iri);
  }
}
