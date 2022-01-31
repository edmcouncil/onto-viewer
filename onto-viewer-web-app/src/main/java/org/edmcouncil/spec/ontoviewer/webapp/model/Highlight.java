package org.edmcouncil.spec.ontoviewer.webapp.model;

import java.util.StringJoiner;

public class Highlight {

  private final String fieldIdentifier;
  private final String highlightedText;

  public Highlight(String fieldIdentifier, String highlightedText) {
    this.fieldIdentifier = fieldIdentifier;
    this.highlightedText = highlightedText;
  }

  public String getFieldIdentifier() {
    return fieldIdentifier;
  }

  public String getHighlightedText() {
    return highlightedText;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", Highlight.class.getSimpleName() + "[", "]")
        .add("fieldIdentifier='" + fieldIdentifier + "'")
        .add("highlightedText='" + highlightedText + "'")
        .toString();
  }
}
