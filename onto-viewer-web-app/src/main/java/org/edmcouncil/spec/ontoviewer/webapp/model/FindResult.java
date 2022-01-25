package org.edmcouncil.spec.ontoviewer.webapp.model;

import java.util.List;
import java.util.StringJoiner;

public class FindResult {

  private final String iri;
  private final String type;
  private final String label;
  private final List<Highlight> highlights;
  private final double score;

  public FindResult(String iri, String type, String label, List<Highlight> highlights,
      double score) {
    this.iri = iri;
    this.type = type;
    this.label = label;
    this.highlights = highlights;
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

  public List<Highlight> getHighlights() {
    return highlights;
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
        .add("highlights='" + highlights + "'")
        .add("score='" + score + "'")
        .toString();
  }
}
