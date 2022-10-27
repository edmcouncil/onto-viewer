package org.edmcouncil.spec.ontoviewer.webapp.service;

import java.io.IOException;
import java.util.Collections;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.OwlDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.model.UpdateJob;
import org.edmcouncil.spec.ontoviewer.webapp.configuration.FallbackConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class FallbackService {

  private static final Logger LOG = LoggerFactory.getLogger(FallbackService.class);
  private final RestTemplate restTemplate;
  private String apiKey;
  private String fallbackUrl;

  public FallbackService(RestTemplateBuilder restTemplateBuilder,
      FileService fileService,
      FallbackConfig fallbackConfig)
      throws IOException {
    this.restTemplate = restTemplateBuilder.build();
    this.apiKey = fileService.getApiKeyFromFile();
    this.fallbackUrl = fallbackConfig.getFallbackUrl();
  }

  public boolean isFallbackEmpty() {
    return fallbackUrl.isEmpty();
  }

  public void sendRequestToNextInstance(String id) {
    if (isFallbackEmpty()) {
      return;
    }
    if (id.equals("0")) {
      makeRestartRequest();
    } else {
      makeUpdateRequest();
    }
  }

  private void makeUpdateRequest() {
    String url = fallbackUrl.concat(fallbackUrl.endsWith("/") ? "" : "/").concat("api/update");
    HttpHeaders headers = createHeaders();
    this.restTemplate.put(url, headers);

    ResponseEntity<UpdateJob> response = getUpdateJobResponseEntity(
        url, headers);

    if (response.getStatusCode() == HttpStatus.OK) {
      LOG.info("Update response from next instance: {}", response.getBody());
    } else {
      LOG.warn("Failed update response from next instance: {}", response.getBody());
    }
  }

  private void makeRestartRequest() {
    String url = fallbackUrl.concat(fallbackUrl.endsWith("/") ? "" : "/").concat("api/");
    HttpHeaders headers = createHeaders();
    ResponseEntity<UpdateJob> response = getUpdateJobResponseEntity(
        url, headers);
    if (response.getStatusCode() == HttpStatus.OK) {
      LOG.info("Restart response from next instance: {}", response.getBody());
    } else {
      LOG.warn("Failed restart response from next instance: {}", response.getBody());
    }
  }

  private ResponseEntity<UpdateJob> getUpdateJobResponseEntity(String url, HttpHeaders headers) {
    HttpEntity<String> entity = new HttpEntity<>("", headers);
    ResponseEntity<UpdateJob> response = this.restTemplate.exchange(url, HttpMethod.PUT, entity,
        UpdateJob.class);
    return response;
  }

  private HttpHeaders createHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.add("X-API-KEY", this.apiKey);
    return headers;
  }

  public String getFallbackUrl() {
    return fallbackUrl;
  }

  public void setFallbackUrl(String fallbackUrl) {
    this.fallbackUrl = fallbackUrl;
  }
}
