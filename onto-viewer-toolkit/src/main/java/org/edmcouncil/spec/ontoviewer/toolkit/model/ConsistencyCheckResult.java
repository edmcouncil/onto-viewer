package org.edmcouncil.spec.ontoviewer.toolkit.model;

import java.util.StringJoiner;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.LoadedOntologyData.LoadingDetails;

public class ConsistencyCheckResult {

  private final boolean consistent;
  private final String inconsistencyExplanation;

  public ConsistencyCheckResult(boolean consistent, String inconsistencyExplanation) {
    this.consistent = consistent;
    this.inconsistencyExplanation = inconsistencyExplanation;
  }

  public boolean isConsistent() {
    return consistent;
  }

  public String explainInconsistency() { return inconsistencyExplanation; }

  @Override
  public String toString() {
    return new StringJoiner(", ", ConsistencyCheckResult.class.getSimpleName() + "[", "]")
        .add("consistent=" + consistent)
        .add("inconsistency_explanation=" + inconsistencyExplanation)
        .toString();
  }
}
