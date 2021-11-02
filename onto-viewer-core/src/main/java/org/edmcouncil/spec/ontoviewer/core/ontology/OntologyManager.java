package org.edmcouncil.spec.ontoviewer.core.ontology;

import java.util.Set;
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
  private Set<OWLOntology> ontologies;

  public OWLOntology getOntology() {
    return ontology;
  }

  public void updateOntology(OWLOntology ont) {
    this.ontology = ont;
  }

  public Stream<OWLOntology> getOntologyWithImports() {
    if (this.ontologies == null) {
      this.ontologies = ontology.imports().collect(Collectors.toSet());
      this.ontologies.add(ontology);
    }
    return this.ontologies.stream();
  }
}