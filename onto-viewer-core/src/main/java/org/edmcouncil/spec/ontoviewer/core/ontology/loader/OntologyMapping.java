package org.edmcouncil.spec.ontoviewer.core.ontology.loader;

import java.nio.file.Path;
import java.util.StringJoiner;
import org.semanticweb.owlapi.model.IRI;

public class OntologyMapping {

  enum MappingSource {
    CONFIGURATION,
    CATALOG_FILE,
  }

  private final IRI iri;
  private final Path path;
  private final String initialPath;
  private final MappingSource mappingSource;

  public OntologyMapping(IRI iri, Path path, String initialPath, MappingSource mappingSource) {
    this.iri = iri;
    this.path = path;
    this.initialPath = initialPath;
    this.mappingSource = mappingSource;
  }

  public IRI getIri() {
    return iri;
  }

  public Path getPath() {
    return path;
  }

  public String getInitialPath() {
    return initialPath;
  }

  public MappingSource getMappingSource() {
    return mappingSource;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", OntologyMapping.class.getSimpleName() + "[", "]")
        .add("iri=" + iri)
        .add("path=" + path)
        .add("initialPath='" + initialPath + "'")
        .add("mappingSource=" + mappingSource)
        .toString();
  }
}
