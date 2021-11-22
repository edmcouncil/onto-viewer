package org.edmcouncil.spec.ontoviewer.core.model.property;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OwlAxiomPropertyEntity {

  private String iri;
  private String label;

  public OwlAxiomPropertyEntity() {
  }

  public OwlAxiomPropertyEntity(String iri, String label) {
    this.iri = iri;
    this.label = label;
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

  @Override
  public String toString() {
    return "OwlAxiomPropertyEntity{" + "iri=" + iri + ", label=" + label + '}';
  }
}