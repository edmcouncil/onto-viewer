package org.edmcouncil.spec.fibo.view.controller;

import java.util.Arrays;
import java.util.List;
import org.edmcouncil.spec.fibo.view.model.ErrorResult;
import org.edmcouncil.spec.fibo.weasel.model.module.FiboModule;
import org.edmcouncil.spec.fibo.weasel.ontology.DetailsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.edmcouncil.spec.fibo.view.util.ModelBuilder;
import org.edmcouncil.spec.fibo.weasel.exception.NotFoundElementInOntologyException;
import org.edmcouncil.spec.fibo.weasel.model.details.OwlDetails;
import org.edmcouncil.spec.fibo.weasel.ontology.updater.UpdateBlocker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Controller
@RequestMapping(value = {"/api/module"})
public class ModuleApiController {

  private static final Logger LOG = LoggerFactory.getLogger(ModuleApiController.class);

  @Autowired
  private DetailsManager ontologyManager;
  @Autowired
  private UpdateBlocker blocker;

  @GetMapping("/module")
  public ResponseEntity getAllModulesDataAsJson() {
    LOG.debug("[REQ] GET : api / module");
    if (!blocker.isInitializeAppDone()) {
      LOG.debug("Application initialization has not completed");
      return new ResponseEntity<>("503 Service Unavailable", HttpStatus.SERVICE_UNAVAILABLE);
    }
    List<FiboModule> modules = ontologyManager.getAllModulesData();
    return ResponseEntity.ok(modules);
  }

}
