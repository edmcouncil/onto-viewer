package org.edmcouncil.spec.fibo.config.configuration.model.impl.element;

import org.edmcouncil.spec.fibo.config.configuration.model.ConfigItemAbstract;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class GroupLabelPriorityItem extends ConfigItemAbstract {

  private Priority value;

  public Priority getValue() {
    return value;
  }

  public void setValue(Priority value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return value.name();
  }

  public enum Priority {
    DEFINED, EXTRACTED
  }

}
