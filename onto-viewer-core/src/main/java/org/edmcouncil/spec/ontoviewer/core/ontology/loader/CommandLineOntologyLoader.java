package org.edmcouncil.spec.ontoviewer.core.ontology.loader;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemService;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.LoadedOntologyData.LoadingDetails;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.OntologySource.SourceType;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.listener.MissingImport;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.listener.MissingImportListenerImpl;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.zip.ViewerZipFilesOperations;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyDocumentAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLineOntologyLoader extends AbstractOntologyLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineOntologyLoader.class);

  private final ConfigurationData configurationData;
  private final MissingImportListenerImpl missingImportListenerImpl;
  private final FileSystemService fileSystemService;

  public CommandLineOntologyLoader(ConfigurationData configurationData,
      FileSystemService fileSystemService) {
    this.configurationData = configurationData;
    this.missingImportListenerImpl = new MissingImportListenerImpl();
    this.fileSystemService = fileSystemService;
  }

  public LoadedOntologyData load() throws OWLOntologyCreationException {
    ViewerZipFilesOperations viewerZipFilesOperations = new ViewerZipFilesOperations();
    Set<MissingImport> missingImports = viewerZipFilesOperations
        .prepareZipToLoad(configurationData, fileSystemService);
    this.missingImportListenerImpl.addAll(missingImports);

    var owlOntologyManager = OWLManager.createOWLOntologyManager();

    setOntologyMapping(owlOntologyManager);

    var ontologySources = prepareOntologySources();
    ontologySources.forEach(ontologySource -> {
      if (ontologySource.getSourceType() == SourceType.FILE) {
        if (Files.exists(Path.of(ontologySource.getLocation()))) {
          owlOntologyManager.getIRIMappers()
              .add(
                  new SimpleIRIMapper(
                      IRI.create(ontologySource.getLocation()),
                      IRI.create(new File(ontologySource.getLocation()))));
        }
      }
    });

    var loadedOntologyData = loadOntologiesFromIris(owlOntologyManager, ontologySources);
    loadedOntologyData = new LoadedOntologyData(
        loadedOntologyData,
        new LoadingDetails(new ArrayList<>(missingImportListenerImpl.getNotImportUri())));
    return loadedOntologyData;
  }

  private Set<OntologySource> prepareOntologySources() {
    Set<OntologySource> ontologySources = new HashSet<>();

    var ontologiesConfig = configurationData.getOntologiesConfig();
    var ontologyPaths = ontologiesConfig.getPaths();
    for (String ontologyPathString : ontologyPaths) {
      Path ontologyPath = Path.of(ontologyPathString);

      if (!ontologyPath.isAbsolute()) {
        ontologyPath = ontologyPath.toAbsolutePath();
      }

      if (Files.isDirectory(ontologyPath)) {
        mappingDirectory(ontologyPath, ontologySources, ontologyPathString);
      } else {
        ontologySources.add(new OntologySource(ontologyPath.toString(), ontologyPathString, SourceType.FILE));
      }
    }

    for (String ontologyUrl : ontologiesConfig.getUrls()) {
      ontologySources.add(new OntologySource(ontologyUrl, SourceType.URL));
    }

    return ontologySources;
  }

  private void setOntologyMapping(OWLOntologyManager owlOntologyManager) {
    var ontologyMapping = configurationData.getOntologiesConfig().getOntologyMappings();
    ontologyMapping
        .forEach((ontologyIri, ontologyPath) ->
            owlOntologyManager.getIRIMappers().add(
                new SimpleIRIMapper(
                    IRI.create(ontologyIri),
                    IRI.create(new File(ontologyPath)))));
  }

  private LoadedOntologyData loadOntologiesFromIris(OWLOntologyManager ontologyManager, Set<OntologySource> ontologySources)
      throws OWLOntologyCreationException {
    Map<IRI, IRI> ontologyIrisToPaths = new HashMap<>();
    Map<String, IRI> ontologyPathsToIris = new HashMap<>();

    var loaderConfiguration = new OWLOntologyLoaderConfiguration();
    loaderConfiguration = loaderConfiguration.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
    ontologyManager.setOntologyLoaderConfiguration(loaderConfiguration);
    ontologyManager.addMissingImportListener(missingImportListenerImpl);

    var umbrellaOntology = ontologyManager.createOntology();
    Map<String, String> sourceNamespacesMap = new HashMap<>();

    for (OntologySource ontologySource : ontologySources) {
      LOGGER.debug("Loading ontology from IRI '{}'...", ontologySource);

      try {
        var currentOntology = ontologyManager.loadOntology(ontologySource.getAsIri());
        OWLDocumentFormat ontologyFormat = ontologyManager.getOntologyFormat(currentOntology);
        if (ontologyFormat != null) {
          sourceNamespacesMap.putAll(ontologyFormat.asPrefixOWLDocumentFormat().getPrefixName2PrefixMap());
        }

        var ontologyIri = currentOntology.getOntologyID().getOntologyIRI().orElse(ontologySource.getAsIri());
        ontologyIrisToPaths.put(ontologyIri, ontologySource.getAsIri());
        ontologyPathsToIris.put(ontologySource.getOriginalLocation(), ontologyIri);

        LOGGER.debug("Loaded '{}' ontology with {} axioms.",
            ontologySource,
            currentOntology.getLogicalAxiomCount());

        var importDeclaration = ontologyManager
            .getOWLDataFactory()
            .getOWLImportsDeclaration(ontologyIri);

        var addImport = new AddImport(umbrellaOntology, importDeclaration);
        umbrellaOntology.applyChange(addImport);
      } catch (OWLOntologyAlreadyExistsException | OWLOntologyDocumentAlreadyExistsException ex) {
        LOGGER.warn(String.format("Ontology '%s' has already been loaded.", ontologySource));
      }
    }

    return new LoadedOntologyData(umbrellaOntology, ontologyIrisToPaths, ontologyPathsToIris, sourceNamespacesMap);
  }
}