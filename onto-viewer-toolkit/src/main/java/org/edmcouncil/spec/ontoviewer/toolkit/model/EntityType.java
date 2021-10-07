package org.edmcouncil.spec.ontoviewer.toolkit.model;

public enum EntityType {
  CLASS("Class"),
  INDIVIDUAL("Individual");

  private final String displayName;

  EntityType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
