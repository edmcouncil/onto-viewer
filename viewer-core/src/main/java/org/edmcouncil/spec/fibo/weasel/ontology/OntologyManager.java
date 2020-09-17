package org.edmcouncil.spec.fibo.weasel.ontology;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.edmcouncil.spec.fibo.config.configuration.model.AppConfiguration;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ViewerCoreConfiguration;
import org.edmcouncil.spec.fibo.config.utils.files.FileSystemManager;
import org.edmcouncil.spec.fibo.weasel.ontology.loader.AutoOntologyLoader;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

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

  
 /* public void init() {
    LOG.info("Configuration loaded ? : {}", appConfiguration != null
            || !appConfiguration.getViewerCoreConfig().isEmpty());
    LOG.info("File system manager created ? : {}", fileSystemManager != null);

    ViewerCoreConfiguration viewerCoreConfiguration = appConfiguration.getViewerCoreConfig();

    try {
      AutoOntologyLoader loader = new AutoOntologyLoader(fileSystemManager, viewerCoreConfiguration);
      ontology = loader.load();
    } catch (OWLOntologyCreationException ex) {
      LOG.error("[ERROR]: Error when creating ontology. Stoping application. Exception: {} \n Message: {}", ex.getStackTrace(), ex.getMessage());
      System.exit(-1);
    } catch (IOException ex) {
      LOG.error("[ERROR]: Cannot load ontology. Stoping application. Stack Trace: {}", Arrays.toString(ex.getStackTrace()));
      System.exit(-1);
    } catch (ParserConfigurationException ex) {
      LOG.error("[ERROR]: Cannot load ontology, parser exception. Stoping application. Stack Trace: {}", Arrays.toString(ex.getStackTrace()));
    } catch (XPathExpressionException ex) {
      LOG.error("[ERROR]: Cannot load ontology, xpath expression exception. Stoping application. Stack Trace: {}", Arrays.toString(ex.getStackTrace()));
    } catch (SAXException ex) {
      LOG.error("[ERROR]: Cannot load ontology, sax exception. Stoping application. Stack Trace: {}", Arrays.toString(ex.getStackTrace()));
    }
     
  }*/

  public OWLOntology getOntology() {
    return ontology;
  }

  public void updateOntology(OWLOntology ont) {
    this.ontology = ont;
  }
}
