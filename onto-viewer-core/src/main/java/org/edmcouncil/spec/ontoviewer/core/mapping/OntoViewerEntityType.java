package org.edmcouncil.spec.ontoviewer.core.mapping;

import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLEntity;

public enum OntoViewerEntityType {
  DATA_PROPERTY("Data Property"),
  DATATYPE("Datatype"),
  CLASS("Class"),
  INDIVIDUAL("Individual"),
  ANONYMOUS_INDIVIDUAL("Anonymous individual"),
  OBJECT_PROPERTY("Object Property"),
  UNKNOWN("Unknown");

  private final String displayName;

  OntoViewerEntityType(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public static OntoViewerEntityType fromEntityType(OWLEntity entity) {
    if (entity.getEntityType().equals(EntityType.CLASS)) {
      return OntoViewerEntityType.CLASS;
    } else if (entity.getEntityType().equals(EntityType.NAMED_INDIVIDUAL)) {
      return OntoViewerEntityType.INDIVIDUAL;
    } else if (entity.getEntityType().equals(EntityType.OBJECT_PROPERTY)) {
      return OntoViewerEntityType.OBJECT_PROPERTY;
    } else if (entity.getEntityType().equals(EntityType.DATA_PROPERTY)) {
      return OntoViewerEntityType.DATA_PROPERTY;
    } else if (entity.getEntityType().equals(EntityType.DATATYPE)) {
      return OntoViewerEntityType.DATATYPE;
    } else {
      return OntoViewerEntityType.UNKNOWN;
    }
  }

  public static OntoViewerEntityType fromEntityType(String anonymous) {
    if (anonymous.startsWith("_:genid")) {
      return OntoViewerEntityType.ANONYMOUS_INDIVIDUAL;
    } else {
      return OntoViewerEntityType.UNKNOWN;
    }
  }
}