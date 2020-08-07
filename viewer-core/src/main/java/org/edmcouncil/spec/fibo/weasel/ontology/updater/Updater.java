package org.edmcouncil.spec.fibo.weasel.ontology.updater;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import org.edmcouncil.spec.fibo.config.configuration.model.AppConfiguration;
import org.edmcouncil.spec.fibo.config.utils.files.FileSystemManager;
import org.edmcouncil.spec.fibo.weasel.ontology.OntologyManager;
import org.edmcouncil.spec.fibo.weasel.ontology.data.label.provider.LabelProvider;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.text.TextSearcherDb;
import org.edmcouncil.spec.fibo.weasel.ontology.updater.model.UpdateHistory;
import org.edmcouncil.spec.fibo.weasel.ontology.updater.model.UpdateJob;
import org.edmcouncil.spec.fibo.weasel.ontology.updater.util.UpdateHistoryManager;
import org.edmcouncil.spec.fibo.weasel.ontology.updater.util.UpdateJobIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Updater {

  private static final Logger LOG = LoggerFactory.getLogger(Updater.class);
  @Autowired
  private AppConfiguration config;
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
  private UpdateHistoryManager historyManager;
  private Map<String, UpdateJob> jobs = new HashMap<>();

  @PostConstruct
  public void init() {
    long now = System.currentTimeMillis() / 1000;
    LOG.debug("init time " + now);
  }

  
  @Scheduled(cron = "0 */5 * * * ?")
  public void autoSave() {
    save();
  }

  public void update(UpdateJob job) {
    jobs.put(job.getId(), job);
    Thread t = new UpdaterThread(config,
            ontologyManager,
            fileSystemManager,
            labelProvider,
            textSearcherDb,
            blocker,
            job) {
    };
    t.start();
  }

  public UpdateJob getJobWithId(String id) {
    return jobs.get(id);
  }

  private void save() {
    UpdateHistory uh = new UpdateHistory();
    uh.setJobs(jobs);
    uh.setLastId(UpdateJobIdGenerator.last());
    try {
      historyManager.saveUpdateHistory(uh);
    } catch (IOException ex) {
       LOG.debug("Cannot save UpdateJob history data. ERROR: " + ex.getMessage());
    }
  }
}
