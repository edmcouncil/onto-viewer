package org.edmcouncil.spec.ontoviewer.core.ontology.loader;

import java.util.StringJoiner;

public class OntologyLoadingProblem {

  private final OntologySource ontologySource;
  private final String message;
  private final Class<?> exceptionClass;

  public OntologyLoadingProblem(OntologySource ontologySource,
      String message,
      Class<? extends Exception> exceptionClass) {
    this.ontologySource = ontologySource;
    this.message = message;
    this.exceptionClass = exceptionClass;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", OntologyLoadingProblem.class.getSimpleName() + "[", "]")
        .add("ontologySource=" + ontologySource)
        .add("message='" + message + "'")
        .add("exceptionClass=" + exceptionClass)
        .toString();
  }
}
