package org.edmcouncil.spec.fibo.weasel.ontology.data.handler.fibo;

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

}
