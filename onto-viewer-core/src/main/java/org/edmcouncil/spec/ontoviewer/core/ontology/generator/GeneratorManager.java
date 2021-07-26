package org.edmcouncil.spec.ontoviewer.core.ontology.generator;

public class GeneratorManager {

  private final StringBuilder sb;
  private boolean isFirst;
  private final String label;

  public GeneratorManager(String label) {
    this.sb = new StringBuilder();
    this.isFirst = true;
    this.label = label;
  }

  public StringBuilder getSb() {
    return sb;
  }

  public boolean isFirst() {
    return isFirst;
  }

  public void setFirst(boolean first) {
    isFirst = first;
  }

  public String getLabel() {
    return label;
  }
}
