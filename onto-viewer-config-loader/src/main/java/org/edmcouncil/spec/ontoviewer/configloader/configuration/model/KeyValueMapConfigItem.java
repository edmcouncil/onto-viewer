package org.edmcouncil.spec.ontoviewer.configloader.configuration.model;

import java.util.Map;

public class KeyValueMapConfigItem extends ConfigItemAbstract {

  private final Map<String, Object> properties;

  public KeyValueMapConfigItem(Map<String, Object> properties) {
    super(ConfigItemType.KEY_VALUE_MAP);
    this.properties = properties;
  }

  public Object get(String key) {
    return properties.get(key);
  }

  public void putIfAbsent(String key, Object value) {
    properties.putIfAbsent(key, value);
  }

  public Map<String, Object> getProperties() {
    return properties;
  }
}