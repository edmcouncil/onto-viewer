package org.edmcouncil.spec.fibo.weasel.ontology.loader;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
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

    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(inputOntologyFile);
      doc.getDocumentElement().normalize();

      //owl:Ontology
      NodeList nodes = doc.getElementsByTagName("http://www.w3.org/2002/07/owl#Ontology");

      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          Element element = (Element) node;
         LOG.debug("Attriubute {}", getAtribute("http://www.w3.org/1999/02/22-rdf-syntax-ns#about", element));
        }
      }

    } catch (ParserConfigurationException | SAXException ex) {
      LOG.debug("   ");
    }

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

  static String getValue(String tag, Element element) {
    NodeList nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
    Node node = (Node) nodes.item(0);
    return node.getNodeValue();
  }
    static String getAtribute(String attribute, Element element) {
    return element.getAttribute(attribute);
  }
}
