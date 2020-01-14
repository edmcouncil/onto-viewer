package org.edmcouncil.spec.fibo.weasel.ontology;

import java.io.IOException;
import java.util.Arrays;
import javax.annotation.PostConstruct;
import org.edmcouncil.spec.fibo.config.configuration.model.AppConfiguration;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ViewerCoreConfiguration;
import org.edmcouncil.spec.fibo.config.utils.files.FileSystemManager;
import org.edmcouncil.spec.fibo.weasel.ontology.loader.OntologyLoader;
import org.edmcouncil.spec.fibo.weasel.ontology.loader.OntologyLoaderFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class OntologyManager {

  private static final Logger LOG = LoggerFactory.getLogger(OntologyManager.class);

  private OWLOntology ontology;

  @Autowired
  private AppConfiguration appConfiguration;
  @Autowired
  private FileSystemManager fileSystemManager;

  @PostConstruct
  public void init() {
    LOG.info("Configuration loaded ? : {}", appConfiguration != null
        || !appConfiguration.getViewerCoreConfig().isEmpty());
    LOG.info("File system manager created ? : {}", fileSystemManager != null);

    ViewerCoreConfiguration viewerCoreConfiguration = appConfiguration.getViewerCoreConfig();
    OntologyLoader loader = new OntologyLoaderFactory().getInstance(viewerCoreConfiguration, fileSystemManager);
    String location = viewerCoreConfiguration.getOntologyLocation();

    try {
      this.ontology = loader.loadOntology(location);
    } catch (OWLOntologyCreationException ex) {
      LOG.error("[ERROR]: Error when creating ontology. Stoping application. Exception: {} \n Message: {}", ex.getStackTrace(), ex.getMessage());
      System.exit(-1);
    } catch (IOException ex) {
      LOG.error("[ERROR]: Cannot load ontology. Stoping application. Stack Trace: {}", Arrays.toString(ex.getStackTrace()));
      System.exit(-1);
    }
  }

  public OWLOntology getOntology() {
    return ontology;
  }
}
