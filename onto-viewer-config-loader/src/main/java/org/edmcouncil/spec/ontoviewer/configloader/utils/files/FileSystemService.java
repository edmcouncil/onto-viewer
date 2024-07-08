package org.edmcouncil.spec.ontoviewer.configloader.utils.files;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface FileSystemService {

  Path getViewerHomeDir();

  default void createDir(Path dirPath) throws IOException {
    try {
      Files.createDirectory(dirPath);
    } catch (IOException ex) {
      var msg = String.format("Unable to create a dir '%s'.", dirPath);
      throw new IOException(msg, ex);
    }
  }

  Path getDefaultPathToOntologyFile() throws IOException;

  default Path getPathToFile(String pathString) throws IOException {
    var path = Paths.get(pathString);
    if (path.isAbsolute()) {
      return path;
    } else {
      Path homeDir = getViewerHomeDir();
      return createDirIfNotExists(homeDir).resolve(path);
    }
  }

  Path getPathToConfigDownloadDirectory() throws IOException;

  Path getPathToApiKey();

  default Path createDirIfNotExists(Path dirToCreate) throws IOException {
    if (Files.notExists(dirToCreate)) {
      createDir(dirToCreate);
    }
    return dirToCreate;
  }

  default String readFileContent(Path path) throws IOException {
    return Files.readString(path);
  }

  Path getPathToDefaultConfigDirectory() throws IOException;
}
