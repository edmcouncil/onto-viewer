package org.edmcouncil.spec.ontoviewer.toolkit.config;

import java.io.IOException;
import java.nio.file.Path;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemService;
import org.springframework.stereotype.Service;

@Service
public class OntoViewerToolkitFileSystemManager implements FileSystemService {

  @Override
  public Path getViewerHomeDir() {
    return Path.of("");
  }

  @Override
  public Path getDefaultPathToOntologyFile() throws IOException {
    return null;
  }

  @Override
  public Path getPathToConfigDownloadDirectory() throws IOException {
    return null;
  }

  @Override
  public Path getPathToApiKey() {
    return null;
  }

  @Override
  public Path getPathToDefaultConfigDirectory() throws IOException {
    return null;
  }
}
