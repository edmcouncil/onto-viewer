package org.edmcouncil.spec.ontoviewer.core.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.edmcouncil.spec.ontoviewer.core.model.EntityEntry;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.springframework.stereotype.Service;

@Service
public class EntitiesCacheService {

  private final OntologyManager ontologyManager;
  private final Map<IRI, EntityEntry> entityEntryMap;

  public EntitiesCacheService(OntologyManager ontologyManager) {
    this.ontologyManager = ontologyManager;
    this.entityEntryMap = new HashMap<>();
  }

  public EntityEntry getEntityEntry(IRI entityIri) {
    if (!entityEntryMap.containsKey(entityIri)) {
      var entity = obtainEntity(entityIri);
      EntityEntry entityEntry;
      if (entity != null) {
        entityEntry = new EntityEntry(Optional.of(entity));
      } else {
        entityEntry = new EntityEntry(Optional.empty());
      }
      entityEntryMap.put(entityIri, entityEntry);
    }

    return entityEntryMap.get(entityIri);
  }

  private OWLEntity obtainEntity(IRI entityIri) {
    return ontologyManager.getOntology()
        .entitiesInSignature(entityIri, Imports.INCLUDED)
        .findFirst()
        .orElse(null);
  }
}