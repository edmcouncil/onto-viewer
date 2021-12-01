package org.edmcouncil.spec.ontoviewer.toolkit.handlers;

import openllet.owlapi.OpenlletReasonerFactory;
import org.edmcouncil.spec.ontoviewer.core.ontology.DetailsManager;
import org.springframework.stereotype.Service;

@Service
public class OntologyConsistencyChecker {

  private final DetailsManager detailsManager;

  public OntologyConsistencyChecker(DetailsManager detailsManager) {
    this.detailsManager = detailsManager;
  }

  public boolean checkOntologyConsistency() {
    var ontology = detailsManager.getOntology();
    var reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ontology);
    return reasoner.isConsistent();
  }
}