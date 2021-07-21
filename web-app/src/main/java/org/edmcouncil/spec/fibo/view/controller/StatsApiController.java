package org.edmcouncil.spec.fibo.view.controller;

import org.edmcouncil.spec.fibo.weasel.model.module.FiboModule;
import org.edmcouncil.spec.fibo.weasel.ontology.DetailsManager;
import org.edmcouncil.spec.fibo.weasel.ontology.updater.UpdateBlocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import org.edmcouncil.spec.fibo.weasel.ontology.stats.OntologyStatsManager;

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
    LOG.debug("[REQ] GET : api / stats");

    if (!blocker.isInitializeAppDone()) {
      LOG.debug("Application initialization has not completed");
      return new ResponseEntity<>("503 Service Unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }
    return ResponseEntity.ok(ontologyStatsManager.getOntologyStats());
  }
}
