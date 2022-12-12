package org.edmcouncil.spec.ontoviewer.webapp.controller.api;

import org.edmcouncil.spec.ontoviewer.webapp.service.IntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/integration")
public class IntegrationController {

  private final IntegrationService integrationService;

  public IntegrationController(IntegrationService integrationService) {
    this.integrationService = integrationService;
  }

  @GetMapping("/dwDescribe")
  public ResponseEntity<String> getDataWorldDescription(@RequestParam("iri") String iri) {
    return ResponseEntity.ok(integrationService.getDataWorldDescribe(iri));
  }
}
