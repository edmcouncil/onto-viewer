package org.edmcouncil.spec.fibo.weasel.ontology;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.edmcouncil.spec.fibo.config.configuration.model.AppConfiguration;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.WeaselConfiguration;
import org.edmcouncil.spec.fibo.config.utils.files.FileSystemManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class OntologyManager {

  private static final Logger LOG = LoggerFactory.getLogger(OntologyManager.class);

  private OWLOntology ontology;

  @Autowired
  private AppConfiguration config;

  @PostConstruct
  public void init() throws IOException {
    WeaselConfiguration weaselConfiguration = (WeaselConfiguration) config.getWeaselConfig();
    try {
      if (weaselConfiguration.isOntologyLocationSet()) {
        if (weaselConfiguration.isOntologyLocationURL()) {
          String ontoURL = weaselConfiguration.getURLOntology();
          loadOntologyFromURL(ontoURL);
        } else {
          String ontoPath = weaselConfiguration.getPathOntology();
          loadOntologyFromFile(ontoPath);
        }
      } else {
        loadOntologyFromFile(null);
      }
    } catch (OWLOntologyCreationException ex) {
      LOG.error("[ERROR]: Error when creating ontology. Exception: {}", ex.getStackTrace(), ex.getMessage());
    }
  }

  @PreDestroy
  public void destroy() {

  }

  /**
   * This method is used to load ontology from file
   *
   * @param ontoPath OntoPath is the access path from which the ontology is being loaded.
   */
  private void loadOntologyFromFile(String ontoPath) throws IOException, OWLOntologyCreationException {
    FileSystemManager fsm = new FileSystemManager();
    Path pathToOnto = null;
    if (ontoPath == null) {
      pathToOnto = fsm.getDefaultPathToOntologyFile();
    } else {
      pathToOnto = fsm.getPathToOntologyFile(ontoPath);

    }
    LOG.debug("Path to ontology : {}", pathToOnto.toString());
    File inputOntologyFile = pathToOnto.toFile();

    OWLOntologyManager m = OWLManager.createOWLOntologyManager();

    OWLOntology o = m.loadOntologyFromOntologyDocument(inputOntologyFile);

    IRI fiboIRI = IRI.create("https://spec.edmcouncil.org/fibo/ontologyAboutFIBOProd/");

    m.makeLoadImportRequest(new OWLImportsDeclarationImpl(m.getOntologyDocumentIRI(o)));
    Stream<OWLOntology> directImports = m.imports(o);
    o = m.createOntology(fiboIRI, directImports, false);
    ontology = o;

  }

  /**
   * This method is used to load ontology from URL
   *
   * @param ontoURL OntoUrl is the web address from which the ontology is being loaded.
   * @return set of ontology
   */
  private Set<OWLOntology> loadOntologyFromURL(String ontoURL) throws IOException, OWLOntologyCreationException {

    LOG.debug("URL to Ontology : {} ", ontoURL);
    HttpGet httpGet = new HttpGet(ontoURL);
    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpResponse response = httpClient.execute(httpGet);
    Set<OWLOntology> result = new HashSet<>();
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      long len = entity.getContentLength();
      InputStream inputStream = entity.getContent();
      OWLOntology newOntology = manager.loadOntologyFromOntologyDocument(inputStream);
      IRI fiboIRI = IRI.create(ontoURL);
      manager.makeLoadImportRequest(new OWLImportsDeclarationImpl(manager.getOntologyDocumentIRI(newOntology)));
      Stream<OWLOntology> directImports = manager.imports(newOntology);
      newOntology = manager.createOntology(fiboIRI, directImports, false);
      ontology = newOntology;
    }
    return result;
  }

  /**
   * This method is used to open all Ontologies from directory
   *
   * @param ontologiesDir OntologiesDir is a loaded ontology file.
   * @param manager Manager loading and acessing ontologies.
   * @return set of ontology.
   */
  private Set<OWLOntology> openOntologiesFromDirectory(File ontologiesDir, OWLOntologyManager manager) throws OWLOntologyCreationException {
    Set<OWLOntology> result = new HashSet<>();
    for (File file : ontologiesDir.listFiles()) {
      LOG.debug("isFile : {}, name: {}", file.isFile(), file.getName());
      if (file.isFile()) {
        if (getFileExtension(file).equalsIgnoreCase("rdf") && !file.getName().contains("Metadata")) {

          OWLOntology newOntology = manager.loadOntologyFromOntologyDocument(file);
          result.add(newOntology);
        }
      } else if (file.isDirectory()) {
        Set<OWLOntology> tmp = openOntologiesFromDirectory(file, manager);
        result.addAll(tmp);
      }

    }
    return result;
  }

  private static String getFileExtension(File file) {
    String fileName = file.getName();
    if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
      return fileName.substring(fileName.lastIndexOf(".") + 1);
    } else {
      return "";
    }
  }

  public OWLOntology getOntology() {
    return ontology;
  }

}
