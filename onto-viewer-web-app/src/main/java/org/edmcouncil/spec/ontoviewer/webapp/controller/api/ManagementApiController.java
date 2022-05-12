package org.edmcouncil.spec.ontoviewer.webapp.controller.api;

import static org.edmcouncil.spec.ontoviewer.webapp.common.RequestConstants.API_KEY_NOT_VALID_MESSAGE;
import static org.edmcouncil.spec.ontoviewer.webapp.common.RequestConstants.SUCCESS_RESPONSE;

import org.edmcouncil.spec.ontoviewer.webapp.common.RequestConstants;
import org.edmcouncil.spec.ontoviewer.webapp.model.BaseResponse;
import org.edmcouncil.spec.ontoviewer.webapp.model.ErrorResponse;
import org.edmcouncil.spec.ontoviewer.webapp.service.ApiKeyService;
import org.edmcouncil.spec.ontoviewer.webapp.service.ApplicationManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ManagementApiController {

  private final ApiKeyService apiKeyService;
  private final ApplicationManagementService applicationManagementService;

  public ManagementApiController(ApiKeyService apiKeyService,
      ApplicationManagementService applicationManagementService) {
    this.apiKeyService = apiKeyService;
    this.applicationManagementService = applicationManagementService;
  }

  @PutMapping("/api/")
  public ResponseEntity<BaseResponse> stopApplication(
      @RequestHeader(value = RequestConstants.X_API_KEY) String apiKeyHeader) {

    if (!apiKeyService.validateApiKey(apiKeyHeader)) {
      return ResponseEntity.badRequest().body(
          new ErrorResponse(API_KEY_NOT_VALID_MESSAGE, null));
    }

    applicationManagementService.stopApplication();

    return ResponseEntity.ok().body(new BaseResponse(SUCCESS_RESPONSE));
  }
}
