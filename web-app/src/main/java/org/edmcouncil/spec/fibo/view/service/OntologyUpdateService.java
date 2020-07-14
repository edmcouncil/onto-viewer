package org.edmcouncil.spec.fibo.view.service;

import org.edmcouncil.spec.fibo.weasel.ontology.updater.util.UpdateJobGenerator;
import org.edmcouncil.spec.fibo.weasel.ontology.updater.Updater;
import org.edmcouncil.spec.fibo.weasel.ontology.updater.model.UpdateJob;
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
    return updater.getJobWithId(updateId);
  }

}
