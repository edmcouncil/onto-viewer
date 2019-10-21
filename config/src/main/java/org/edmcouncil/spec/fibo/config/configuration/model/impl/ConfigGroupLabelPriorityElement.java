package org.edmcouncil.spec.fibo.config.configuration.model.impl;

import org.edmcouncil.spec.fibo.config.configuration.model.ConfigElementAbstract;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class ConfigGroupLabelPriorityElement extends ConfigElementAbstract {

  private GroupLabelPriority value;

  public GroupLabelPriority getValue() {
    return value;
  }

  public void setValue(GroupLabelPriority value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value.name();
  }

  public enum GroupLabelPriority {
    FRAGMENT, LABEL
  }

}
