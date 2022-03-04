package org.edmcouncil.spec.ontoviewer.webapp.controller;

import java.util.Set;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.listener.MissingImport;
import org.edmcouncil.spec.ontoviewer.webapp.boot.UpdateBlocker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author patrycja.miazek (patrycja.miazek@makolab.com)
 */

@RestController
@RequestMapping("/api/missingImports")
public class MissingImportsController extends BaseController {

  @Autowired
  private OntologyManager ontologyManager;

  public MissingImportsController(OntologyManager ontologyManager, UpdateBlocker updateBlocker) {
    super(updateBlocker);
    this.ontologyManager = ontologyManager;
  }

  @GetMapping
  public ResponseEntity<Set<MissingImport>> getMissingImportsAsJson() {
    checkIfApplicationIsReady();
    return ResponseEntity.ok(ontologyManager.getMissingImports());
  }
}
