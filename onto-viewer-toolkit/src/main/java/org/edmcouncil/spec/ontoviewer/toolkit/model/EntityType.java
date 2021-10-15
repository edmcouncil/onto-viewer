package org.edmcouncil.spec.ontoviewer.toolkit.model;

public enum EntityType {
  DATA_PROPERTY("DataProperty"),
  DATATYPE("Datatype"),
  CLASS("Class"),
  INDIVIDUAL("Individual"),
  OBJECT_PROPERTY("ObjectProperty");

  private final String displayName;

  EntityType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
