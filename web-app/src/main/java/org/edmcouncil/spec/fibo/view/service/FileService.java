package org.edmcouncil.spec.fibo.view.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.edmcouncil.spec.fibo.config.utils.files.FileSystemManager;
import org.edmcouncil.spec.fibo.view.util.ApiKeyGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class FileService {

  @Autowired
  private FileSystemManager fsm;
  
  
  public String getApiKeyFromFile() throws IOException{
    String res = null;
    Path apiKey = fsm.getPathToApiKey();
    File keyFile = apiKey.toFile();
    if(keyFile.exists()){
      res = new String(Files.readAllBytes(apiKey));
    } else {
      res = ApiKeyGenerator.generateApiKey();
      Files.write(apiKey, res.getBytes());
    }
    return res;
  }
  
}
