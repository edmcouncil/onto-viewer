package org.edmcouncil.spec.ontoviewer.core.ontology.updater.util;

import org.edmcouncil.spec.ontoviewer.core.ontology.updater.model.UpdateJob;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.model.UpdateJobStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdaterOperation {

  private static Logger LOG = LoggerFactory.getLogger(UpdaterOperation.class);

   public static UpdateJob setJobStatusToInProgress(UpdateJob job) {
    job.setStatus(UpdateJobStatus.IN_PROGRESS);
    job.setMsg("");
    LOG.debug(job.toString());
    return job;
  }

  public static UpdateJob setJobStatusToError(UpdateJob job, String msgError) {
    job.setStatus(UpdateJobStatus.ERROR);
    job.setMsg(msgError);
    LOG.debug(job.toString());
    return job;
  }

  public static UpdateJob setJobStatusToDone(UpdateJob job) {
    job.setStatus(UpdateJobStatus.DONE);
    job.setMsg("");
    LOG.debug(job.toString());
    return job;
  }

  public static UpdateJob setJobStatusToWaiting(UpdateJob job, String msg) {
    job.setStatus(UpdateJobStatus.WAITING);
    job.setMsg(msg);
    LOG.debug(job.toString());
    return job;
  }

  public static UpdateJob setJobStatusToInterrupt(UpdateJob job, String msg) {
    job.setStatus(UpdateJobStatus.INTERRUPT_IN_PROGRESS);
    job.setMsg(msg);
    LOG.debug(job.toString());
    return job;
  }

}
