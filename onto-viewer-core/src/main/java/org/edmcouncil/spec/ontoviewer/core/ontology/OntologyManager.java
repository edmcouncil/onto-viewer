package org.edmcouncil.spec.ontoviewer.core.ontology;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class OntologyManager {

  private OWLOntology ontology;

  public OWLOntology getOntology() {
    return ontology;
  }

  public void updateOntology(OWLOntology ont) {
    this.ontology = ont;
  }

  public Stream<OWLOntology> getOntologyWithImports() {
    var ontologies = ontology.imports().collect(Collectors.toSet());
    ontologies.add(ontology);
    return ontologies.stream();
  }
}
