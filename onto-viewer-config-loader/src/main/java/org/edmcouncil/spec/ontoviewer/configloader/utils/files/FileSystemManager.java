package org.edmcouncil.spec.ontoviewer.configloader.utils.files;

import java.io.IOException;
import java.nio.file.Files;
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
public class FileSystemManager {

  private static final String WEASEL_DEFAULT_HOME_DIR_NAME = ".weasel";

  private static final Logger LOG = LoggerFactory.getLogger(FileSystemManager.class);

  private final String defaultHomePath;
  private final String viewerConfigFileName;
  private final String defaultOntologyFileName;
  private final String configPath;

  public FileSystemManager(AppProperties appProperties) {
    defaultHomePath = appProperties.getDefaultHomePath();
    viewerConfigFileName = appProperties.getViewerConfigFileName();
    defaultOntologyFileName = appProperties.getDefaultOntologyFileName();
    configPath = appProperties.getConfigPath();
  }

  public Path getViewerHomeDir() {
    Path userHomeDir;
    switch (defaultHomePath) {
      case "user.home":
        String userHomeProperty = System.getProperty("user.home");
        userHomeDir = Paths.get(userHomeProperty);
        LOG.trace("User home dir is '{}'.", userHomeDir);
        userHomeDir = userHomeDir.resolve(WEASEL_DEFAULT_HOME_DIR_NAME);
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

  private Path createDirIfNotExists(Path dirToCreate) throws IOException {
    if (Files.notExists(dirToCreate)) {
      createDir(dirToCreate);
    }
    return dirToCreate;
  }

  public void createDir(Path dirPath) throws IOException {
    try {
      Files.createDirectory(dirPath);
    } catch (IOException ex) {
      var msg = String.format("Unable to create a dir '%s'.", dirPath);
      throw new IOException(msg, ex);
    }
  }

  public Path getDefaultPathToOntologyFile() throws IOException {
    Path homeDir = getViewerHomeDir();
    return createDirIfNotExists(homeDir).resolve(defaultOntologyFileName);
  }

  public Path getPathToFile(String pathString) throws IOException {
    var path = Paths.get(pathString);
    if (path.isAbsolute()) {
      return path;
    } else {
      Path homeDir = getViewerHomeDir();
      return createDirIfNotExists(homeDir).resolve(path);
    }
  }

  public Path getPathToConfigFile() throws IOException {
    var path = Paths.get(configPath);
    if (path.isAbsolute()) {
      return path;
    } else {
      var homePath = getViewerHomeDir();
      var configDirPath = createDirIfNotExists(homePath).resolve(configPath);
      return createDirIfNotExists(configDirPath);
    }
  }

  public Path getPathToApiKey() {
    return getViewerHomeDir().resolve("api.key");
  }

}