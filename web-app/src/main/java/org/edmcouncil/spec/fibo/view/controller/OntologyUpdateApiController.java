package org.edmcouncil.spec.fibo.view.controller;

import java.util.List;
import org.edmcouncil.spec.fibo.view.model.ErrorResult;
import org.edmcouncil.spec.fibo.view.model.UpdateRequest;
import org.edmcouncil.spec.fibo.view.service.ApiKeyService;
import org.edmcouncil.spec.fibo.view.service.OntologyUpdateService;
import org.edmcouncil.spec.fibo.weasel.model.module.FiboModule;
import org.edmcouncil.spec.fibo.weasel.ontology.DetailsManager;
import org.edmcouncil.spec.fibo.weasel.ontology.updater.UpdateBlocker;
import org.edmcouncil.spec.fibo.weasel.ontology.updater.model.UpdateJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Controller
@RequestMapping(value = {"/api/update"})
public class OntologyUpdateApiController {

  private static final Logger LOG = LoggerFactory.getLogger(OntologyUpdateApiController.class);
  private static final String notValidApiKeyMessage = "ApiKey is not valid for this instance.";
  private static final String notCurrentUpdatesInProgress = "There is no update in the INPROGRESS state";

  @Autowired
  private ApiKeyService keyService;

  @Autowired
  private OntologyUpdateService updateService;

  @Autowired
  private DetailsManager ontologyManager;

  /*@RequestMapping(value = {"/key"})
  @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity startUpdate(@RequestParam("apiKey") String apiKey) {
    LOG.debug("[REQ] POST : /api/update/key ");
    if (!keyService.validateApiKey(req.getApiKey())) {
      LOG.debug(notValidApiKeyMessage);
      return ResponseEntity.badRequest().body(new ErrorResult(notValidApiKeyMessage));
    }
    UpdateJob uj = updateService.startUpdate();
    return ResponseEntity.ok(uj);
  }
   */
  @PutMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity startUpdateKeyInHeader(
          @RequestHeader(name = "X-API-Key", required = false) String apiKeyHeader,
          @RequestParam(value = "apiKey", required = false) String apiKeyParam) {
    LOG.debug("[REQ] PUT : /api/update ");
    String key = "";
    if (apiKeyHeader != null) {
      key = apiKeyHeader;
    } else if (apiKeyParam != null) {
      key = apiKeyParam;
    }
    if (!keyService.validateApiKey(key)) {
      LOG.debug(notValidApiKeyMessage);
      return ResponseEntity.badRequest().body(new ErrorResult(notValidApiKeyMessage));
    }
    UpdateJob uj = updateService.startUpdate();
    return ResponseEntity.ok(uj);
  }

  @ResponseBody
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity getUpdateStatus(@RequestHeader(name = "X-API-Key", required = false) String apiKeyHeader,
          @RequestParam(value = "apiKey", required = false) String apiKeyParam,
          @RequestParam(value = "updateId", required = true) String updateId) {
    String key = "";
    if (apiKeyHeader != null) {
      key = apiKeyHeader;
    } else if (apiKeyParam != null) {
      key = apiKeyParam;
    }
    LOG.debug("[REQ] GET : /api/update/ ");
    if (!keyService.validateApiKey(key)) {
      LOG.debug(notValidApiKeyMessage);
      return ResponseEntity.badRequest().body(new ErrorResult(notValidApiKeyMessage));
    }
    UpdateJob uj = updateService.getUpdateStatus(updateId);
    return ResponseEntity.ok(uj);
  }
}
