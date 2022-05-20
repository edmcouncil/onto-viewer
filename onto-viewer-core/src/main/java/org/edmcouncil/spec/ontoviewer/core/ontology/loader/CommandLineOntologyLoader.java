package org.edmcouncil.spec.ontoviewer.core.ontology.loader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.listener.MissingImport;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.listener.MissingImportListenerImpl;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.zip.ViewerZipFilesOperations;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyDocumentAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLineOntologyLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineOntologyLoader.class);

  private final ConfigurationData configurationData;
  private final MissingImportListenerImpl missingImportListenerImpl;
  private final FileSystemManager fileSystemManager;

  public CommandLineOntologyLoader(ConfigurationData configurationData,
      FileSystemManager fileSystemManager) {
    this.configurationData = configurationData;
    this.missingImportListenerImpl = new MissingImportListenerImpl();
    this.fileSystemManager = fileSystemManager;
  }

  public OWLOntology load() throws OWLOntologyCreationException {

    ViewerZipFilesOperations viewerZipFilesOperations = new ViewerZipFilesOperations();
    Set<MissingImport> missingImports = viewerZipFilesOperations
        .prepareZipToLoad(configurationData, fileSystemManager);
    this.missingImportListenerImpl.addAll(missingImports);
    
    var owlOntologyManager = OWLManager.createOWLOntologyManager();

    setOntologyMapping(owlOntologyManager);

    var ontologyIrisToLoad = new HashSet<IRI>();
    var ontologyMappings = new HashMap<String, String>();

    var ontologyLocations = configurationData.getOntologiesConfig().getPaths();
    for (String ontologyLocation : ontologyLocations) {
      ontologyIrisToLoad.add(IRI.create(Path.of(ontologyLocation).toFile()));

      ontologyMappings.put(ontologyLocation, ontologyLocation);
    }

    var ontologyUrls = configurationData.getOntologiesConfig().getUrls();
    for (String ontologyUrl : ontologyUrls) {
      ontologyIrisToLoad.add(IRI.create(ontologyUrl));
    }

    ontologyMappings.forEach((documentIri, fileIri) -> {
      if (Files.exists(Path.of(fileIri))) {
        owlOntologyManager.getIRIMappers()
            .add(new SimpleIRIMapper(IRI.create(documentIri), IRI.create(fileIri)));
      } else {
        LOGGER.warn("File ('{}') that should map to an ontology ('{}') doesn't exist.",
            documentIri, fileIri);
      }
    });

    return loadOntologiesFromIris(owlOntologyManager, ontologyIrisToLoad);
  }

  private void setOntologyMapping(OWLOntologyManager owlOntologyManager) {
    var ontologyMapping = configurationData.getOntologiesConfig().getOntologyMappings();
      ontologyMapping.forEach((ontologyIri, ontologyPath)
        -> owlOntologyManager.getIRIMappers().add(
            new SimpleIRIMapper(
                IRI.create(ontologyIri),
                IRI.create(new File(ontologyPath)))));
  }

  private OWLOntology loadOntologiesFromIris(OWLOntologyManager ontologyManager, Set<IRI> ontologyIrisToLoad)
      throws OWLOntologyCreationException {
    var loaderConfiguration = new OWLOntologyLoaderConfiguration();
    loaderConfiguration = loaderConfiguration.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
    ontologyManager.setOntologyLoaderConfiguration(loaderConfiguration);
    ontologyManager.addMissingImportListener(missingImportListenerImpl);

    var umbrellaOntology = ontologyManager.createOntology();

    for (IRI ontologyIri : ontologyIrisToLoad) {
      LOGGER.debug("Loading ontology from IRI '{}'...", ontologyIri);

      try {
        var currentOntology = ontologyManager.loadOntology(ontologyIri);

        LOGGER.debug("Loaded '{}' ontology with {} axioms.",
            ontologyIri,
            currentOntology.getLogicalAxiomCount());

        var importDeclaration = ontologyManager.getOWLDataFactory().getOWLImportsDeclaration(ontologyIri);

        var addImport = new AddImport(umbrellaOntology, importDeclaration);
        umbrellaOntology.applyChange(addImport);
      } catch (OWLOntologyAlreadyExistsException | OWLOntologyDocumentAlreadyExistsException ex) {
        LOGGER.warn(String.format("Ontology '%s' has already been loaded.", ontologyIri));
      }
    }

    return umbrellaOntology;
  }
}
