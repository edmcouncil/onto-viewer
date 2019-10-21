package org.edmcouncil.spec.fibo.config.configuration.model.impl;

import org.edmcouncil.spec.fibo.config.configuration.model.ConfigElementAbstract;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class ConfigMissingLanguageElement extends ConfigElementAbstract {

  private MissingLanguageAction value;

  public MissingLanguageAction getValue() {
    return value;
  }

  public void setValue(MissingLanguageAction value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value.name();
  }
  
  public enum MissingLanguageAction{
    FIRST, FRAGMENT
  }

}
