package org.edmcouncil.spec.ontoviewer.webapp.util;

import java.util.UUID;

public class ApiKeyGenerator {

  public static String generateApiKey() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
