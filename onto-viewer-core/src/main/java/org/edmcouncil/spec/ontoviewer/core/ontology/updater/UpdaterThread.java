package org.edmcouncil.spec.ontoviewer.core.ontology.updater;

import org.edmcouncil.spec.ontoviewer.core.ontology.updater.util.UpdaterOperation;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ConfigurationService;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.CoreConfiguration;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.edmcouncil.spec.ontoviewer.core.model.onto.OntologyResources;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo.FiboDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.AutoOntologyLoader;
import org.edmcouncil.spec.ontoviewer.core.ontology.scope.ScopeIriOntology;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.text.TextDbItem;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.text.TextSearcherDb;
import org.edmcouncil.spec.ontoviewer.core.ontology.stats.OntologyStatsManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.model.InterruptUpdate;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.model.UpdateJob;
import org.edmcouncil.spec.ontoviewer.core.ontology.updater.model.UpdateJobStatus;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnloadableImportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public abstract class UpdaterThread extends Thread implements Thread.UncaughtExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(UpdaterThread.class);
  private static final String interruptMessage = "Interrupts this update. New update request.";

  private ConfigurationService config;
  private OntologyManager ontologyManager;
  private FileSystemManager fileSystemManager;
  private LabelProvider labelProvider;
  private TextSearcherDb textSearcherDb;
  private UpdateBlocker blocker;
  private UpdateJob job;
  private FiboDataHandler fiboDataHandler;
  private ScopeIriOntology scopeIriOntology;
  private OntologyStatsManager ontologyStatsManager;

  public UpdaterThread(ConfigurationService config, OntologyManager ontologyManager, FileSystemManager fileSystemManager, LabelProvider labelProvider, TextSearcherDb textSearcherDb, UpdateBlocker blocker, FiboDataHandler fiboDataHandler, UpdateJob job, ScopeIriOntology scopeIriOntology, OntologyStatsManager osm) {
    this.config = config;
    this.ontologyManager = ontologyManager;
    this.fileSystemManager = fileSystemManager;
    this.labelProvider = labelProvider;
    this.textSearcherDb = textSearcherDb;
    this.blocker = blocker;
    this.job = job;
    this.fiboDataHandler = fiboDataHandler;
    this.scopeIriOntology = scopeIriOntology;
    this.ontologyStatsManager = osm;
    this.setName("UpdateThread-" + job.getId());
  }

  @Override
  public void run() {
    while (true) {
      if (blocker.isUpdateNow()) {
        try {

          if (isInterrupt()) {
            UpdaterOperation.setJobStatusToError(job, interruptMessage);
            this.interrupt();
            return;
          }
          String msg = String.format("UpdateJob with id: %s waiting to end other updates", job.getId());
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
      String msgError = null;

      LOG.info("Configuration loaded ? : {}", config != null
          || !config.getCoreConfiguration().isEmpty());
      LOG.info("File system manager created ? : {}", fileSystemManager != null);

      CoreConfiguration viewerCoreConfiguration = config.getCoreConfiguration();

      if (isInterrupt()) {
        throw new InterruptUpdate();
      }

      //download ontology file/files
      //load ontology to var
      try {
        AutoOntologyLoader loader = new AutoOntologyLoader(fileSystemManager, viewerCoreConfiguration);
        ontology = loader.load();
      } catch (OWLOntologyCreationException ex) {
        msgError = ex.getMessage();
        LOG.error("[ERROR]: Error when creating ontology. Stoping application. Exception: {} \n Message: {}", ex.getStackTrace(), ex.getMessage());
      } catch (IOException ex) {
        msgError = ex.getMessage();
        LOG.error("[ERROR]: Cannot load ontology. Stoping application. Stack Trace: {}", Arrays.toString(ex.getStackTrace()));
      } catch (ParserConfigurationException ex) {
        msgError = ex.getMessage();
        LOG.error("[ERROR]: Cannot load ontology, parser exception. Stoping application. Stack Trace: {}", Arrays.toString(ex.getStackTrace()));
      } catch (XPathExpressionException ex) {
        msgError = ex.getMessage();
        LOG.error("[ERROR]: Cannot load ontology, xpath expression exception. Stoping application. Stack Trace: {}", Arrays.toString(ex.getStackTrace()));
      } catch (SAXException ex) {
        msgError = ex.getMessage();
        LOG.error("[ERROR]: Cannot load ontology, sax exception. Stoping application. Stack Trace: {}", Arrays.toString(ex.getStackTrace()));
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

      Set<String> scopes = scopeIriOntology.getScopeIri(ontology);

      //get default labels
      // TODO: We take default labels only to put them in label provider a few lines down...
//      Map<String, String> defaultLabels = labelProvider.getDefaultLabels();

      //get default text searcher db
      Map<String, TextDbItem> textSearcherDbDefaultData = textSearcherDb.loadDefaultData(ontology);

      if (isInterrupt()) {
        throw new InterruptUpdate();
      }

      //block resourcess when swaping
      blocker.setBlockerStatus(Boolean.TRUE);

      scopeIriOntology.setScopes(scopes);

      ontologyManager.updateOntology(ontology);

      // TODO: See comment on #144
//      labelProvider.clearAndSet(defaultLabels);

      textSearcherDb.clearAndSetDb(textSearcherDbDefaultData);

      //load ontology resource must be here, fibo data handler use label provider
      fiboDataHandler.populateOntologyResources(ontology);

      fiboDataHandler.clearAndSetNewModules(ontology);
      
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
      LOG.error("{}",ex.getStackTrace());
      UpdaterOperation.setJobStatusToError(job, interruptMessage);
      blocker.setUpdateNow(Boolean.FALSE);
      this.interrupt();
      return;
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
