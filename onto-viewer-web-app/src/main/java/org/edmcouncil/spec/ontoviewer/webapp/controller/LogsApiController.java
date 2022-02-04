package org.edmcouncil.spec.ontoviewer.webapp.controller;

import static org.edmcouncil.spec.ontoviewer.webapp.common.RequestConstants.API_KEY_NOT_VALID_MESSAGE;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.edmcouncil.spec.ontoviewer.webapp.model.ErrorResponse;
import org.edmcouncil.spec.ontoviewer.webapp.service.ApiKeyService;
import org.edmcouncil.spec.ontoviewer.webapp.service.LogsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
@Controller
@RequestMapping(value = {"/api/logs"})
public class LogsApiController {

  private static final Logger LOG = LoggerFactory.getLogger(LogsApiController.class);
  private final ApiKeyService keyService;
  private final LogsService logsService;

  public LogsApiController(ApiKeyService keyService, LogsService logsService) {
    this.keyService = keyService;
    this.logsService = logsService;
  }

  @GetMapping
  public ResponseEntity getLogs(@RequestHeader(name = "X-API-Key", required = false) String apiKeyHeader,
      @RequestParam(value = "ApiKey", required = false) String apiKeyParam,
      @RequestParam(value = "date", required = false) Optional<String> date,
      @RequestHeader(value = "Accept", required = true) String acceptHeader) throws IOException {
    if (!acceptHeader.contains(MediaType.TEXT_PLAIN_VALUE)) {
      return ResponseEntity.badRequest().body("Incorrect or missing header. `Accept: " + acceptHeader + "'");
    }
    String key = "";
    if (apiKeyHeader != null) {
      key = apiKeyHeader;
    } else if (apiKeyParam != null) {
      key = apiKeyParam;
    }

    if (!keyService.validateApiKey(key)) {
      LOG.debug(API_KEY_NOT_VALID_MESSAGE);
      return ResponseEntity.badRequest().body(
          new ErrorResponse(API_KEY_NOT_VALID_MESSAGE, null));
    }
    List<String> logs = logsService.getLogs(date.orElse(null));

    return ResponseEntity.ok(String.join("\n", logs));
  }

}
