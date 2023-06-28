package org.edmcouncil.spec.ontoviewer.configloader.utils.files;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.properties.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class FileSystemManager implements FileSystemService {

  private static final String VIEWER_DEFAULT_HOME_DIR_NAME = ".onto-viewer";
  private static final String DEFAULT_CONFIG_LOCATION = "config";

  private static final Logger LOG = LoggerFactory.getLogger(FileSystemManager.class);

  private final String defaultHomePath;
  private final String defaultOntologyFileName;
  private String configDownloadPath;

  public FileSystemManager(AppProperties appProperties) {
    defaultHomePath = appProperties.getDefaultHomePath()!=null?appProperties.getDefaultHomePath():"";
    defaultOntologyFileName = appProperties.getDefaultOntologyFileName();
    if (appProperties.getConfigDownloadPath() == null || appProperties.getConfigDownloadPath().isEmpty()) {
      configDownloadPath = DEFAULT_CONFIG_LOCATION;
    } else {
      configDownloadPath = appProperties.getConfigDownloadPath();
    }
  }

  @Override
  public Path getViewerHomeDir() {
    Path userHomeDir;
    switch (defaultHomePath) {
      case "user.home":
        String userHomeProperty = System.getProperty("user.home");
        userHomeDir = Paths.get(userHomeProperty);
        LOG.trace("User home dir is '{}'.", userHomeDir);
        userHomeDir = userHomeDir.resolve(VIEWER_DEFAULT_HOME_DIR_NAME);
        break;
      case "*":
        userHomeDir = Paths.get("");
        LOG.trace("Working directory is '{}'.", userHomeDir.toAbsolutePath());
        break;
      default:
        userHomeDir = Paths.get(defaultHomePath);
        LOG.debug(
            "Application working directory determined on 'app.defaultHomePath' from the property"
                + " file: {}",
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
  public Path getPathToConfigDownloadDirectory() throws IOException {
        var path = Paths.get(configDownloadPath);
    if (path.isAbsolute()) {
      return path;
    } else {
      var homePath = getViewerHomeDir();
      var configDirPath = createDirIfNotExists(createDirIfNotExists(homePath).resolve(configDownloadPath));
      return configDirPath;
    }
  }

  @Override
  public Path getPathToApiKey() {
    return getViewerHomeDir().resolve("api.key");
  }

  @Override
  public Path getPathToDefaultConfigDirectory() throws IOException {
    return getPathToFile(DEFAULT_CONFIG_LOCATION).toAbsolutePath();
  }

}
