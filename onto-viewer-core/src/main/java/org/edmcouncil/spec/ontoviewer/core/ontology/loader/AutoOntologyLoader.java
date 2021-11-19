package org.edmcouncil.spec.ontoviewer.core.ontology.loader;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.CoreConfiguration;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.mapper.SimpleOntologyMapperCreator;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.mapper.VersionIriMapper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.AutoIRIMapper;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 * @author Michal Daniel (michal.daniel@makolab.com)
 */

public class AutoOntologyLoader {

  private static final Logger LOGGER = getLogger(AutoOntologyLoader.class);
  private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(".rdf", ".owl");

  private final FileSystemManager fileSystemManager;
  private final CoreConfiguration coreConfiguration;

  public AutoOntologyLoader(FileSystemManager fileSystemManager,
      CoreConfiguration viewerCoreConfiguration) {
    this.fileSystemManager = fileSystemManager;
    this.coreConfiguration = viewerCoreConfiguration;
  }

  public OWLOntology load() throws OWLOntologyCreationException, IOException,
      ParserConfigurationException, XPathExpressionException, SAXException {
    Set<IRI> irisToLoad = new HashSet<>();
    var manager = OWLManager.createOWLOntologyManager();

    loadMappersToOntologyManager(manager, coreConfiguration.getOntologyMapper());

    Map<String, Set<String>> ontologyLocations = coreConfiguration.getOntologyLocation();
    for (Map.Entry<String, Set<String>> ontologyLocationEntry : ontologyLocations.entrySet()) {
      switch (ontologyLocationEntry.getKey()) {
        case ConfigKeys.ONTOLOGY_DIR:
          for (String dir : ontologyLocationEntry.getValue()) {
            Path dirPath = fileSystemManager.getPathToOntologyFile(dir);
            LOGGER.debug("directory path: {}", dirPath.toAbsolutePath());

            var autoIRIMapper = new AutoIRIMapper(dirPath.toAbsolutePath().toFile(), true);
            autoIRIMapper.setFileExtensions(SUPPORTED_EXTENSIONS);
            autoIRIMapper.update();
            VersionIriMapper versionIriMapper = new VersionIriMapper(dirPath.toAbsolutePath());
            versionIriMapper.mapOntologyVersion(autoIRIMapper);
            manager.getIRIMappers().add(autoIRIMapper, versionIriMapper);
            irisToLoad.addAll(autoIRIMapper.getOntologyIRIs());
          }

          break;
        case ConfigKeys.ONTOLOGY_URL:
          ontologyLocationEntry.getValue()
              .forEach(ontologyUrl -> irisToLoad.add(IRI.create(ontologyUrl)));

          break;

        case ConfigKeys.ONTOLOGY_PATH:
          for (String path : ontologyLocationEntry.getValue()) {
            Path pathToOnto = fileSystemManager.getPathToOntologyFile(path);
            IRI iri = IRI.create(pathToOnto.toUri());
            irisToLoad.add(iri);
            var mappers = SimpleOntologyMapperCreator.createAboutMapper(new File(path));
            mappers.addAll(SimpleOntologyMapperCreator.createVersionMapper(new File(path)));
            mappers.forEach(mapper -> manager.getIRIMappers().add(mapper));
          }

          break;
      }
    }

    LOGGER.debug("From configuration {} mappings has been read.", manager.getIRIMappers().size());
    return loadOntologiesFromIRIs(manager, irisToLoad);
  }

  private void loadMappersToOntologyManager(OWLOntologyManager manager,
      Set<String> listOfMappersDirectory) throws IOException, ParserConfigurationException,
      XPathExpressionException, SAXException {
    for (String mapperStringPath : listOfMappersDirectory) {
      LOGGER.debug("Prepare Mapper for directory: {}", mapperStringPath);
      Path mapperPath = fileSystemManager.getPathToOntologyFile(mapperStringPath);
      AutoIRIMapper autoIRIMapper = new AutoIRIMapper(mapperPath.toAbsolutePath().toFile(), true);
      autoIRIMapper.setFileExtensions(SUPPORTED_EXTENSIONS);
      autoIRIMapper.update();
      VersionIriMapper versionIriMapper = new VersionIriMapper(mapperPath.toAbsolutePath());
      versionIriMapper.mapOntologyVersion(autoIRIMapper);
      manager.getIRIMappers().add(autoIRIMapper, versionIriMapper);
    }
  }

  private OWLOntology loadOntologiesFromIRIs(OWLOntologyManager ontologyManager, Set<IRI> iris)
      throws OWLOntologyCreationException {
    var umbrellaOntology = ontologyManager.createOntology();

    for (IRI ontologyIri : iris) {
      var currentOntology = ontologyManager.loadOntology(ontologyIri);

      LOGGER.debug("Loaded '{}' ontology with {} axioms.",
          ontologyIri,
          currentOntology.getLogicalAxiomCount());

      var importDeclaration =
          ontologyManager.getOWLDataFactory().getOWLImportsDeclaration(ontologyIri);

      var addImport = new AddImport(umbrellaOntology, importDeclaration);
      umbrellaOntology.applyDirectChange(addImport);
      ontologyManager.makeLoadImportRequest(importDeclaration);
    }

    return umbrellaOntology;
  }
}