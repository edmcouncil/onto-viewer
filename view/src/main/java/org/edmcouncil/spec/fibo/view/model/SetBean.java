package org.edmcouncil.spec.fibo.view.model;

import java.util.Set;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */ 

public class SetBean {

  private Set<String> value;

  public Set<String> getValue() {
    return value;
  }

  public void setValue(Set<String> value) {
    this.value = value;
  }

}
