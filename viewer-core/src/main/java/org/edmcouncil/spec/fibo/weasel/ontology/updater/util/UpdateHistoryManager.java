package org.edmcouncil.spec.fibo.weasel.ontology.updater.util;

import com.google.gson.Gson;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.edmcouncil.spec.fibo.config.utils.files.FileSystemManager;
import org.edmcouncil.spec.fibo.weasel.ontology.updater.model.UpdateHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpdateHistoryManager {

  @Autowired
  private FileSystemManager fsm;

  public UpdateHistory loadUpdateHistory() throws IOException {
    Path p = fsm.getPathToUpdateHistory();
    if (!p.toFile().exists()) {
      UpdateHistory uh = new UpdateHistory();
      saveUpdateHistory(uh);
    }
    String json = new String(Files.readAllBytes(p));
    Gson gson = new Gson();
    UpdateHistory uh = gson.fromJson(json, UpdateHistory.class);
    return uh;
  }

  public void saveUpdateHistory(UpdateHistory uh) throws IOException {
    Path p = fsm.getPathToUpdateHistory();
    Gson gson = new Gson();
    String json = gson.toJson(uh);
    Files.write(p, json.getBytes());
  }

}
