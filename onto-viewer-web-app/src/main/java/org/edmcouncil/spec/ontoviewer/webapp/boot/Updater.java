package org.edmcouncil.spec.ontoviewer.webapp.boot;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ConfigurationService;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.DataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.scope.ScopeIriOntology;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.text.TextSearcherDb;
import org.edmcouncil.spec.ontoviewer.core.ontology.stats.OntologyStatsManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.model.UpdateJob;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.model.UpdateJobStatus;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.util.UpdateJobGenerator;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.util.UpdaterOperation;
import org.edmcouncil.spec.ontoviewer.webapp.search.LuceneSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class Updater {

    private static final Logger LOG = LoggerFactory.getLogger(Updater.class);

    private final Map<String, UpdateJob> jobs = new HashMap<>();

    @Autowired
    private ConfigurationService config;
    @Autowired
    private OntologyManager ontologyManager;
    @Autowired
    private FileSystemManager fileSystemManager;
    @Autowired
    private LabelProvider labelProvider;
    @Autowired
    private TextSearcherDb textSearcherDb;
    @Autowired
    private UpdateBlocker blocker;
    @Autowired
    private DataHandler fiboDataHandler;
    @Autowired
    private ScopeIriOntology scopeIriOntology;
    @Autowired
    private OntologyStatsManager ontologyStatsManager;
    @Autowired
    private LuceneSearcher luceneSearcher;

    private static final String interruptMessage = "Interrupts this update. New update request.";

    @EventListener(ApplicationReadyEvent.class)
    public void afterStart() {
        UpdateJob job = UpdateJobGenerator.generateNewUpdateJob();
        this.update(job);
    }

    public void update(UpdateJob job) {
        jobs.put(job.getId(), job);

        interruptOtherWaitingJobs();
        interruptWorkingJob();

        UpdaterThread t = new UpdaterThread(config,
                ontologyManager,
                fileSystemManager,
                labelProvider,
                textSearcherDb,
                blocker,
                fiboDataHandler,
                job,
                scopeIriOntology,
                ontologyStatsManager,
                luceneSearcher) {
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
                .forEachOrdered((value) -> {
                    UpdaterOperation.setJobStatusToError(value, interruptMessage);
                });
    }

    private void interruptWorkingJob() {
        jobs.values().stream()
                .filter((value) -> !(value.getId().equals(String.valueOf(0))))
                .filter((value) -> (value.getStatus() == UpdateJobStatus.IN_PROGRESS))
                .forEachOrdered((value) -> {
                    UpdaterOperation.setJobStatusToInterrupt(value, interruptMessage);
                });
    }

}
