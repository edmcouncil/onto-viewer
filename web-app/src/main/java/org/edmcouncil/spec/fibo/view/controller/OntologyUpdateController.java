package org.edmcouncil.spec.fibo.view.controller;

import java.util.List;
import org.edmcouncil.spec.fibo.view.model.ErrorResult;
import org.edmcouncil.spec.fibo.view.model.UpdateRequest;
import org.edmcouncil.spec.fibo.view.service.ApiKeyService;
import org.edmcouncil.spec.fibo.view.service.OntologyUpdateService;
import org.edmcouncil.spec.fibo.weasel.ontology.updater.model.UpdateJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Controller
@RequestMapping(value = {"/update/ontology"})
public class OntologyUpdateController {

  private static final Logger LOG = LoggerFactory.getLogger(OntologyUpdateController.class);
  private static final String notValidApiKeyMessage = "ApiKey is not valid for this instance.";
  
  @Autowired
  private ApiKeyService keyService;

  @Autowired
  private OntologyUpdateService updateService;

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity getAllModulesDataAsJson(@RequestBody UpdateRequest req) {
    LOG.debug("[REQ] POST : /update/ontology ");
    if(!keyService.validateApiKey(req.getApiKey())){
      LOG.debug(notValidApiKeyMessage);
      return ResponseEntity.badRequest().body(new ErrorResult(notValidApiKeyMessage));
    }
    UpdateJob uj = updateService.startUpdate();
    return ResponseEntity.ok(uj);
  }

  @ResponseBody
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity getUpdateStatus(@RequestBody UpdateRequest req) {
    LOG.debug("[REQ] GET : /update/ontology ");
    if(!keyService.validateApiKey(req.getApiKey())){
      LOG.debug(notValidApiKeyMessage);
      return ResponseEntity.badRequest().body(new ErrorResult(notValidApiKeyMessage));
    }
    UpdateJob uj = updateService.getUpdateStatus(req.getUpdateId());
    return ResponseEntity.ok(uj);

  }

}
