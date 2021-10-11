package org.edmcouncil.spec.ontoviewer.webapp.service;

import java.io.IOException;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ApiKeyService {

  private static final Logger LOG = LoggerFactory.getLogger(ApiKeyService.class);

  private final FileService fileService;

  private String apiKey = null;

  public ApiKeyService(FileService fileService) {
    this.fileService = fileService;
  }

  @PostConstruct
  public void init() {
    try {
      this.apiKey = fileService.getApiKeyFromFile();
    } catch (IOException ex) {
      LOG.error("Cannot load or create file with apiKey. Error: {}", ex.getMessage());
    }
  }

  public boolean validateApiKey(String keyToCheck) {
    return apiKey.equals(keyToCheck);
  }
}