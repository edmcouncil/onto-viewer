package org.edmcouncil.spec.ontoviewer.core.ontology.loader;

import java.io.File;
import java.util.Objects;
import java.util.StringJoiner;
import org.semanticweb.owlapi.model.IRI;

public class OntologySource {

  enum SourceType {
    FILE,
    URL,
  }

  private final String location;
  private final String originalLocation;
  private final SourceType sourceType;
  private IRI ontologyIri;

  public OntologySource(String location, SourceType sourceType) {
    this.location = location;
    this.originalLocation = location;
    this.sourceType = sourceType;
  }

  public OntologySource(String location, String originalLocation, SourceType sourceType) {
    this.location = location;
    this.originalLocation = originalLocation;
    this.sourceType = sourceType;
  }

  public String getLocation() {
    return location;
  }

  public String getOriginalLocation() {
    return originalLocation;
  }

  public SourceType getSourceType() {
    return sourceType;
  }

  public IRI getOntologyIri() {
    return ontologyIri;
  }

  public void setOntologyIri(IRI ontologyIri) {
    this.ontologyIri = ontologyIri;
  }

  public IRI getAsIri() {
    if (sourceType == SourceType.URL) {
      return IRI.create(location);
    } else {
      return IRI.create(new File(location));
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OntologySource)) {
      return false;
    }
    OntologySource that = (OntologySource) o;
    return location.equals(that.location) && sourceType == that.sourceType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(location, sourceType);
  }


}
