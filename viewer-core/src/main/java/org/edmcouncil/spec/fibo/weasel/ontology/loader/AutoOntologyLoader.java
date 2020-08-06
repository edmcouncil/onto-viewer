package org.edmcouncil.spec.fibo.weasel.ontology.loader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.edmcouncil.spec.fibo.config.configuration.model.ConfigKeys;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ViewerCoreConfiguration;
import org.edmcouncil.spec.fibo.config.utils.files.FileSystemManager;
import org.edmcouncil.spec.fibo.weasel.ontology.loader.mapper.VersionIriMapper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;

public class AutoOntologyLoader {

  private static final Logger LOG = LoggerFactory.getLogger(AutoOntologyLoader.class);

  private FileSystemManager fsm;
  private ViewerCoreConfiguration config;
  static List<String> supportedExtensions = Arrays.asList(".rdf", ".owl");

  public AutoOntologyLoader(FileSystemManager fsm, ViewerCoreConfiguration viewerCoreConfiguration) {
    this.fsm = fsm;
    this.config = viewerCoreConfiguration;
  }

  public OWLOntology load() throws OWLOntologyCreationException, IOException, ParserConfigurationException, ParserConfigurationException, XPathExpressionException, XPathExpressionException, SAXException {
    Set<IRI> irisToLoad = new HashSet<>();
    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    OWLOntology onto = manager.createOntology();
    Map<String, Set<String>> ontologyLocation = config.getOntologyLocation();

    for (Map.Entry<String, Set<String>> entry : ontologyLocation.entrySet()) {
      switch (entry.getKey()) {
        case ConfigKeys.ONTOLOGY_DIR:
          for (String dir : entry.getValue()) {
            Path dirPath = fsm.getPathToOntologyFile(dir);
            LOG.debug("directory path: {}", dirPath.toAbsolutePath().toString());
            AutoIRIMapper autoIRIMapper = new AutoIRIMapper(dirPath.toAbsolutePath().toFile(), true);
            autoIRIMapper.setFileExtensions(supportedExtensions);
            autoIRIMapper.update();
            VersionIriMapper versionIriMapper = new VersionIriMapper(dirPath.toAbsolutePath());
            versionIriMapper.mapOntologyVersion(autoIRIMapper);
            manager.getIRIMappers().add(autoIRIMapper, versionIriMapper);
            irisToLoad.addAll(autoIRIMapper.getOntologyIRIs());
          }

          break;
        case ConfigKeys.ONTOLOGY_URL:
          entry.getValue()
                  .stream()
                  .map((url) -> IRI.create(url))
                  .forEachOrdered((iri) -> {
                    irisToLoad.add(iri);
                  });
          break;

        case ConfigKeys.ONTOLOGY_PATH:

          for (String path : entry.getValue()) {
            Path pathToOnto = fsm.getPathToOntologyFile(path);
            IRI iri = IRI.create(pathToOnto.toUri());
            irisToLoad.add(iri);
          }
          break;

      }

    }
    LOG.debug("Mappers count: {}", manager.getIRIMappers().size());
    onto = loadOntologiesFromIRIs(irisToLoad, onto, manager);
    return onto;
  }

  private OWLOntology loadOntologiesFromIRIs(Set<IRI> iris, OWLOntology onto, OWLOntologyManager manager) throws OWLOntologyCreationException {

    for (IRI iri : iris) {
      //LOG.debug("Now load ontology with iri {}", iri);
      OWLOntology o = manager.loadOntology(iri);
      OWLImportsDeclaration importDeclaration = manager.getOWLDataFactory()
              .getOWLImportsDeclaration(iri);
      AddImport importt = new AddImport(onto, importDeclaration);
      onto.applyDirectChange(importt);
      //onto.applyChange(importt);
      manager.makeLoadImportRequest(importDeclaration);
    }
    manager.makeLoadImportRequest(new OWLImportsDeclarationImpl(manager.getOntologyDocumentIRI(onto)));	
    try (Stream<OWLOntology> imports = manager.imports(onto)) {	
      LOG.debug("create ontology");	
      onto = manager.createOntology(IRI.create(""), imports, false);	
    }
    return onto;
  }
}
