package org.edmcouncil.spec.ontoviewer.core.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.edmcouncil.spec.ontoviewer.core.model.EntityEntry;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
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
      var entity = obtainEntity(entityIri);
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

  private OWLEntity obtainEntity(IRI entityIri) {
    return ontologyManager.getOntology()
        .entitiesInSignature(entityIri, Imports.INCLUDED)
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