package org.edmcouncil.spec.ontoviewer.core.ontology.loader;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.listener.MissingImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

public class LoadedOntologyData {

  private final OWLOntology ontology;
  private final Map<IRI, IRI> irisToPathsMapping;
  private final Map<String, IRI> pathsToIrisMapping;
  private final LoadingDetails loadingDetails;

  public LoadedOntologyData(OWLOntology ontology, Map<IRI, IRI> irisToPathsMapping) {
    this.ontology = ontology;
    this.irisToPathsMapping = irisToPathsMapping;
    this.pathsToIrisMapping = new HashMap<>();
    this.loadingDetails = new LoadingDetails(Collections.emptyList());
  }

  public LoadedOntologyData(OWLOntology ontology, Map<IRI, IRI> irisToPathsMapping,
      Map<String, IRI> pathsToIrisMapping) {
    this.ontology = ontology;
    this.irisToPathsMapping = irisToPathsMapping;
    this.pathsToIrisMapping = pathsToIrisMapping;
    this.loadingDetails = new LoadingDetails(Collections.emptyList());
  }

  public LoadedOntologyData(LoadedOntologyData loadedOntologyData, LoadingDetails loadingDetails) {
    this.ontology = loadedOntologyData.getOntology();
    this.irisToPathsMapping = loadedOntologyData.getIrisToPathsMapping();
    this.pathsToIrisMapping = loadedOntologyData.getPathsToIrisMapping();
    this.loadingDetails = loadingDetails;
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

  public LoadingDetails getLoadingDetails() {
    return loadingDetails;
  }

  public static class LoadingDetails {

    private final List<MissingImport> missingImports;

    public LoadingDetails(List<MissingImport> missingImports) {
      this.missingImports = missingImports;
    }

    public List<MissingImport> getMissingImports() {
      return missingImports;
    }
  }
}
