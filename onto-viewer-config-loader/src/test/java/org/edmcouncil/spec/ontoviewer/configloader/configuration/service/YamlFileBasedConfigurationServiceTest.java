package org.edmcouncil.spec.ontoviewer.configloader.configuration.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.properties.AppProperties;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class YamlFileBasedConfigurationServiceTest {

  public static final String CONFIG_DIR = "config";

  @TempDir
  Path homeDir;

  @Test
  void shouldReadDefaultConfig() {
    var yamlConfigurationService = new YamlFileBasedConfigurationService(prepareFileSystem());
    yamlConfigurationService.init();



    System.out.println(yamlConfigurationService);
  }

  private FileSystemManager prepareFileSystem() {
    Path configDir = homeDir.resolve(CONFIG_DIR).toAbsolutePath();
    try {
      Files.createDirectory(configDir);
    } catch (IOException ex) {
      throw new IllegalStateException("Unable to create test config dir.", ex);
    }

    var appProperties = new AppProperties();
    appProperties.setConfigPath(configDir.toString());
    return new FileSystemManager(appProperties);
  }
}