package org.edmcouncil.spec.ontoviewer.core.ontology.loader;

import java.util.HashMap;
import java.util.Map;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

public class LoadedOntologyData {

  private final OWLOntology ontology;
  private final Map<IRI, IRI> irisToPathsMapping;
  private final Map<String, IRI> pathsToIrisMapping;

  public LoadedOntologyData(OWLOntology ontology, Map<IRI, IRI> irisToPathsMapping) {
    this.ontology = ontology;
    this.irisToPathsMapping = irisToPathsMapping;
    this.pathsToIrisMapping = new HashMap<>();
  }

  public LoadedOntologyData(OWLOntology ontology, Map<IRI, IRI> irisToPathsMapping,
      Map<String, IRI> pathsToIrisMapping) {
    this.ontology = ontology;
    this.irisToPathsMapping = irisToPathsMapping;
    this.pathsToIrisMapping = pathsToIrisMapping;
  }

  public OWLOntology getOntology() {
    return ontology;
  }

  public Map<IRI, IRI> getIrisToPathsMapping() {
    return irisToPathsMapping;
  }

  public Map<String, IRI> getPathsToIrisMapping() {
    return pathsToIrisMapping;
  }
}
