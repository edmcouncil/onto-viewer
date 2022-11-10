package org.edmcouncil.spec.ontoviewer.webapp.service;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ApplicationIdService {

  private final String id = UUID.randomUUID().toString();

  public String getId() {
    return id;
  }


}
