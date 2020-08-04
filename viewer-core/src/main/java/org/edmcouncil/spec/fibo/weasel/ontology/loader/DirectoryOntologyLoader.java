package org.edmcouncil.spec.fibo.weasel.ontology.loader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.edmcouncil.spec.fibo.config.utils.files.FileSystemManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;

/**
 * @author MichaĹ‚ Daniel (michal.daniel@makolab.com)
 */
class DirectoryOntologyLoader implements OntologyLoader {

  private static final Logger LOG = LoggerFactory.getLogger(DirectoryOntologyLoader.class);

  private FileSystemManager fsm;

  DirectoryOntologyLoader(FileSystemManager fsm) {
    this.fsm = fsm;
  }
  static List<String> supportedExtensions = Arrays.asList(".rdf", ".owl");

  @Override
  public OWLOntology loadOntology(String path) throws IOException, OWLOntologyCreationException {
    Path dirPath = fsm.getPathToOntologyFile(path);

    LOG.debug("Path to directory is '{}'", dirPath);

    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    AutoIRIMapper autoIRIMapper = new AutoIRIMapper(new File(dirPath.toAbsolutePath().toString()), true);
    autoIRIMapper.setFileExtensions(supportedExtensions);
    autoIRIMapper.update();
    LOG.error("auto iri maper " + autoIRIMapper.getDocumentIRI(IRI.create("http://www.w3.org/2004/02/skos/core")));
    LOG.info("mapped ontologies " + autoIRIMapper.getOntologyIRIs().stream().count());
    OWLOntology onto = manager.createOntology();

    manager.getIRIMappers().add(autoIRIMapper);
    //onto = manager.loadOntology(IRI.create("https://spec.edmcouncil.org/fibo/ontology/AboutFIBODev/"));

    //onto = openOntologiesFromDirectory(dirPath.toFile(), manager, onto);
    onto = loadOntologiesFromIRIs(autoIRIMapper.getOntologyIRIs(), onto, manager);
    //Set<OWLOntology> ontologies = openOntologiesFromDirectorySet(dirPath.toFile(), manager, onto);

    //onto = manager.createOntology(IRI.create(""), ontologies, false);
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

    Collator crl = Collator.getInstance();

    List<File> files = Arrays.asList(ontologiesDir.listFiles())
            .stream()
            .sorted((File o1, File o2) -> crl.compare(o1.getName(), o2.getName()))
            .collect(Collectors.toList());

    AutoIRIMapper mapper = null;
    for (OWLOntologyIRIMapper iriMapper : manager.getIRIMappers()) {
      mapper = (AutoIRIMapper) iriMapper;
    }

    for (File file : files) {
      LOG.debug("Load ontology file : {}", file.getName());

      if (file.isFile()) {
        if (supportedExtensions.contains(getFileExtension(file).toLowerCase())) {
          OWLOntology o = manager.loadOntologyFromOntologyDocument(IRI.create(file));
          OWLImportsDeclaration importDeclaration = manager.getOWLDataFactory()
                  .getOWLImportsDeclaration(mapper.getDocumentIRI(IRI.create(file)));
          manager.applyChange(new AddImport(onto, importDeclaration));
          manager.makeLoadImportRequest(importDeclaration);
        }
      } else if (file.isDirectory()) {
//        AutoIRIMapper autoIRIMapper = new AutoIRIMapper(file, true);
//        autoIRIMapper.setFileExtensions(supportedExtensions);
//        manager.getIRIMappers().add(autoIRIMapper);
        openOntologiesFromDirectory(file, manager, onto);

      }

    }
    return onto;
  }

  private Set<OWLOntology> openOntologiesFromDirectorySet(File ontologiesDir, OWLOntologyManager manager, OWLOntology onto) throws OWLOntologyCreationException {
    Set<OWLOntology> result = new HashSet<>();
    Collator crl = Collator.getInstance();

    List<File> files = Arrays.asList(ontologiesDir.listFiles())
            .stream()
            .sorted((File o1, File o2) -> crl.compare(o1.getName(), o2.getName()))
            .collect(Collectors.toList());

    for (File file : files) {
      LOG.debug("Load ontology file : {}", file.getName());

      if (file.isFile()) {
        if (supportedExtensions.contains(getFileExtension(file).toLowerCase())) {
          OWLOntology ontology = manager.loadOntology(IRI.create(file));
          result.add(ontology);
//          OWLImportsDeclaration importDeclaration = manager.getOWLDataFactory()
//                  .getOWLImportsDeclaration(IRI.create(file));
//          manager.applyChange(new AddImport(onto, importDeclaration));
          //manager.makeLoadImportRequest(importDeclaration);
        }
      } else if (file.isDirectory()) {
        result.addAll(openOntologiesFromDirectorySet(file, manager, onto));

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

  private OWLOntology loadOntologiesFromIRIs(Set<IRI> iris, OWLOntology onto, OWLOntologyManager manager) throws OWLOntologyCreationException {

    for (IRI iri : iris) {
      OWLOntology o = manager.loadOntology(iri);
      OWLImportsDeclaration importDeclaration = manager.getOWLDataFactory()
              .getOWLImportsDeclaration(iri);
      manager.applyChange(new AddImport(onto, importDeclaration));
      manager.makeLoadImportRequest(importDeclaration);
    }

    return onto;
  }

}
