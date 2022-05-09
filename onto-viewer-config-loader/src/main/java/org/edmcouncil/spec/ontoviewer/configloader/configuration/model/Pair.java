package org.edmcouncil.spec.ontoviewer.configloader.configuration.model;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class Pair {

  private String label;
  private String iri;

  public Pair() {
  }

  public Pair(String label, String iri) {
    this.label = label;
    this.iri = iri;
  }

  public String getLabel() {
    return this.label;
  }

  public String getIri() {
    return this.iri;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setIri(String iri) {
    this.iri = iri;
  }
}