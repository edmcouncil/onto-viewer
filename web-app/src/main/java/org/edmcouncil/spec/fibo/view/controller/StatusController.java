package org.edmcouncil.spec.fibo.view.controller;

import java.util.List;
import org.edmcouncil.spec.fibo.view.model.Status;
import org.edmcouncil.spec.fibo.weasel.model.module.FiboModule;
import org.edmcouncil.spec.fibo.weasel.ontology.DetailsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Controller
@Deprecated
@RequestMapping(value = {"/status"})
public class StatusController {

  private static final Logger LOG = LoggerFactory.getLogger(StatusController.class);

  @GetMapping
  public ResponseEntity<Status> getAllModulesDataAsJson() {
    LOG.debug("[REQ] GET : status");
    Status status = new Status();
    status.setName("FiboViewer");
    status.setVersion(System.getProperty("build.version"));
    return ResponseEntity.ok(status);
  }

}
