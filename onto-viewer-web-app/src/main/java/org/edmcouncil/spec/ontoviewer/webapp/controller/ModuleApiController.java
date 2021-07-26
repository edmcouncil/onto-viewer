package org.edmcouncil.spec.ontoviewer.webapp.controller;

import org.edmcouncil.spec.ontoviewer.core.model.module.FiboModule;
import org.edmcouncil.spec.ontoviewer.core.ontology.DetailsManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.UpdateBlocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Controller
@RequestMapping(value = {"/api/module"})
public class ModuleApiController {

  private static final Logger LOG = LoggerFactory.getLogger(ModuleApiController.class);

  private final DetailsManager ontologyManager;
  private final UpdateBlocker blocker;

  public ModuleApiController(DetailsManager ontologyManager, UpdateBlocker blocker) {
    this.ontologyManager = ontologyManager;
    this.blocker = blocker;
  }

  @GetMapping
  public ResponseEntity<List<FiboModule>> getAllModulesDataAsJson() {
    LOG.debug("[REQ] GET : api / module");

    if (!blocker.isInitializeAppDone()) {
      LOG.debug("Application initialization has not completed");
      return ResponseEntity.internalServerError().build();
    }

    List<FiboModule> modules = ontologyManager.getAllModulesData();
    return ResponseEntity.ok(modules);
  }
}
