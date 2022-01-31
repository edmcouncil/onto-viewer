package org.edmcouncil.spec.ontoviewer.webapp.controller;

import org.edmcouncil.spec.ontoviewer.core.ontology.stats.OntologyStatsManager;
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
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Controller
@RequestMapping(value = {"/api/stats"})
public class StatsApiController {

  private static final Logger LOG = LoggerFactory.getLogger(StatsApiController.class);

  @Autowired
  private OntologyStatsManager ontologyStatsManager;
  @Autowired
  private UpdateBlocker blocker;

  @GetMapping
  public ResponseEntity getAllModulesDataAsJson() {
    if (!blocker.isInitializeAppDone()) {
      LOG.debug("Application initialization has not completed");
      return new ResponseEntity<>("503 Service Unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }
    return ResponseEntity.ok(ontologyStatsManager.getOntologyStats());    
  }
}
