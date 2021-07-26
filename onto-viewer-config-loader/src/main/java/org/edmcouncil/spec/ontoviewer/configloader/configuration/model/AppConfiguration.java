package org.edmcouncil.spec.ontoviewer.configloader.configuration.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.annotation.PostConstruct;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.loader.ConfigLoader;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.ViewerCoreConfiguration;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class AppConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(AppConfiguration.class);

  private final FileSystemManager fileSystemManager;

  private ViewerCoreConfiguration viewerCoreConfig;

  public AppConfiguration(FileSystemManager fileSystemManager) {
    this.fileSystemManager = fileSystemManager;
  }

  @PostConstruct
  public void init() {
    LOG.debug("Start loading configuration.");

    var configLoader = new ConfigLoader();
    Path configFilePath;

    try {
      configFilePath = fileSystemManager.getPathToConfigFile();

      LOG.debug("Path to Configs Directory : {}", configFilePath.toAbsolutePath());
      LOG.debug("Load config");
      LOG.debug("List Files : {}", configFilePath.toFile().listFiles().toString());
      for (File file : configFilePath.toFile().listFiles()) {
        LOG.debug("Path to ConfigFile : {}", file.toPath());
        if (file.isFile()) {
          configLoader.loadWeaselConfiguration(file.toPath());
        }
      }

      this.viewerCoreConfig = configLoader.getConfiguration();

    } catch (IOException ex) {
      LOG.error("[ERROR] IOException while loading config file");
    }

    this.viewerCoreConfig = configLoader.getConfiguration();

    if (!viewerCoreConfig.isEmpty()) {
      LOG.debug("Configuration: ");
      viewerCoreConfig.getConfiguration()
          .entrySet()
          .stream()
          .map((entry) -> {
            LOG.debug("\t{}", entry.getKey());
            return entry;
          }).forEachOrdered((entry) -> {
        entry.getValue()
            .stream()
            .map((object) -> object.toString())
            .forEachOrdered((s) -> {
              LOG.debug("\t\t{}", s);
            });
      });
    } else {
      LOG.debug("Use default weasel configuration");
    }

  }

  public ViewerCoreConfiguration getViewerCoreConfig() {
    return viewerCoreConfig;
  }

}
