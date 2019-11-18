package org.edmcouncil.spec.fibo.weasel.ontology;

import java.io.IOException;
import java.util.Arrays;
import javax.inject.Inject;
import org.edmcouncil.spec.fibo.config.configuration.model.AppConfiguration;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ViewerCoreConfiguration;
import org.edmcouncil.spec.fibo.config.utils.files.FileSystemManager;
import org.edmcouncil.spec.fibo.weasel.ontology.loader.OntologyLoader;
import org.edmcouncil.spec.fibo.weasel.ontology.loader.OntologyLoaderFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class OntologyManager {

  private static final Logger LOG = LoggerFactory.getLogger(OntologyManager.class);

  private OWLOntology ontology;

  @Inject
  public OntologyManager(AppConfiguration config, FileSystemManager fsm) {
    ViewerCoreConfiguration viewerCoreConfiguration = config.getWeaselConfig();
    OntologyLoader lod = new OntologyLoaderFactory().getInstance(viewerCoreConfiguration, fsm);
    String location = viewerCoreConfiguration.getOntologyLocation();

    try {
      this.ontology = lod.loadOntology(location);
    } catch (OWLOntologyCreationException ex) {
      LOG.error("[ERROR]: Error when creating ontology. Stoping application. Exception: {}", ex.getStackTrace(), ex.getMessage());
      System.exit(-1);
    } catch (IOException ex) {
      LOG.error("[ERROR]: Cannot load ontology. Stoping application. Stack Trace: {}", Arrays.toString(ex.getStackTrace()));
      System.exit(-1);
    }
  }
  
  public OWLOntology getOntology() {
    return ontology;
  }

  /*private Set<OWLOntology> getDefaultOntologies(OWLOntologyManager manager) throws IOException, OWLOntologyCreationException {
    String ontoURL = OWL_ONTOLOGY;

    Set<OWLOntology> defaultOntologies = new HashSet();

    LOG.debug("URL to Ontology : {} ", ontoURL);
    HttpGet httpGet = new HttpGet(ontoURL);
    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpResponse response = httpClient.execute(httpGet);
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      InputStream inputStream = entity.getContent();
      OWLOntology newOntology = manager.loadOntologyFromOntologyDocument(inputStream);
      IRI fiboIRI = IRI.create(ontoURL);
      manager.makeLoadImportRequest(new OWLImportsDeclarationImpl(manager.getOntologyDocumentIRI(newOntology)));
      Stream<OWLOntology> directImports = manager.imports(newOntology);
      directImports = directImports.collect(Collectors.toSet()).stream();
      newOntology = manager.createOntology(fiboIRI, directImports, false);
      defaultOntologies.add(newOntology);
      LOG.debug("Loaded Ontology size = {}", newOntology.signature().count());
    }
    LOG.debug("Loaded Default ontology set size = {}", defaultOntologies.size());
    return defaultOntologies;
  }*/
}
