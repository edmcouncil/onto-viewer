package org.edmcouncil.spec.ontoviewer.webapp.boot;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo.FiboDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.AutoOntologyLoader;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.listener.MissingImport;
import org.edmcouncil.spec.ontoviewer.core.ontology.scope.ScopeIriOntology;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.text.TextDbItem;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.text.TextSearcherDb;
import org.edmcouncil.spec.ontoviewer.core.ontology.stats.OntologyStatsManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.model.InterruptUpdate;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.model.UpdateJob;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.model.UpdateJobStatus;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.util.UpdaterOperation;
import org.edmcouncil.spec.ontoviewer.webapp.search.LuceneSearcher;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class UpdaterThread extends Thread implements Thread.UncaughtExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(UpdaterThread.class);
  private static final String INTERRUPT_MESSAGE = "Interrupts this update. New update request.";

  private OntologyManager ontologyManager;
  private FileSystemManager fileSystemManager;
  private TextSearcherDb textSearcherDb;
  private UpdateBlocker blocker;
  private UpdateJob job;
  private FiboDataHandler fiboDataHandler;
  private ScopeIriOntology scopeIriOntology;
  private OntologyStatsManager ontologyStatsManager;
  private LuceneSearcher luceneSearcher;
  private ApplicationConfigurationService applicationConfigurationService;

  public UpdaterThread(OntologyManager ontologyManager,
      FileSystemManager fileSystemManager,
      TextSearcherDb textSearcherDb,
      UpdateBlocker blocker,
      FiboDataHandler fiboDataHandler,
      UpdateJob job,
      ScopeIriOntology scopeIriOntology,
      OntologyStatsManager osm,
      LuceneSearcher luceneSearcher,
      ApplicationConfigurationService applicationConfigurationService) {
    this.ontologyManager = ontologyManager;
    this.fileSystemManager = fileSystemManager;
    this.textSearcherDb = textSearcherDb;
    this.blocker = blocker;
    this.job = job;
    this.fiboDataHandler = fiboDataHandler;
    this.scopeIriOntology = scopeIriOntology;
    this.ontologyStatsManager = osm;
    this.luceneSearcher = luceneSearcher;
    this.applicationConfigurationService = applicationConfigurationService;
    this.setName("UpdateThread-" + job.getId());
  }

  @Override
  public void run() {
    while (true) {
      if (blocker.isUpdateNow()) {
        try {

          if (isInterrupt()) {
            UpdaterOperation.setJobStatusToError(job, INTERRUPT_MESSAGE);
            this.interrupt();
            return;
          }
          String msg = String.format("UpdateJob with id: %s waiting to end other updates",
              job.getId());
          UpdaterOperation.setJobStatusToWaiting(job, msg);
          LOG.debug(msg);
          //Wait for one sec so it doesn't print too fast
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
          UpdaterOperation.setJobStatusToError(job, e.getMessage());
          return;
        }
      } else {
        blocker.setUpdateNow(Boolean.TRUE);
        break;
      }
    }

    try {
      if (isInterrupt()) {
        throw new InterruptUpdate();
      }

      job = UpdaterOperation.setJobStatusToInProgress(job);

      OWLOntology ontology = null;
      Map<IRI, IRI> iriToPathMapping = new HashMap<>();
      String msgError = null;

      LOG.info("File system manager created ? : {}", fileSystemManager != null);

      ConfigurationData configurationData = applicationConfigurationService.getConfigurationData();

      if (isInterrupt()) {
        throw new InterruptUpdate();
      }

      //download ontology file/files
      //load ontology to var
      AutoOntologyLoader loader = new AutoOntologyLoader(configurationData, fileSystemManager);
      try {
        var loadedOntologyData = loader.load();
        ontology = loadedOntologyData.getOntology();
        iriToPathMapping = loadedOntologyData.getIriToPathMapping();
      } catch (OWLOntologyCreationException ex) {
        msgError = ex.getMessage();
        LOG.error(
            "[ERROR]: Error when creating ontology. Stopping application. Exception: {} \n Message: {}",
            ex.getStackTrace(), ex.getMessage());
      }

      if (msgError != null) {
        LOG.error("[ERROR]: Cannot update, id {}", job.getId());
        job = UpdaterOperation.setJobStatusToError(job, msgError);
        blocker.setUpdateNow(Boolean.FALSE);
        return;
      }
      if (isInterrupt()) {
        throw new InterruptUpdate();
      }

      Set<MissingImport> missingImports = loader.getMissingImportListenerImpl().getNotImportUri();

      Set<String> scopes = scopeIriOntology.getScopeIri(ontology);

      if (isInterrupt()) {
        throw new InterruptUpdate();
      }

      //block resourcess when swaping
      blocker.setBlockerStatus(Boolean.TRUE);

      scopeIriOntology.setScopes(scopes);

      ontologyManager.updateOntology(ontology);

      ontologyManager.setMissingImports(missingImports);
      ontologyManager.setIriToPathMapping(iriToPathMapping);

      //get default text searcher db
      Map<String, TextDbItem> textSearcherDbDefaultData = textSearcherDb.loadDefaultData(ontology);
      textSearcherDb.clearAndSetDb(textSearcherDbDefaultData);

      luceneSearcher.populateIndex();

      //load ontology resource must be here, fibo data handler use label provider
      fiboDataHandler.populateOntologyResources(ontology);

      fiboDataHandler.getFiboOntologyHandler().setModulesTree(fiboDataHandler.getAllModules()); // TODO: Seems not right
      ontologyStatsManager.clear();
      ontologyStatsManager.generateStats(ontology);

      blocker.setBlockerStatus(Boolean.FALSE);

      job = UpdaterOperation.setJobStatusToDone(job);

      blocker.setUpdateNow(Boolean.FALSE);

      if (job.getId().equals(String.valueOf(0))) {
        blocker.setInitializeAppDone(Boolean.TRUE);
      }

      LOG.info("Application has started successfully.");
    } catch (InterruptUpdate ex) {
      LOG.error("{}", ex.getStackTrace());
      UpdaterOperation.setJobStatusToError(job, INTERRUPT_MESSAGE);
      blocker.setUpdateNow(Boolean.FALSE);
      this.interrupt();
    } catch (NullPointerException | UnloadableImportException ex) {
      ex.printStackTrace();
      UpdaterOperation.setJobStatusToError(job, ex.getMessage());
      blocker.setUpdateNow(Boolean.FALSE);
      this.interrupt();
    }
  }

  @Override
  public void uncaughtException(Thread t, Throwable e) {
    UpdaterOperation.setJobStatusToError(job, e.getMessage());
    LOG.error(e.getStackTrace().toString());
    blocker.setUpdateNow(Boolean.FALSE);
  }

  public UpdateJob getJob() {
    return job;
  }

  private Boolean isInterrupt() {
    return job.getStatus() == UpdateJobStatus.ERROR
        || job.getStatus() == UpdateJobStatus.INTERRUPT_IN_PROGRESS;
  }
}
