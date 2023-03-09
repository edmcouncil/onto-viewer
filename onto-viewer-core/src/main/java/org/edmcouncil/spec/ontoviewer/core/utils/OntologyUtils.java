package org.edmcouncil.spec.ontoviewer.core.utils;

import java.util.Optional;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.springframework.stereotype.Component;

/**
 * Created by Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class OntologyUtils {
    private final OntologyManager ontologyManager;

    public OntologyUtils(OntologyManager ontologyManager) {
        this.ontologyManager = ontologyManager;
    }

    public Optional<OWLOntology> getOntologyByIRI(IRI ontologyIri) {
        OWLOntologyManager manager = ontologyManager.getOntology().getOWLOntologyManager();

        for (OWLOntology currentOntology : manager.ontologies().collect(Collectors.toSet())) {
            var ontologyIriOptional = currentOntology.getOntologyID().getOntologyIRI();
            if (ontologyIriOptional.isPresent()) {
                var currentOntologyIri = ontologyIriOptional.get();
                var createIri = IRI.create(
                        ontologyIri.getIRIString().substring(0, ontologyIri.getIRIString().length() - 1));

                if (currentOntologyIri.equals(ontologyIri)
                        || currentOntologyIri.equals(createIri)) {
                    return Optional.of(currentOntology);
                }
            }
        }

        return Optional.empty();
    }
}
