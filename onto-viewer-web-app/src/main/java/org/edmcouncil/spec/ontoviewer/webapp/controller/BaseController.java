package org.edmcouncil.spec.ontoviewer.webapp.controller;

import org.edmcouncil.spec.ontoviewer.core.exception.ApplicationNotInitializedException;
import org.edmcouncil.spec.ontoviewer.webapp.boot.UpdateBlocker;

public class BaseController {

  private final UpdateBlocker updateBlocker;

  public BaseController(UpdateBlocker updateBlocker) {
    this.updateBlocker = updateBlocker;
  }

  public void checkIfApplicationIsReady() throws ApplicationNotInitializedException {
    if (!updateBlocker.isInitializeAppDone()) {
      throw new ApplicationNotInitializedException("Application initialization has not completed");
    }
  }
}
