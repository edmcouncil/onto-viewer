package org.edmcouncil.spec.ontoviewer.webapp.service;

import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.edmcouncil.spec.ontoviewer.webapp.util.ApiKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class FileService {

  @Autowired
  private FileSystemManager fileSystemManager;

  public String getApiKeyFromFile() throws IOException {
    String result;
    Path apiKeyPath = fileSystemManager.getPathToApiKey();
    File keyFile = apiKeyPath.toFile();
    if (keyFile.exists()) {
      result = new String(Files.readAllBytes(apiKeyPath));
    } else {
      result = ApiKeyGenerator.generateApiKey();
      Files.write(apiKeyPath, result.getBytes());
    }
    return result;
  }

}
