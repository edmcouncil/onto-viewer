package org.edmcouncil.spec.fibo.view.service;

import java.io.IOException;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApiKeyService {

  private static final Logger LOG = LoggerFactory.getLogger(ApiKeyService.class);

  private String apiKey = null;

  @Autowired
  private FileService fs;

  @PostConstruct
  public void init() {
    try {
      this.apiKey = fs.getApiKeyFromFile();
    } catch (IOException ex) {
      LOG.error("Cannot load or create file with apiKey. Error: " + ex.getMessage());
    }
  }

  public Boolean validateApiKey(String keyToCheck) {
    return keyToCheck.equals(apiKey);
  }

}
