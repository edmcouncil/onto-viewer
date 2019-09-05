package org.edmcouncil.spec.fibo.config.configuration.model.impl;
import org.edmcouncil.spec.fibo.config.configuration.model.ConfigElement;
import org.edmcouncil.spec.fibo.config.configuration.model.WeaselConfigKeys;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */

public class ConfigRenameElement extends ConfigPairElement {

  public String getOldName() {
    return super.getValueA();
  }

  public void setOldName(String oldName) {
    super.setValueA(oldName);
  }

  public String getNewName() {
    return super.getValueB();
  }

  public void setNewName(String newName) {
    super.setValueB(newName);
  }

}
