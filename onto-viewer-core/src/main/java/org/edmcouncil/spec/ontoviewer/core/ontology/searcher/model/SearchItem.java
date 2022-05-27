package org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model;

import java.util.StringJoiner;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevel;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class SearchItem {

  private String iri;
  private String label;
  private String description;
  private double relevancy;
  private MaturityLevel maturityLevel;

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

  public void setMaturityLevel(MaturityLevel maturityLevel) {
    this.maturityLevel = maturityLevel;
  }

  public MaturityLevel getMaturityLevel() {
    return maturityLevel;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", SearchItem.class.getSimpleName() + "[", "]")
        .add("iri='" + iri + "'")
        .add("label='" + label + "'")
        .add("description='" + description + "'")
        .add("relevancy=" + relevancy)
        .add("maturityLevel=" + maturityLevel)
        .toString();
  }
}
