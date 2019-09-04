package org.edmcouncil.spec.fibo.config.configuration.model.impl;

import org.edmcouncil.spec.fibo.config.configuration.model.ConfigElement;
import org.edmcouncil.spec.fibo.config.configuration.model.ConfigElementType;
import org.edmcouncil.spec.fibo.config.configuration.model.PairImpl;

/**
 * Create by Michał Daniel (michal.daniel@makolab.com)
 */
public class ConfigPairElement extends PairImpl<String, String> implements ConfigElement {

  private ConfigElementType type;

  @Override
  public ConfigElementType getType() {
    return this.type;
  }

}
