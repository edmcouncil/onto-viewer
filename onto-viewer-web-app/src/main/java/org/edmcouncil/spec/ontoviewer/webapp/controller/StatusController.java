package org.edmcouncil.spec.ontoviewer.webapp.controller;

import org.edmcouncil.spec.ontoviewer.webapp.model.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Controller
@Deprecated
@RequestMapping(value = {"/status"})
public class StatusController {

  @GetMapping
  public ResponseEntity<Status> getAllModulesDataAsJson() {
    Status status = new Status();
    status.setName("FiboViewer");
    status.setVersion(System.getProperty("build.version"));
    return ResponseEntity.ok(status);
  }
}
