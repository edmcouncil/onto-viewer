package org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.hint;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class HintItem {

  private String iri;
  private String label;
  private Double relevancy;

  public String getIri() {
    return iri;
  }

  public void setIri(String iri) {
    this.iri = iri;
  }

  public Double getRelevancy() {
    return relevancy;
  }

  public void setRelevancy(Double relevancy) {
    this.relevancy = relevancy;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

    @Override
    public String toString() {
        return "{" + "iri=" + iri + ", label=" + label + ", relevancy=" + relevancy + '}';
    }
}
