package org.edmcouncil.spec.fibo.weasel.ontology.loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;

/**
 * This class is used to load ontology from URL
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
class UrlOntologyLoader implements OntologyLoader {

  private static final Logger LOG = LoggerFactory.getLogger(UrlOntologyLoader.class);

  /**
   *
   * This class is used to load ontology from URL
   *
   * @param path OntoUrl is the web address from which the ontology is being loaded.
   * @return set of ontology
   *
   * @throws java.io.IOException
   * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
   */
  @Override
  public OWLOntology loadOntology(String path) throws IOException, OWLOntologyCreationException {

    LOG.debug("URL to Ontology : {} ", path);
    HttpGet httpGet = new HttpGet(path);
    httpGet.addHeader("Accept", "application/rdf+xml, application/xml; q=0.7, text/xml; q=0.6, text/plain; q=0.1, */*; q=0.09");
    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
      HttpResponse response = httpClient.execute(httpGet);
      HttpEntity entity = response.getEntity();
      if (entity != null) {
        InputStream inputStream = entity.getContent();

        //manager.makeLoadImportRequest(importDeclaration);
        OWLOntology newOntology = manager.loadOntologyFromOntologyDocument(inputStream);

        IRI ontoIri = IRI.create("https://spec.edmcouncil.org/fibo/ontology");

        //makeDefaultsOntologiesImport(manager, newOntology);
        OWLImportsDeclaration declaration = new OWLImportsDeclarationImpl(manager.getOntologyDocumentIRI(newOntology));
        manager.makeLoadImportRequest(declaration);
        Stream<OWLOntology> imports = manager.imports(newOntology);

        newOntology = manager.createOntology(ontoIri, imports, false);
        httpClient.close();
        return newOntology;
      }
    }
    return null;
  }

  private void makeDefaultsOntologiesImport(OWLOntologyManager manager, OWLOntology ontology) {
    String[] ontologies = new String[]{"https://www.w3.org/2002/07/owl.rdf",
      "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
      "http://www.w3.org/2000/01/rdf-schema#",
      "http://purl.org/dc/elements/1.1/",
      "https://www.w3.org/2009/08/skos-reference/skos.rdf"};

    for (String oo : ontologies) {
      OWLImportsDeclaration importDeclaration = manager.getOWLDataFactory()
          .getOWLImportsDeclaration(IRI.create(oo));
      manager.applyChange(new AddImport(ontology, importDeclaration));
      manager.makeLoadImportRequest(importDeclaration);
    }
  }

}
