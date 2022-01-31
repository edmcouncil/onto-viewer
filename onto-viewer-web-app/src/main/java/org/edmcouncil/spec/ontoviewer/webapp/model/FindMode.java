package org.edmcouncil.spec.ontoviewer.webapp.model;

public enum FindMode {
  BASIC,
  ADVANCE;

  public static FindMode getMode(String name) {
    for (FindMode findMode : values()) {
      if (findMode.name().equalsIgnoreCase(name)) {
        return findMode;
      }
    }
    return null;
  }
}
