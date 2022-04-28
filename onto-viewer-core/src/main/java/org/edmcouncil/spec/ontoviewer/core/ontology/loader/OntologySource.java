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
  private final SourceType sourceType;

  public OntologySource(String location, SourceType sourceType) {
    this.location = location;
    this.sourceType = sourceType;
  }

  public String getLocation() {
    return location;
  }

  public SourceType getSourceType() {
    return sourceType;
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

  @Override
  public String toString() {
    return new StringJoiner(", ", OntologySource.class.getSimpleName() + "[", "]")
        .add("location=" + location)
        .add("sourceType=" + sourceType)
        .toString();
  }
}
