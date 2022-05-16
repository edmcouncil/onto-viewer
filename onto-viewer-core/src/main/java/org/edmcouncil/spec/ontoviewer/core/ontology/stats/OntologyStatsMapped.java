package org.edmcouncil.spec.ontoviewer.core.ontology.stats;

import java.util.Map;

/**
 * @author Michal Daniel (michal.daniel@makolab.com)
 */
public class OntologyStatsMapped {

  private Map<String, Number> stats;
  private Map<String, String> labels;

  public Map<String, Number> getStats() {
    return stats;
  }

  public void setStats(Map<String, Number> stats) {
    this.stats = stats;
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  public void setLabels(Map<String, String> labels) {
    this.labels = labels;
  }
}