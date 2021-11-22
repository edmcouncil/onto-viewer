package org.edmcouncil.spec.ontoviewer.core.model;

import java.util.Optional;
import org.edmcouncil.spec.ontoviewer.core.exception.OntoViewerException;
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

  public <T extends OWLEntity> T getEntityAs(Class<T> type) throws OntoViewerException {
    var entityObject = entity.orElseThrow(
        () -> new IllegalStateException("Entity is not present."));
    if (type.isAssignableFrom(entityObject.getClass())) {
      return type.cast(entityObject);
    } else {
      var entityIri = entity.map(owlEntity -> owlEntity.getIRI().toString())
          .orElse("<empty>");
      var message = String.format("Unable to cast '%s' entity (of type %s) to type %s.",
          entityIri, entityObject.getClass(), type.getName());
      throw new OntoViewerException(message);
    }
  }
}