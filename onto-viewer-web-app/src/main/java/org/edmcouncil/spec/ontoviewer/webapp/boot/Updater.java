package org.edmcouncil.spec.ontoviewer.webapp.boot;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemService;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.ResourcesPopulate;
import org.edmcouncil.spec.ontoviewer.core.ontology.scope.ScopeIriOntology;
import org.edmcouncil.spec.ontoviewer.core.ontology.stats.OntologyStatsManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.model.UpdateJob;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.model.UpdateJobStatus;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.util.UpdateJobGenerator;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.util.UpdaterOperation;
import org.edmcouncil.spec.ontoviewer.webapp.search.LuceneSearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class Updater {

  private final Map<String, UpdateJob> jobs = new HashMap<>();

  @Autowired
  private OntologyManager ontologyManager;
  @Autowired
  private FileSystemService fileSystemService;
  @Autowired
  private UpdateBlocker blocker;
  @Autowired
  private ScopeIriOntology scopeIriOntology;
  @Autowired
  private OntologyStatsManager ontologyStatsManager;
  @Autowired
  private LuceneSearcher luceneSearcher;
  @Autowired
  private ApplicationConfigurationService applicationConfigurationService;
  @Autowired
  private ResourcesPopulate resourcesPopulate;
  private static final String INTERRUPT_MESSAGE = "Interrupts this update. New update request.";

  @EventListener(ApplicationReadyEvent.class)
  public void afterStart() {
    UpdateJob job = UpdateJobGenerator.generateNewUpdateJob();
    this.update(job);
  }

  public void update(UpdateJob job) {
    jobs.put(job.getId(), job);

    interruptOtherWaitingJobs();
    interruptWorkingJob();

    UpdaterThread t = new UpdaterThread(
        ontologyManager,
        fileSystemService,
        blocker,
        job,
        scopeIriOntology,
        ontologyStatsManager,
        luceneSearcher,
        applicationConfigurationService, resourcesPopulate) {
    };
    t.start();

  }

  public UpdateJob getJobWithId(String id) {
    return jobs.get(id);
  }

  public UpdateJob currentInProgressOrLast() {
    for (UpdateJob value : jobs.values()) {
      if (value.getStatus() == UpdateJobStatus.IN_PROGRESS) {
        return value;
      }
    }
    return jobs.values()
        .stream()
        .max(Comparator.comparing(UpdateJob::getId))
        .orElse(null);
  }

  private void interruptOtherWaitingJobs() {
    jobs.values().stream()
        .filter((value) -> !(value.getId().equals(String.valueOf(0))))
        .filter((value) -> (value.getStatus() == UpdateJobStatus.WAITING))
        .forEachOrdered((value) -> UpdaterOperation.setJobStatusToError(value, INTERRUPT_MESSAGE));
  }

  private void interruptWorkingJob() {
    jobs.values().stream()
        .filter((value) -> !(value.getId().equals(String.valueOf(0))))
        .filter((value) -> (value.getStatus() == UpdateJobStatus.IN_PROGRESS))
        .forEachOrdered((value) -> UpdaterOperation.setJobStatusToInterrupt(value, INTERRUPT_MESSAGE));
  }
}