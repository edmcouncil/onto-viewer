package org.edmcouncil.spec.ontoviewer.core.mapping;

public enum EntityType {
  DATA_PROPERTY("Data Property"),
  DATATYPE("Datatype"),
  CLASS("Class"),
  INDIVIDUAL("Individual"),
  OBJECT_PROPERTY("Object Property");

  private final String displayName;

  EntityType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
