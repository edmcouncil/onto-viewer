package org.edmcouncil.spec.ontoviewer.configloader.configuration.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.PostConstruct;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.loader.ConfigLoader;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.CoreConfiguration;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileBasedConfigurationService implements ConfigurationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileBasedConfigurationService.class);

  private final FileSystemManager fileSystemManager;
  private CoreConfiguration coreConfiguration;

  public FileBasedConfigurationService(FileSystemManager fileSystemManager) {
    this.fileSystemManager = fileSystemManager;
  }

  @PostConstruct
  public void init() {
    LOGGER.debug("Start loading configuration.");

    var configLoader = new ConfigLoader();
    Path configFilePath;

    try {
      configFilePath = fileSystemManager.getPathToConfigFile();

      LOGGER.debug("Path to Configs Directory : {}", configFilePath.toAbsolutePath());
      LOGGER.debug("Load config");
      LOGGER.debug("List Files : {}", configFilePath.toFile().listFiles().toString());
      for (File file : configFilePath.toFile().listFiles()) {
        LOGGER.debug("Path to ConfigFile : {}", file.toPath());
        if (file.isFile()) {
          configLoader.loadWeaselConfiguration(file.toPath());
        }
      }

      this.coreConfiguration = configLoader.getConfiguration();

    } catch (IOException ex) {
      LOGGER.error("[ERROR] IOException while loading config file");
    }

    this.coreConfiguration = configLoader.getConfiguration();

    if (!coreConfiguration.isEmpty()) {
      LOGGER.debug("Configuration: ");
      coreConfiguration.getConfiguration()
          .entrySet()
          .stream()
          .map((entry) -> {
            LOGGER.debug("\t{}", entry.getKey());
            return entry;
          }).forEachOrdered((entry) -> {
            entry.getValue()
                .stream()
                .map((object) -> object.toString())
                .forEachOrdered((s) -> {
                  LOGGER.debug("\t\t{}", s);
                });
          });
    } else {
      LOGGER.debug("Use default weasel configuration");
    }
  }

  @Override
  public CoreConfiguration getCoreConfiguration() {
    if (this.coreConfiguration == null) {
      throw new IllegalStateException("Core configuration is not initialized.");
    }

    return this.coreConfiguration;
  }
}