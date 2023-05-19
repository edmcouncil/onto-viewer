package org.edmcouncil.spec.ontoviewer.core.model.property;

import java.util.List;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class MultiValue<T> extends PropertyValueAbstract<List<T>> {

  private String label;

  private String fullRenderedString;

  public void addValue(T newVal) {
    super.getValue().add(newVal);
  }

  public void setFullRenderedString(String fullRenderedString) {
    this.fullRenderedString = fullRenderedString;
  }

  public String getFullRenderedString() {
    return fullRenderedString;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
}
