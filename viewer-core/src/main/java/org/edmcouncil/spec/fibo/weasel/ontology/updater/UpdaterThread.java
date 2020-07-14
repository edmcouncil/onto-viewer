package org.edmcouncil.spec.fibo.weasel.ontology.updater;

import org.edmcouncil.spec.fibo.weasel.ontology.updater.util.UpdaterOperation;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.edmcouncil.spec.fibo.config.configuration.model.AppConfiguration;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ViewerCoreConfiguration;
import org.edmcouncil.spec.fibo.config.utils.files.FileSystemManager;
import org.edmcouncil.spec.fibo.weasel.ontology.OntologyManager;
import org.edmcouncil.spec.fibo.weasel.ontology.data.label.provider.LabelProvider;
import org.edmcouncil.spec.fibo.weasel.ontology.loader.OntologyLoader;
import org.edmcouncil.spec.fibo.weasel.ontology.loader.OntologyLoaderFactory;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.text.TextDbItem;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.text.TextSearcherDb;
import org.edmcouncil.spec.fibo.weasel.ontology.updater.model.UpdateJob;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class UpdaterThread extends Thread implements Thread.UncaughtExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(UpdaterThread.class);

  private AppConfiguration config;
  private OntologyManager ontologyManager;
  private FileSystemManager fileSystemManager;
  private LabelProvider labelProvider;
  private TextSearcherDb textSearcherDb;
  private UpdateBlocker blocker;
  private UpdateJob job;

  public UpdaterThread(AppConfiguration config, OntologyManager ontologyManager, FileSystemManager fileSystemManager, LabelProvider labelProvider, TextSearcherDb textSearcherDb, UpdateBlocker blocker, UpdateJob job) {
    this.config = config;
    this.ontologyManager = ontologyManager;
    this.fileSystemManager = fileSystemManager;
    this.labelProvider = labelProvider;
    this.textSearcherDb = textSearcherDb;
    this.blocker = blocker;
    this.job = job;
    this.setName("UpdateThread-" + job.getId());
  }

  @Override
  public void run() {
    while (true) {
      if (blocker.isUpdateNow()) {
        try {
          //Wait for one sec so it doesn't print too fast
          String msg = String.format("UpdateJob with id: %s waiting to end other updates", job.getId());
          UpdaterOperation.setJobStatusToWaiting(job, msg);
          LOG.debug(msg);
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      } else {
        blocker.setUpdateNow(Boolean.TRUE);
        break;
      }

    }
    job = UpdaterOperation.setJobStatusToInProgress(job);
    //download ontology file/files

    //load ontology to var
    OWLOntology ontology = null;
    String msgError = null;

    LOG.info("Configuration loaded ? : {}", config != null
            || !config.getViewerCoreConfig().isEmpty());
    LOG.info("File system manager created ? : {}", fileSystemManager != null);

    ViewerCoreConfiguration viewerCoreConfiguration = config.getViewerCoreConfig();
    OntologyLoader loader = new OntologyLoaderFactory().getInstance(viewerCoreConfiguration, fileSystemManager);
    String location = viewerCoreConfiguration.getOntologyLocation();

    try {
      ontology = loader.loadOntology(location);
    } catch (OWLOntologyCreationException ex) {
      msgError = ex.getMessage();
      LOG.error("[ERROR]: Error when creating ontology. Exception: {} \n Message: {}", ex.getStackTrace(), msgError);
    } catch (IOException ex) {
      msgError = ex.getMessage();
      LOG.error("[ERROR]: Cannot load ontology. Stack Trace: {}", Arrays.toString(ex.getStackTrace()));
    }
    if (msgError != null) {
      LOG.error("[ERROR]: Cannot update, id {}", job.getId());
      job = UpdaterOperation.setJobStatusToError(job, msgError);
      blocker.setUpdateNow(Boolean.FALSE);
      return;
    }

    //get default labels
    Map<String, String> defaultLabels = labelProvider.getDefaultLabels();

    //get default text searcher db
    Map<String, TextDbItem> textSearcherDbDefaultData = textSearcherDb.loadDefaultData(ontology);

    //block resourcess when swaping
    blocker.setBlockerStatus(Boolean.TRUE);

    ontologyManager.updateOntology(ontology);
    labelProvider.clearAndSet(defaultLabels);
    textSearcherDb.clearAndSetDb(textSearcherDbDefaultData);

    blocker.setBlockerStatus(Boolean.FALSE);

    job = UpdaterOperation.setJobStatusToDone(job);
    blocker.setUpdateNow(Boolean.FALSE);
    return;
  }

  @Override
  public void uncaughtException(Thread t, Throwable e) {

    UpdaterOperation.setJobStatusToError(job, e.getMessage());
    LOG.error(e.getStackTrace().toString());
    blocker.setUpdateNow(Boolean.FALSE);

  }

}
