package org.edmcouncil.spec.ontoviewer.core.ontology.updater.model;

import java.util.HashMap;
import java.util.Map;

public class UpdateHistory {

  private long lastId = 0;
  private Map<String, UpdateJob> jobs = new HashMap<>();

  public long getLastId() {
    return lastId;
  }

  public void setLastId(long lastId) {
    this.lastId = lastId;
  }

  public Map<String, UpdateJob> getJobs() {
    return jobs;
  }

  public void setJobs(Map<String, UpdateJob> jobs) {
    this.jobs = jobs;
  }

}
