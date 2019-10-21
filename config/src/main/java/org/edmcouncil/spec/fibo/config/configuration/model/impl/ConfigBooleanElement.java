package org.edmcouncil.spec.fibo.config.configuration.model.impl;

import org.edmcouncil.spec.fibo.config.configuration.model.ConfigElementAbstract;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class ConfigBooleanElement extends ConfigElementAbstract {

  private Boolean value;

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
