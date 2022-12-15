package org.edmcouncil.spec.ontoviewer.toolkit.model;

import java.util.StringJoiner;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.LoadedOntologyData.LoadingDetails;

public class ConsistencyCheckResult {

  private final boolean consistent;
  private final LoadingDetails loadingDetails;

  public ConsistencyCheckResult(boolean consistent, LoadingDetails loadingDetails) {
    this.consistent = consistent;
    this.loadingDetails = loadingDetails;
  }

  public boolean isConsistent() {
    return consistent;
  }

  public LoadingDetails getLoadingDetails() {
    return loadingDetails;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ConsistencyCheckResult.class.getSimpleName() + "[", "]")
        .add("consistent=" + consistent)
        .add("loadingDetails=" + loadingDetails)
        .toString();
  }
}
