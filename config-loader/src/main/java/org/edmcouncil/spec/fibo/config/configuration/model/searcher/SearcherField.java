package org.edmcouncil.spec.fibo.config.configuration.model.searcher;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class SearcherField {

  private String iri;
  private Double boost = 1.0d;

  public String getIri() {
    return iri;
  }

  public void setIri(String iri) {
    this.iri = iri;
  }

  public Double getBoost() {
    return boost;
  }

  public void setBoost(Double boost) {
    this.boost = boost;
  }

}
