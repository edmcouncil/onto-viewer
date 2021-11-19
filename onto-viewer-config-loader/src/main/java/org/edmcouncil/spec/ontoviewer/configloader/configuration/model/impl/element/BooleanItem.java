package org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigItemAbstract;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class BooleanItem extends ConfigItemAbstract {

  private Boolean value;

  public BooleanItem() {
  }

  public BooleanItem(Boolean value) {
    this.value = value;
  }

  public Boolean getValue() {
    return value;
  }

  public void setValue(Boolean value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value.toString();
  }
}