package org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class SearchItem {

  private String iri;
  private String label;
  private String description;
  private double relevancy;

  public String getIri() {
    return iri;
  }

  public double getRelevancy() {
    return relevancy;
  }

  public void setRelevancy(double relevancy) {
    this.relevancy = relevancy;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

    @Override
    public String toString() {
        return "{" + "iri=" + iri + ", label=" + label + ", relevancy=" + relevancy + '}';
    }
}
