package org.edmcouncil.spec.ontoviewer.core.model.property;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OwlAxiomPropertyEntity {

  private String iri;
  private String label;
  private boolean deprecated;

  public OwlAxiomPropertyEntity() {
  }

  public OwlAxiomPropertyEntity(String iri, String label, boolean deprecated) {
    this.iri = iri;
    this.label = label;
    this.deprecated = deprecated;
  }

  public String getIri() {
    return iri;
  }

  public void setIri(String iri) {
    this.iri = iri;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public boolean isDeprecated() {
    return deprecated;
  }

  public void setDeprecated(boolean deprecated) {
    this.deprecated = deprecated;
  }

  @Override
  public String toString() {
    return "OwlAxiomPropertyEntity{" +
        "iri='" + iri + '\'' +
        ", label='" + label + '\'' +
        ", deprecated=" + deprecated +
        '}';
  }
}