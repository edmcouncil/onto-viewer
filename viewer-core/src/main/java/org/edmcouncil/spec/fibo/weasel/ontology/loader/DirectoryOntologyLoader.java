package org.edmcouncil.spec.fibo.weasel.ontology.loader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
class DirectoryOntologyLoader implements OntologyLoader {

  private static final Logger LOG = LoggerFactory.getLogger(DirectoryOntologyLoader.class);

  private FileSystemManager fsm;

  DirectoryOntologyLoader(FileSystemManager fsm) {
    this.fsm = fsm;
  }

  @Override
  public OWLOntology loadOntology(String path) throws IOException, OWLOntologyCreationException {
    Path dirPath = fsm.getPathToOntologyFile(path);

    LOG.debug("Path to directory is '{}'", dirPath);

    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    OWLOntology onto = manager.createOntology();
    onto = openOntologiesFromDirectory(dirPath.toFile(), manager, onto);
    manager.makeLoadImportRequest(new OWLImportsDeclarationImpl(manager.getOntologyDocumentIRI(onto)));
    try (Stream<OWLOntology> imports = manager.imports(onto)) {
      LOG.debug("create ontology");
      onto = manager.createOntology(IRI.create(""), imports, false);
    }
    return onto;
  }

  /**
   * This method is used to open all Ontologies from directory
   *
   * @param ontologiesDir OntologiesDir is a loaded ontology file.
   * @param manager Manager loading and acessing ontologies.
   * @return set of ontology.
   */
  private OWLOntology openOntologiesFromDirectory(File ontologiesDir, OWLOntologyManager manager, OWLOntology onto) throws OWLOntologyCreationException {

    for (File file : ontologiesDir.listFiles()) {
      LOG.debug("Load ontology file : {}", file.getName());

      if (file.isFile()) {
        if (!getFileExtension(file).equalsIgnoreCase("xml")) {
          /*
          try {
            String about = getAboutFromFile(file);
            if (about != null) {
              HttpGet httpGet = new HttpGet(about);
              httpGet.addHeader("Accept", "application/rdf+xml, application/xml; q=0.7, text/xml; q=0.6, text/plain; q=0.1, *//*; q=0.09");
              try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpResponse response = httpClient.execute(httpGet);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                  InputStream inputStream = entity.getContent();
                  onto = manager.loadOntologyFromOntologyDocument(inputStream);
                  httpClient.close();
                  OWLImportsDeclaration importDeclaration = manager.getOWLDataFactory()
                          .getOWLImportsDeclaration(IRI.create(about));
                  manager.applyChange(new AddImport(onto, importDeclaration));
                  manager.makeLoadImportRequest(importDeclaration);
                  continue;
                }
              }
            } else {
              LOG.debug("about is null");
            }
          } catch (IOException | NullPointerException e) {
            LOG.debug(e.getMessage());

          } */

          manager.loadOntologyFromOntologyDocument(file);
          OWLImportsDeclaration importDeclaration = manager.getOWLDataFactory()
                  .getOWLImportsDeclaration(IRI.create(file));
          manager.applyChange(new AddImport(onto, importDeclaration));
          manager.makeLoadImportRequest(importDeclaration);
        }
      } else if (file.isDirectory()) {
        openOntologiesFromDirectory(file, manager, onto);

      }

    }
    return onto;
  }

