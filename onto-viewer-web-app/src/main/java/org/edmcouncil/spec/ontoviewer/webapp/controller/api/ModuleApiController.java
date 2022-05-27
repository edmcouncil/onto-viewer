package org.edmcouncil.spec.ontoviewer.webapp.controller.api;

import java.util.List;
import org.edmcouncil.spec.ontoviewer.core.model.module.OntologyModule;
import org.edmcouncil.spec.ontoviewer.core.ontology.DetailsManager;
import org.edmcouncil.spec.ontoviewer.webapp.boot.UpdateBlocker;
import org.edmcouncil.spec.ontoviewer.webapp.controller.BaseController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@RestController
@RequestMapping(value = {"/api/module"})
public class ModuleApiController extends BaseController {

  private final DetailsManager ontologyManager;

  public ModuleApiController(DetailsManager ontologyManager, UpdateBlocker blocker) {
    super(blocker);
    this.ontologyManager = ontologyManager;
  }

  @GetMapping
  public ResponseEntity<List<OntologyModule>> getAllModulesDataAsJson() {
    checkIfApplicationIsReady();

    List<OntologyModule> modules = ontologyManager.getAllModulesData();
    return ResponseEntity.ok(modules);
  }
}
