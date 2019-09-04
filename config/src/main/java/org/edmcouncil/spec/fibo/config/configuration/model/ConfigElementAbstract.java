package org.edmcouncil.spec.fibo.config.configuration.model;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class ConfigElementAbstract implements ConfigElement {

  private ConfigElementType type;

  public ConfigElementAbstract() {
  }

  public ConfigElementAbstract(ConfigElementType type) {
    this.type = type;
  }

  @Override
  public ConfigElementType getType() {
    return this.type;
  }

  public void setType(ConfigElementType type) {
    this.type = type;
  }

}
