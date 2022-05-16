package org.edmcouncil.spec.ontoviewer.webapp.configuration;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.YamlFileBasedConfigurationService;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

  private final FileSystemManager fileSystemManager;

  public ApplicationConfiguration(FileSystemManager fileSystemManager) {
    this.fileSystemManager = fileSystemManager;
  }

  @Bean
  public ApplicationConfigurationService getConfigurationService() {
    return new YamlFileBasedConfigurationService(fileSystemManager);
  }
}