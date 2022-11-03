package org.edmcouncil.spec.ontoviewer.webapp.controller.api;

import static org.edmcouncil.spec.ontoviewer.webapp.common.RequestConstants.API_KEY_NOT_VALID_MESSAGE;
import static org.edmcouncil.spec.ontoviewer.webapp.common.RequestConstants.SUCCESS_RESPONSE;

import org.edmcouncil.spec.ontoviewer.webapp.common.RequestConstants;
import org.edmcouncil.spec.ontoviewer.webapp.model.BaseResponse;
import org.edmcouncil.spec.ontoviewer.webapp.model.ErrorResponse;
import org.edmcouncil.spec.ontoviewer.webapp.service.ApiKeyService;
import org.edmcouncil.spec.ontoviewer.webapp.service.ApplicationIdService;
import org.edmcouncil.spec.ontoviewer.webapp.service.ApplicationManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ManagementApiController {

  private static final Logger LOG = LoggerFactory.getLogger(ManagementApiController.class);
  private final ApiKeyService apiKeyService;
  private final ApplicationManagementService applicationManagementService;
  private final ApplicationIdService applicationIdService;

  public ManagementApiController(ApiKeyService apiKeyService,
      ApplicationManagementService applicationManagementService,
      ApplicationIdService applicationIdService) {
    this.apiKeyService = apiKeyService;
    this.applicationManagementService = applicationManagementService;
    this.applicationIdService = applicationIdService;
  }

  @PutMapping("/api/")
  public ResponseEntity<BaseResponse> stopApplication(
      @RequestHeader(value = RequestConstants.X_API_KEY) String apiKeyHeader,
      @RequestHeader(value = RequestConstants.APPLICATION_ID, required = false) String applicationId) {

    if (!apiKeyService.validateApiKey(apiKeyHeader)) {
      return ResponseEntity.badRequest().body(
          new ErrorResponse(API_KEY_NOT_VALID_MESSAGE, null));
    }
    if (applicationId != null && !applicationId.isEmpty() && applicationIdService.getId()
        .equals(applicationId)) {
      LOG.info(RequestConstants.APPLICATION_ID_THE_SAME_MESSAGE);
      return ResponseEntity.ok(new BaseResponse(RequestConstants.APPLICATION_ID_THE_SAME_MESSAGE));
    }
    applicationManagementService.stopApplication();

    return ResponseEntity.ok().body(new BaseResponse(SUCCESS_RESPONSE));
  }
}
