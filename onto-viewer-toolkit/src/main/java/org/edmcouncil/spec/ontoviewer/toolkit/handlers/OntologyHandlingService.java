package org.edmcouncil.spec.ontoviewer.toolkit.handlers;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.stereotype.Service;

@Service
public class OntologyHandlingService {

  private final OWLOntologyManager owlOntologyManager;

  public OntologyHandlingService() {
    this.owlOntologyManager = OWLManager.createOWLOntologyManager();
  }

  public OWLOntologyManager getOwlOntologyManager() {
    return owlOntologyManager;
  }
}
