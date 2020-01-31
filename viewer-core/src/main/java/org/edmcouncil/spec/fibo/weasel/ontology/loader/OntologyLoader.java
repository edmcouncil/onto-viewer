package org.edmcouncil.spec.fibo.weasel.ontology.loader;

import java.io.IOException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Simply interface for all ontology loaders
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public interface OntologyLoader {
  OWLOntology loadOntology(String path) throws IOException, OWLOntologyCreationException;
}
