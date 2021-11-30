package org.edmcouncil.spec.ontoviewer.webapp.model;

import java.util.StringJoiner;

public class FindResult {

  private final String iri;
  private final String type;
  private final String label;
  private final String highlight;
  private final double score;

  public FindResult(String iri, String type, String label, String highlight, double score) {
    this.iri = iri;
    this.type = type;
    this.label = label;
    this.highlight = highlight;
    this.score = score;
  }

  public String getIri() {
    return iri;
  }

  public String getType() {
    return type;
  }

  public String getLabel() {
    return label;
  }

  public String getHighlight() {
    return highlight;
  }

  public double getScore() {
    return score;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", FindResult.class.getSimpleName() + "[", "]")
        .add("iri='" + iri + "'")
        .add("type='" + type + "'")
        .add("label='" + label + "'")
        .add("highlight='" + highlight + "'")
        .add("score='" + score + "'")
        .toString();
  }
}
