package org.edmcouncil.spec.ontoviewer.core.utils;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class UrlChecker {

  private static final String URL_PATTERN = "^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";

  public static boolean isUrl(String str) {
    return str.matches(URL_PATTERN);
  }

}
