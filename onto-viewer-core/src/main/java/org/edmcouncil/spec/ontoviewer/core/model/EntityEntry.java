package org.edmcouncil.spec.ontoviewer.core.model;

import java.util.Optional;
import org.semanticweb.owlapi.model.OWLEntity;

public class EntityEntry {

  private final Optional<OWLEntity> entity;

  public EntityEntry(Optional<OWLEntity> entity) {
    this.entity = entity;
  }

  public boolean isPresent() {
    return entity.isPresent();
  }

  public Optional<OWLEntity> getEntity() {
    return entity;
  }

  public <T extends OWLEntity> T getEntityAs(Class<T> type) {
    try {
      return (T) entity.orElseThrow(() -> new IllegalStateException("Entity is not present."));
    } catch (ClassCastException ex) {
      throw new IllegalStateException("Unable to cast entity to type " + type.getName());
    }
  }
}