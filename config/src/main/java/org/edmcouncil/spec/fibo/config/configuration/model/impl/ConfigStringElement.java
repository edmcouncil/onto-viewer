package org.edmcouncil.spec.fibo.config.configuration.model.impl;

import java.util.Objects;
import org.edmcouncil.spec.fibo.config.configuration.model.ConfigElementAbstract;

/**
 * Create by Michał Daniel (michal.daniel@makolab.com)
 */
public class ConfigStringElement extends ConfigElementAbstract {

  private static int order = 0;

  private final String value;
  private final int orderVal;

  public ConfigStringElement(String value) {
    orderVal = order++;
    this.value = value;
  }

  public int getOrderVal() {
    return orderVal;
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.value);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ConfigStringElement other = (ConfigStringElement) obj;
    if (!Objects.equals(this.value, other.value)) {
      return false;
    }
    return true;
  }

}
