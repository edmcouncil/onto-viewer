package org.edmcouncil.spec.fibo.config.configuration.model.impl.element;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */

public class RenameItem extends PairItem {

  public String getOldName() {
    return super.getLabel();
  }

  public void setOldName(String oldName) {
    super.setLabel(oldName);
  }

  public String getNewName() {
    return super.getIri();
  }

  public void setNewName(String newName) {
    super.setIri(newName);
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
  
}
