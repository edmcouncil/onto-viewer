package org.edmcouncil.spec.ontoviewer.configloader.configuration.service;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.loader.ConfigLoader;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.CoreConfiguration;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.properties.AppProperties;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileBasedConfigurationService implements ConfigurationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedConfigurationService.class);

  private final FileSystemManager fileSystemManager;
  private final AppProperties appProperties;

  private CoreConfiguration coreConfiguration;

  public FileBasedConfigurationService(FileSystemManager fileSystemManager, AppProperties appProperties) {
    this.fileSystemManager = fileSystemManager;
    this.appProperties = appProperties;
  }

  @PostConstruct
  public void init() {
    LOGGER.debug("Loading configuration...");

    var configLoader = new ConfigLoader(appProperties);

    try {
      var configFilePath = fileSystemManager.getPathToConfigFile();

      LOGGER.debug("Path to config directory: {}", configFilePath.toAbsolutePath());
      if (!Files.isDirectory(configFilePath)) {
        throw new IllegalStateException(
            String.format("Config file path '%s' is not a directory.", configFilePath));
      }

      try (var paths = Files.list(configFilePath)) {
        for (var path : paths.collect(Collectors.toList())) {
          LOGGER.debug("Adding configuration from file '{}'.", path);
          if (Files.isRegularFile(path)) {
            configLoader.loadViewerConfiguration(path);
          }
        }
      }
    } catch (IOException ex) {
      LOGGER.error("Exception thrown while loading config files", ex);
    }

    this.coreConfiguration = configLoader.getConfiguration();
    this.coreConfiguration.logConfigurationDebugInfo();
  }

  @Override
  public CoreConfiguration getCoreConfiguration() {
    if (this.coreConfiguration == null) {
      throw new IllegalStateException("Core configuration is not initialized.");
    }

    return this.coreConfiguration;
  }
}