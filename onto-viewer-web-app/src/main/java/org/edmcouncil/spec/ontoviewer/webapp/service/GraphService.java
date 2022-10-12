package org.edmcouncil.spec.ontoviewer.webapp.service;

import static org.semanticweb.owlapi.model.parameters.Imports.INCLUDED;

import org.edmcouncil.spec.ontoviewer.core.exception.OntoViewerException;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.graph.OntologyGraph;
import org.edmcouncil.spec.ontoviewer.core.model.graph.viewer.ViewerGraphFactory;
import org.edmcouncil.spec.ontoviewer.core.model.graph.vis.VisGraph;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.RestrictionGraphDataHandler;
import org.edmcouncil.spec.ontoviewer.core.service.EntitiesCacheService;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.springframework.stereotype.Service;

@Service
public class GraphService {

  private RestrictionGraphDataHandler restrictionGraphDataHandler;
  private OntologyManager ontologyManager;
  private EntitiesCacheService entitiesCacheService;

  public GraphService(RestrictionGraphDataHandler restrictionGraphDataHandler, OntologyManager ontologyManager, EntitiesCacheService entitiesCacheService) {
    this.restrictionGraphDataHandler = restrictionGraphDataHandler;
    this.ontologyManager = ontologyManager;
    this.entitiesCacheService = entitiesCacheService;
  }

  public VisGraph handleGraph(String iri, int nodeId, int lastId) throws OntoViewerException {

    IRI entityIri = IRI.create(iri);
    OntologyGraph graph = null;
    if (ontologyManager.getOntology().containsClassInSignature(entityIri, INCLUDED)) {
      var entity = entitiesCacheService.getEntityEntry(entityIri, OwlType.CLASS);
      graph = restrictionGraphDataHandler.handleGraph(
          entity.getEntityAs(OWLClass.class),
          ontologyManager.getOntology(),
          nodeId,
          lastId);
    } else if (ontologyManager.getOntology().containsDataPropertyInSignature(entityIri, INCLUDED)) {
      var entity = entitiesCacheService.getEntityEntry(entityIri, OwlType.DATA_PROPERTY);
      graph = restrictionGraphDataHandler.handleGraph(
          entity.getEntityAs(OWLDataProperty.class),
          ontologyManager.getOntology(),
          nodeId,
          lastId);
    } else if (ontologyManager.getOntology().containsObjectPropertyInSignature(entityIri, INCLUDED)) {
      var entity = entitiesCacheService.getEntityEntry(entityIri, OwlType.OBJECT_PROPERTY);
      graph = restrictionGraphDataHandler.handleGraph(
          entity.getEntityAs(OWLObjectProperty.class),
          ontologyManager.getOntology(),
          nodeId,
          lastId);
    } else if (ontologyManager.getOntology().containsIndividualInSignature(entityIri, INCLUDED)) {
      var entity = entitiesCacheService.getEntityEntry(entityIri, OwlType.INDIVIDUAL);
      graph = restrictionGraphDataHandler.handleGraph(
          entity.getEntityAs(OWLNamedIndividual.class),
          ontologyManager.getOntology(),
          nodeId,
          lastId);
    }

    return new ViewerGraphFactory().convertToVisGraph(graph);
  }
}
