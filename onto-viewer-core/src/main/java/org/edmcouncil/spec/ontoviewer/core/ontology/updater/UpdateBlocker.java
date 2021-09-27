package org.edmcouncil.spec.ontoviewer.core.ontology.updater;

import org.springframework.stereotype.Component;

@Component
public class UpdateBlocker {

  private boolean initializeAppDone = false;
  private boolean block = false;
  private boolean updateNow = false;

  public Boolean isInitializeAppDone() {
    return initializeAppDone;
  }

  public void setInitializeAppDone(Boolean initializeAppDone) {
    this.initializeAppDone = initializeAppDone;
  }

  public Boolean getBlock() {
    return block;
  }

  public void setBlock(Boolean block) {
    this.block = block;
  }

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
