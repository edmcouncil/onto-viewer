package org.edmcouncil.spec.ontoviewer.core.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.edmcouncil.spec.ontoviewer.core.model.EntityEntry;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.springframework.stereotype.Service;

@Service
public class EntitiesCacheService {

  private final OntologyManager ontologyManager;
  private final Map<EntityKey, EntityEntry> entityEntryMap;

  public EntitiesCacheService(OntologyManager ontologyManager) {
    this.ontologyManager = ontologyManager;
    this.entityEntryMap = new HashMap<>();
  }

  public EntityEntry getEntityEntry(IRI entityIri, OwlType owlType) {
    var entityKey = new EntityKey(entityIri, owlType);

    if (!entityEntryMap.containsKey(entityKey)) {
      var entity = obtainEntity(entityIri, getOwlEntityType(owlType));
      EntityEntry entityEntry;
      if (entity != null) {
        entityEntry = new EntityEntry(Optional.of(entity));
      } else {
        entityEntry = new EntityEntry(Optional.empty());
      }
      entityEntryMap.put(entityKey, entityEntry);
    }

    return entityEntryMap.get(entityKey);
  }

  private Class<? extends OWLEntity> getOwlEntityType(OwlType owlType) {
    switch (owlType) {
      case CLASS:
        return OWLClass.class;
      case INDIVIDUAL:
        return OWLNamedIndividual.class;
      case OBJECT_PROPERTY:
        return OWLObjectProperty.class;
      case DATA_PROPERTY:
        return OWLDataProperty.class;
      case ANNOTATION_PROPERTY:
        return OWLAnnotationProperty.class;
      case DATATYPE:
        return OWLDatatype.class;
      default:
        // Shouldn't happen but for the sake of completeness it must be here
        return OWLEntity.class;
    }
  }

  private OWLEntity obtainEntity(IRI entityIri, Class<? extends OWLEntity> entityType) {
    return ontologyManager.getOntology()
        .entitiesInSignature(entityIri, Imports.INCLUDED)
        .filter(owlEntity -> entityType.isAssignableFrom(owlEntity.getClass()))
        .findFirst()
        .orElse(null);
  }

  static class EntityKey {

    private final IRI iri;
    private final OwlType owlType;

    public EntityKey(IRI iri, OwlType owlType) {
      this.iri = iri;
      this.owlType = owlType;
    }

    public IRI getIri() {
      return iri;
    }

    public OwlType getOwlType() {
      return owlType;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      EntityKey entityKey = (EntityKey) o;
      return Objects.equals(iri, entityKey.iri) && owlType == entityKey.owlType;
    }

    @Override
    public int hashCode() {
      return Objects.hash(iri, owlType);
    }
  }
}