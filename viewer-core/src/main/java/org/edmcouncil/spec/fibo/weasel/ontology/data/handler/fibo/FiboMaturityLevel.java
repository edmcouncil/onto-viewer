package org.edmcouncil.spec.fibo.weasel.ontology.data.handler.fibo;

import java.util.Objects;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class FiboMaturityLevel {

  private String label;
  private String iri;

  FiboMaturityLevel(String label, String iri) {
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
    return "FiboMaturityLevel{" + "label=" + label + ", iri=" + iri + '}';
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
    final FiboMaturityLevel other = (FiboMaturityLevel) obj;
    if (!Objects.equals(this.label, other.label)) {
      return false;
    }
    if (!Objects.equals(this.iri, other.iri)) {
      return false;
    }
    return true;
  }

}
