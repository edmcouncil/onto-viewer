package org.edmcouncil.spec.ontoviewer.configloader.configuration.model;

import java.util.Objects;
import java.util.StringJoiner;

public class FindProperty {

  private String label;
  private String identifier;
  private String iri;

  public FindProperty() {
  }

  public FindProperty(String label, String identifier, String iri) {
    this.label = label;
    this.identifier = identifier;
    this.iri = iri;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public String getIri() {
    return iri;
  }

  public void setIri(String iri) {
    this.iri = iri;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FindProperty)) {
      return false;
    }
    FindProperty that = (FindProperty) o;
    return Objects.equals(label, that.label) && Objects.equals(identifier,
        that.identifier) && Objects.equals(iri, that.iri);
  }

  @Override
  public int hashCode() {
    return Objects.hash(label, identifier, iri);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", FindProperty.class.getSimpleName() + "[", "]")
        .add("label='" + label + "'")
        .add("identifier='" + identifier + "'")
        .add("iri='" + iri + "'")
        .toString();
  }
}
