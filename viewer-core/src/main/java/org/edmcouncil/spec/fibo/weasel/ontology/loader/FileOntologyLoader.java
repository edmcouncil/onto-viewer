package org.edmcouncil.spec.fibo.weasel.ontology.loader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.edmcouncil.spec.fibo.config.utils.files.FileSystemManager;
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
 * This class is used to load ontology file from computer.
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
class FileOntologyLoader implements OntologyLoader {

  private static final Logger LOG = LoggerFactory.getLogger(FileOntologyLoader.class);

  private final FileSystemManager fsm;

  public FileOntologyLoader(FileSystemManager fsm) {
    this.fsm = fsm;
  }

  /**
   * This method is used to load ontology from file
   *
   * @param path OntoPath is the access path from which the ontology is being loaded.
   * @return set of ontology
   *
   * @throws java.io.IOException
   * @throws org.semanticweb.owlapi.model.OWLOntologyCreationException
   */
  @Override
  public OWLOntology loadOntology(String path) throws IOException, OWLOntologyCreationException {
    Path pathToOnto = null;
    if (path == null) {
      pathToOnto = fsm.getDefaultPathToOntologyFile();
    } else {
      pathToOnto = fsm.getPathToOntologyFile(path);
    }

    LOG.debug("Path to ontology : {}", pathToOnto.toString());
    File inputOntologyFile = pathToOnto.toFile();

    OWLOntologyManager m = OWLManager.createOWLOntologyManager();

    LOG.debug("Load ontology from document");
    OWLOntology o = m.loadOntologyFromOntologyDocument(inputOntologyFile);

    IRI fiboIRI = IRI.create("https://spec.edmcouncil.org/fibo/ontology");
    LOG.debug("Load import request");

    m.makeLoadImportRequest(new OWLImportsDeclarationImpl(m.getOntologyDocumentIRI(o)));

    LOG.debug("Make imports");
    try (Stream<OWLOntology> imports = m.imports(o)) {
      LOG.debug("create ontology");
      o = m.createOntology(fiboIRI, imports, false);
    }
    return o;

  }

  private File getFileFromResources(String fileName) {

    ClassLoader classLoader = getClass().getClassLoader();

    URL resource = classLoader.getResource(fileName);
    if (resource == null) {
      throw new IllegalArgumentException("file is not found!");
    } else {
      return new File(resource.getFile());
    }

  }

  @Deprecated
  private void loadDefaultsOntologies(OWLOntologyManager m, OWLOntology o) {
    File f = getFileFromResources("ontologies");
    for (File ff : f.listFiles()) {
      LOG.debug("Load ontology file : {}", ff.getName());
      OWLImportsDeclaration importDeclaration = m.getOWLDataFactory()
          .getOWLImportsDeclaration(IRI.create(ff));
      m.applyChange(new AddImport(o, importDeclaration));
      m.makeLoadImportRequest(importDeclaration);
    }
  }

}
