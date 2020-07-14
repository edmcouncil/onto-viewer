package org.edmcouncil.spec.fibo.weasel.ontology.updater;

import org.springframework.stereotype.Component;

@Component
public class UpdateBlocker {

  private Boolean block;
  private Boolean updateNow = false;

  void setBlockerStatus(Boolean status) {
    this.block = status;
  }

  public Boolean isBlocked() {
    return block;
  }

  public Boolean isUpdateNow() {
    return updateNow;
  }

  public void setUpdateNow(Boolean updateNow) {
    this.updateNow = updateNow;
  }

}
