package org.edmcouncil.spec.ontoviewer.configloader.configuration.model;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class ConfigItemAbstract implements ConfigItem {

  private ConfigItemType type;

  public ConfigItemAbstract() {
  }

  public ConfigItemAbstract(ConfigItemType type) {
    this.type = type;
  }

  @Override
  public ConfigItemType getType() {
    return this.type;
  }

  public void setType(ConfigItemType type) {
    this.type = type;
  }

}
