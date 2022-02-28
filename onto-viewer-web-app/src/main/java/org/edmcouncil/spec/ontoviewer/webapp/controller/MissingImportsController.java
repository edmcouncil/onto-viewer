package org.edmcouncil.spec.ontoviewer.webapp.controller;

import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.listener.MissingImportListenerImpl;
import org.edmcouncil.spec.ontoviewer.webapp.boot.UpdateBlocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author patrycja.miazek (patrycja.miazek@makolab.com)
 */

@Controller
@RequestMapping(value = {"/api/missingImports"})
public class MissingImportsController {

  private static final Logger LOG = LoggerFactory.getLogger(MissingImportsController.class);
  @Autowired
  private OntologyManager ontologyManager;
  @Autowired
  private UpdateBlocker blocker;
  
   @GetMapping
  public ResponseEntity getMissingImportsAsJson() {
    if (!blocker.isInitializeAppDone()) {
      LOG.debug("Application initialization has not completed");
      return new ResponseEntity<>("503 Service Unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }
    return ResponseEntity.ok(ontologyManager.getMissingImports());    
  }
}
