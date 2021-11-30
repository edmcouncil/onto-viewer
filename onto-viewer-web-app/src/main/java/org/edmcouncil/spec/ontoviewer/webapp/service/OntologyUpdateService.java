package org.edmcouncil.spec.ontoviewer.webapp.service;

import org.edmcouncil.spec.ontoviewer.webapp.boot.Updater;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.model.UpdateJob;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.util.UpdateJobGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OntologyUpdateService {

  private static final Logger LOG = LoggerFactory.getLogger(OntologyUpdateService.class);

  @Autowired
  private Updater updater;

  public UpdateJob startUpdate() {
    UpdateJob newJob = UpdateJobGenerator.generateNewUpdateJob();
    updater.update(newJob);
    return newJob;
  }

  public UpdateJob getUpdateStatus(String updateId) {
    if (updateId == null) {
      return updater.currentInProgressOrLast();
    }
    return updater.getJobWithId(updateId);
  }
}