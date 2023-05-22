package org.edmcouncil.spec.ontoviewer.configloader.utils.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.properties.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class FileSystemManager implements FileSystemService {

  private static final Logger LOG = LoggerFactory.getLogger(FileSystemManager.class);
  private static final String ONTO_VIEWER_DEFAULT_HOME_DIR_NAME = ".viewer";
  private static final String DEFAULT_CONFIG_DIRECTORY = "config";

  private final String defaultHomePath;
  private final String viewerConfigFileName;
  private final String defaultOntologyFileName;
  @Value("${app.config.updateUrl}")
  private String[] configUpdateUrls;

  public FileSystemManager(AppProperties appProperties) {
    defaultHomePath = appProperties.getDefaultHomePath();
    viewerConfigFileName = appProperties.getViewerConfigFileName();
    defaultOntologyFileName = appProperties.getDefaultOntologyFileName();
  }

  @Override
  public Path getViewerHomeDir() {
    Path userHomeDir;
    switch (defaultHomePath) {
      case "user.home":
        String userHomeProperty = System.getProperty("user.home");
        userHomeDir = Paths.get(userHomeProperty);
        LOG.trace("User home dir is '{}'.", userHomeDir);
        userHomeDir = userHomeDir.resolve(ONTO_VIEWER_DEFAULT_HOME_DIR_NAME);
        break;
      case "*":
        userHomeDir = Paths.get("");
        LOG.trace("Working directory is '{}'.", userHomeDir.toAbsolutePath());
        break;
      default:
        userHomeDir = Paths.get(defaultHomePath);
        LOG.debug(
            "Application working directory determined on 'app.defaultHomePath' from the property file: {}",
            defaultHomePath);
        break;
    }
    return userHomeDir;
  }

  @Override
  public Path getDefaultPathToOntologyFile() throws IOException {
    Path homeDir = getViewerHomeDir();
    return createDirIfNotExists(homeDir).resolve(defaultOntologyFileName);
  }

  @Override
  public Path getPathToConfigFilesOrDefault() throws IOException {

    if (configUpdateUrls != null) {
      for (String updateUrl : configUpdateUrls) {
        String configPath = null;
        if (updateUrl.startsWith("file:")) {
          configPath = updateUrl.substring(7);
        }
        LOG.info("UpdateUrl7: {}", updateUrl);
        Path path = null;
        try {
          path = Paths.get(configPath);
        } catch (InvalidPathException e) {
          LOG.trace("This is not local path.");
        }
        LOG.info("Path2: {}", path);
        if (path != null) {
          if (path.isAbsolute()) {
            if (!path.toFile().exists()) {
              throw new IOException("Path {} does not exist.");
            }
            return path;
          } else {
            var homePath = getViewerHomeDir();
            var configDirPath = homePath.resolve(configPath);
            if (!configDirPath.toFile().exists()) {
              throw new IOException("Path {} does not exist.");
            }
            return configDirPath;
          }
        }
      }
    }
    return createDirIfNotExists(getViewerHomeDir().resolve(DEFAULT_CONFIG_DIRECTORY));
  }

  @Override
  public Path getPathToApiKey() {
    return getViewerHomeDir().resolve("api.key");
  }
}
