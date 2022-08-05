package org.edmcouncil.spec.ontoviewer.webapp.configuration;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.YamlFileBasedConfigurationService;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

  private final FileSystemService fileSystemService;

  public ApplicationConfiguration(FileSystemService fileSystemService) {
    this.fileSystemService = fileSystemService;
  }

  @Bean
  public ApplicationConfigurationService getConfigurationService() {
    return new YamlFileBasedConfigurationService(fileSystemService);
  }
}