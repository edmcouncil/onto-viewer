package org.edmcouncil.spec.ontoviewer.configloader.configuration.model;

import java.util.List;

public class PairWithList {

  private String label;
  private List<String> iris;

  public PairWithList(String label, List<String> iris) {
    this.label = label;
    this.iris = iris;
  }

  public String getLabel() {
    return label;
  }

  public List<String> getIris() {
    return iris;
  }
}
