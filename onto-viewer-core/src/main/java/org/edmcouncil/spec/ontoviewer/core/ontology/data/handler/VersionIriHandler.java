package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.module.ModuleHandler;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VersionIriHandler {

  private static final Logger LOG = LoggerFactory.getLogger(VersionIriHandler.class);

  private final ModuleHandler moduleHandler;
  private final OntologyManager ontologyManager;
  private final Map<String, String> ontologyToVersionIriMap = new HashMap<>();

  private boolean initialized = false;

  public VersionIriHandler(ModuleHandler moduleHandler, OntologyManager ontologyManager) {
    this.moduleHandler = moduleHandler;
    this.ontologyManager = ontologyManager;
  }

  public void init(OntologyManager ontologyManager) {
    for (OWLOntology ontology : ontologyManager.getOntologyWithImports()
        .collect(Collectors.toList())) {
      var optionalVersionIri = ontology.getOntologyID().getVersionIRI();
      if (ontology.getOntologyID().getOntologyIRI().isPresent()) {
        if (optionalVersionIri.isPresent()) {
          LOG.debug("Found version iri for ontology {{}}.", ontology.getOntologyID());
          var ontologyIri = ontology.getOntologyID().getOntologyIRI().get().getIRIString();
          ontologyToVersionIriMap.put(ontologyIri, optionalVersionIri.get().getIRIString());
        } else {
          LOG.debug("Not found version iri for ontology {{}}.", ontology.getOntologyID());
        }
      }
    }

    initialized = true;
  }

  public String getVersionIri(IRI iri) {
    if (!initialized) {
      init(ontologyManager);
    }

    String versionIriString = ontologyToVersionIriMap.get(iri.toString());
    if (versionIriString != null && !versionIriString.isEmpty()) {
      return versionIriString;
    }
    var ontologyIri = moduleHandler.getOntologyIri(iri);
    versionIriString = ontologyToVersionIriMap.get(ontologyIri.getIRIString());
    if (versionIriString != null) {
      versionIriString = versionIriString.concat(iri.getFragment());
    }
    return versionIriString;
  }
}
