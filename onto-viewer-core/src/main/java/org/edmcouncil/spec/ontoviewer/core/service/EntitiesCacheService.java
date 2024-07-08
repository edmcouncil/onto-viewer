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
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
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
    EntityKey entityKey = new EntityKey(entityIri, owlType);
    return getOrCreateEntityEntry(entityKey);
  }

  public EntityEntry getEntityEntry(OWLAnonymousIndividual anonymousIndividual) {
    EntityKey entityKey = new EntityKey(anonymousIndividual);
    return getOrCreateEntityEntry(entityKey);
  }

  private EntityEntry getOrCreateEntityEntry(EntityKey entityKey) {
    entityEntryMap.computeIfAbsent(entityKey, this::createEntityEntry);
    return entityEntryMap.get(entityKey);
  }

  private EntityEntry createEntityEntry(EntityKey key) {
    Optional<? extends OWLEntity> entity = Optional.empty();
    if (key.getIri() != null) {
      entity = findEntityByIRI(key.getIri(), key.getOwlType());
    } else if (key.getAnonymousIndividual() != null) {
      //todo Handle the creation of EntityEntry for OWLAnonymousIndividual
    }
    return new EntityEntry((Optional<OWLEntity>) entity);
  }

  private Optional<? extends OWLEntity> findEntityByIRI(IRI entityIri, OwlType owlType) {
    return ontologyManager.getOntology()
            .entitiesInSignature(entityIri, Imports.INCLUDED)
            .filter(owlEntity -> getOwlEntityType(owlType).isAssignableFrom(owlEntity.getClass()))
            .findFirst();
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
        return OWLEntity.class;
    }
  }

  static class EntityKey {

    private final IRI iri;
    private final OwlType owlType;
    private final OWLAnonymousIndividual anonymousIndividual;

    public EntityKey(IRI iri, OwlType owlType) {
      this.iri = iri;
      this.owlType = owlType;
      this.anonymousIndividual = null;
    }

    public EntityKey(OWLAnonymousIndividual anonymousIndividual) {
      this.iri = null;
      this.owlType = null;
      this.anonymousIndividual = anonymousIndividual;
    }

    public IRI getIri() {
      return iri;
    }

    public OwlType getOwlType() {
      return owlType;
    }

    public OWLAnonymousIndividual getAnonymousIndividual() {
      return anonymousIndividual;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      EntityKey entityKey = (EntityKey) o;
      return Objects.equals(iri, entityKey.iri) &&
              owlType == entityKey.owlType &&
              Objects.equals(anonymousIndividual, entityKey.anonymousIndividual);
    }

    @Override
    public int hashCode() {
      return Objects.hash(iri, owlType, anonymousIndividual);
    }
  }
}