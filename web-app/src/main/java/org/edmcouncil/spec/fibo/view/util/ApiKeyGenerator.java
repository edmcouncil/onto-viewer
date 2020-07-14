package org.edmcouncil.spec.fibo.view.util;

import java.util.UUID;

public class ApiKeyGenerator {

  public static String generateApiKey() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