  /*
  
  
  @Override
  public OWLOntology loadOntology(String path) throws IOException, OWLOntologyCreationException {
    Path dirPath = fsm.getPathToOntologyFile(path);

    LOG.debug("Path to directory is '{}'", dirPath);

    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    OWLOntology onto = manager.createOntology();
    onto = openOntologiesFromDirectory(dirPath.toFile(), manager, onto);
    manager.makeLoadImportRequest(new OWLImportsDeclarationImpl(manager.getOntologyDocumentIRI(onto)));//onto.getOntologyID().getOntologyIRI().get() != null ? onto.getOntologyID().getOntologyIRI().get() : onto.getOntologyID().getDefaultDocumentIRI().get())
    try (Stream<OWLOntology> imports = manager.imports(onto)) {
      LOG.debug("create ontology");
      onto = manager.createOntology(IRI.create(""), imports, false);
      //for (OWLOntology aImport : imports.collect(Collectors.toSet())) {
        //manager.makeLoadImportRequest(new OWLImportsDeclarationImpl(manager.getOntologyDocumentIRI(aImport)));
      //}
    }
    return onto;
  }
/*
  /**
   * This method is used to open all Ontologies from directory
   *
   * @param ontologiesDir OntologiesDir is a loaded ontology file.
   * @param manager Manager loading and acessing ontologies.
   * @return set of ontology.
   */
 /*
  private OWLOntology openOntologiesFromDirectory(File ontologiesDir, OWLOntologyManager manager, OWLOntology onto) throws OWLOntologyCreationException, IOException {

    for (File file : ontologiesDir.listFiles()) {
      LOG.debug("Load ontology file : {}", file.getName());
     // Boolean loadedFromAbout = false;
      if (file.isFile()) {
        if (!getFileExtension(file).equalsIgnoreCase("xml")) {
//          String about = null;
//          try {
//            about = getAboutFromFile(file);
//            if (about != null) {
//              HttpGet httpGet = new HttpGet(about);
//             // httpGet.addHeader("Accept", "application/rdf+xml, application/xml; q=0.7, text/xml; q=0.6, text/plain; q=0.1, *//*; q=0.09");<-blad tu bedzie po odkomentowaniu
//              try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
//                HttpResponse response = httpClient.execute(httpGet);
//                HttpEntity entity = response.getEntity();
//                if (entity != null) {
//                  InputStream inputStream = entity.getContent();
//                  onto = manager.loadOntologyFromOntologyDocument(inputStream);
//                  httpClient.close();
//                  loadedFromAbout = true;
//                }
//              }
//            } else {
//              LOG.debug("about is null");
//            }
//          } catch (IOException | NullPointerException e) {
//            LOG.debug(e.getMessage());
//            loadedFromAbout = false;
//          }

          OWLImportsDeclaration importDeclaration = null;

//          if (loadedFromAbout) {
//            importDeclaration = manager.getOWLDataFactory()
//                    .getOWLImportsDeclaration(IRI.create(about));
//          } else {
            onto = manager.loadOntologyFromOntologyDocument(file);
            importDeclaration = manager.getOWLDataFactory()
                    .getOWLImportsDeclaration(IRI.create(file));
//          }
          //manager.makeLoadImportRequest(new OWLImportsDeclarationImpl(manager.getOntologyDocumentIRI(onto)));
          //manager.applyChange(new AddImport(onto, importDeclaration));
          manager.makeLoadImportRequest(importDeclaration);
        }
      } else if (file.isDirectory()) {
        openOntologiesFromDirectory(file, manager, onto);

      }

    }
    return onto;
  }

  private OWLOntology tryToLoadOntologyFromAboutAttribute(String about) throws IOException, OWLOntologyCreationException {

    OWLOntology onto;
    UrlOntologyLoader uol = new UrlOntologyLoader();
    onto = uol.loadOntology(about);
    return onto;
  }
   */
  private static String getFileExtension(File file) {
    String fileName = file.getName();
    if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
      return fileName.substring(fileName.lastIndexOf(".") + 1);
    } else {
      return "";
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

  private String getAboutFromFile(File file) throws IOException {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    String about = null;
    try {
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(file);
      doc.getDocumentElement().normalize();

      //owl:Ontology
      NodeList nodes = doc.getElementsByTagName("owl:Ontology");

      for (int i = 0; i < nodes.getLength(); i++) {
        Node node = nodes.item(i);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          Element element = (Element) node;
          about = getAtribute("rdf:about", element);
        }
      }

    } catch (ParserConfigurationException | SAXException ex) {
      LOG.debug(ex.getMessage());
    }
    return about;
  }
}
