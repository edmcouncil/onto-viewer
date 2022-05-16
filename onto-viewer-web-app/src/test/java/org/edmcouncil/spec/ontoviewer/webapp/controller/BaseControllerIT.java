package org.edmcouncil.spec.ontoviewer.webapp.controller;

import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.OntologiesConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.YamlMemoryBasedConfigurationService;
import org.edmcouncil.spec.ontoviewer.webapp.boot.UpdateBlocker;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@SpringBootTest
@AutoConfigureMockMvc
public abstract class BaseControllerIT {

  @Autowired
  private UpdateBlocker updateBlocker;

  @BeforeEach
  public void setUp() {
    waitUntilServiceReady();
  }

  @Configuration
  static class IntegrationTestsConfiguration {

    @Primary
    @Bean
    ApplicationConfigurationService getApplicationConfigurationService() {
      var yamlMemoryBasedConfigurationService = new YamlMemoryBasedConfigurationService();
      var ontologiesConfig = yamlMemoryBasedConfigurationService
          .getConfigurationData()
          .getOntologiesConfig();
      ontologiesConfig.getPaths().clear();
      ontologiesConfig.getPaths().add("integration_tests/ontologies");
      return yamlMemoryBasedConfigurationService;
    }
  }

  private void waitUntilServiceReady() {
    await()
        .atMost(60, TimeUnit.SECONDS)
        .until(() -> updateBlocker.isInitializeAppDone());
  }
}