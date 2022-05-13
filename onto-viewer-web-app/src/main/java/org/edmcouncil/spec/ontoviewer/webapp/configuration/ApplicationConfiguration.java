package org.edmcouncil.spec.ontoviewer.webapp.configuration;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.properties.AppProperties;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ConfigurationService;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.FileBasedConfigurationService;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

  private final FileSystemManager fileSystemManager;
  private final AppProperties appProperties;

  public ApplicationConfiguration(FileSystemManager fileSystemManager, AppProperties appProperties) {
    this.fileSystemManager = fileSystemManager;
    this.appProperties = appProperties;
  }

  @Bean
  public ConfigurationService getConfigurationService() {
    return new FileBasedConfigurationService(fileSystemManager, appProperties);
  }
}