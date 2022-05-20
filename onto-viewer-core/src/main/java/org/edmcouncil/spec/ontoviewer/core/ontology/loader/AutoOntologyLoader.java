package org.edmcouncil.spec.ontoviewer.core.ontology.loader;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.OntologiesConfig;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.edmcouncil.spec.ontoviewer.core.exception.OntoViewerException;
import org.edmcouncil.spec.ontoviewer.core.mapping.OntologyCatalogParser;
import org.edmcouncil.spec.ontoviewer.core.mapping.model.Uri;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.OntologyMapping.MappingSource;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.OntologySource.SourceType;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.listener.MissingImportListenerImpl;
import org.edmcouncil.spec.ontoviewer.core.utils.PathUtils;
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

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 * @author Michal Daniel (michal.daniel@makolab.com)
 */
public class AutoOntologyLoader {

  private static final Logger LOGGER = getLogger(AutoOntologyLoader.class);
  private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("rdf", "owl", "ttl");

  private final ConfigurationData configurationData;
  private final FileSystemManager fileSystemManager;
  private final MissingImportListenerImpl missingImportListenerImpl;

  public AutoOntologyLoader(ConfigurationData configurationData, FileSystemManager fileSystemManager) {
    this.configurationData = configurationData;
    this.fileSystemManager = fileSystemManager;
    this.missingImportListenerImpl = new MissingImportListenerImpl();
  }

  public LoadedOntologyData load() throws OWLOntologyCreationException {
    var owlOntologyManager = OWLManager.createOWLOntologyManager();

    var ontologyMappings = loadMappersToOntologyManager();
    LOGGER.info("Using the following list of ontology mappings: {}", ontologyMappings);

    var ontologySources = prepareOntologySources();
    LOGGER.info("Found the following sources of ontologies: {}", ontologySources);

    ontologySources
        .forEach(ontologySource -> {
          if (ontologySource.getSourceType() == SourceType.FILE) {
            try {
              if (Files.exists(Path.of(ontologySource.getLocation()))) {
                owlOntologyManager.getIRIMappers()
                    .add(
                        new SimpleIRIMapper(
                            IRI.create(ontologySource.getLocation()),
                            IRI.create(new File(ontologySource.getLocation()))));
              } else {
                LOGGER.warn("File '{}' that should map to an ontology doesn't exist.", ontologySource.getLocation());
              }
            } catch (InvalidPathException e) {
              LOGGER.warn("Invalid path exception for ontology source: {}", ontologySource, e);
            }
          }
        });

    ontologyMappings.forEach(ontologyMapping ->
        owlOntologyManager.getIRIMappers().add(
            new SimpleIRIMapper(
                ontologyMapping.getIri(),
                IRI.create(ontologyMapping.getPath().toFile()))));

    Map<IRI, IRI> ontologyIrisToPaths = new HashMap<>();
    OWLOntology umbrellaOntology = loadOntologiesFromOntologySources(
        owlOntologyManager,
        ontologySources,
        ontologyIrisToPaths,
        ontologyMappings);
    return new LoadedOntologyData(umbrellaOntology, ontologyIrisToPaths);
  }

  private Set<OntologySource> prepareOntologySources() {
    Set<OntologySource> ontologyIrisToLoad = new HashSet<>();

    OntologiesConfig ontologiesConfig = configurationData.getOntologiesConfig();
    for (String ontologyPathString : ontologiesConfig.getPaths()) {
      Path ontologyPath = Path.of(ontologyPathString);

      if (!ontologyPath.isAbsolute()) {
        // If the provided directory path is not absolute, we want to resolve it based on the app's home dir
        ontologyPath = fileSystemManager.getViewerHomeDir().resolve(ontologyPath);
      }

      if (Files.isDirectory(ontologyPath)) {
        mappingDirectory(ontologyPath, ontologyIrisToLoad);
      } else {
        ontologyIrisToLoad.add(new OntologySource(ontologyPath.toString(), SourceType.FILE));
      }
    }

    for (String ontologyUrl : ontologiesConfig.getUrls()) {
      ontologyIrisToLoad.add(new OntologySource(ontologyUrl, SourceType.URL));
    }

    return ontologyIrisToLoad;
  }

  private List<OntologyMapping> loadMappersToOntologyManager() {
    List<OntologyMapping> ontologyMappings = new ArrayList<>();

    var ontologyCatalogPaths = configurationData.getOntologiesConfig().getCatalogPaths();
    ontologyCatalogPaths.forEach(ontologyCatalogPath -> {
      try {
        if (!Path.of(ontologyCatalogPath).isAbsolute()) {
          ontologyCatalogPath = fileSystemManager.getViewerHomeDir().resolve(ontologyCatalogPath).toString();
        }

        var ontologyMappingParentPath = Path.of(ontologyCatalogPath).getParent();
        var catalog = new OntologyCatalogParser().readOntologyMapping(ontologyCatalogPath);

        for (Uri mapping : catalog.getUri()) {
          var ontologyPath = Path.of(mapping.getUri());
          if (!ontologyPath.isAbsolute()) {
            ontologyPath = ontologyMappingParentPath.resolve(ontologyPath).normalize().toAbsolutePath();
          }
          if (Files.exists(ontologyPath)) {
            ontologyMappings.add(
                new OntologyMapping(
                    IRI.create(mapping.getName()),
                    ontologyPath,
                    mapping.getUri(),
                    MappingSource.CATALOG_FILE)
            );
          } else {
            LOGGER.warn("File ('{}') that should map to an ontology ('{}') doesn't exist.",
                ontologyPath, mapping.getName());
          }
        }
      } catch (OntoViewerException ex) {
        LOGGER.warn("Exception thrown while loading ontology mappings from ontology catalog path '{}'. Details: {}",
            ontologyCatalogPath, ex.getMessage(), ex);
      }
    });

    return ontologyMappings;
  }

  private OWLOntology loadOntologiesFromOntologySources(OWLOntologyManager ontologyManager,
      Set<OntologySource> ontologySources,
      Map<IRI, IRI> ontologyIrisToPaths,
      List<OntologyMapping> ontologyMappings)
      throws OWLOntologyCreationException {
    var umbrellaOntology = ontologyManager.createOntology();
    var ontologyLoadingProblems = new ArrayList<OntologyLoadingProblem>();

    OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
    config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
    ontologyManager.setOntologyLoaderConfiguration(config);
    ontologyManager.addMissingImportListener(missingImportListenerImpl);

    for (OntologySource ontologySource : ontologySources) {
      try {
        var currentOntology = ontologyManager.loadOntology(ontologySource.getAsIri());
        var ontologyIri = currentOntology.getOntologyID().getOntologyIRI().orElse(ontologySource.getAsIri());
        ontologyIrisToPaths.put(ontologyIri, ontologySource.getAsIri());

        var importedOntologies = currentOntology.imports().collect(Collectors.toList());
        var importedIris = importedOntologies.stream()
            .map(owlOntology -> owlOntology.getOntologyID().getOntologyIRI().orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        LOGGER.debug("Loaded '{}' ontology with {} axioms, imported ontologies IRI: {}.",
            ontologyIri,
            currentOntology.getLogicalAxiomCount(),
            importedIris);

        var importDeclaration = ontologyManager.getOWLDataFactory().getOWLImportsDeclaration(ontologyIri);

        var addImport = new AddImport(umbrellaOntology, importDeclaration);
        umbrellaOntology.applyDirectChange(addImport);
        ontologyManager.makeLoadImportRequest(importDeclaration);
      } catch (OWLOntologyAlreadyExistsException ex) {
        var ontologyIri = ex.getOntologyID().getOntologyIRI().orElse(ex.getDocumentIRI());
        ontologyIrisToPaths.put(ontologyIri, ontologySource.getAsIri());

        ontologyLoadingProblems.add(new OntologyLoadingProblem(ontologySource, ex.getMessage(), ex.getClass()));
      } catch (OWLOntologyDocumentAlreadyExistsException ex) {
        var ontologyIri = getOntologyIriFromDocumentIri(ontologyMappings, ex.getOntologyDocumentIRI());
        if (ontologyIri != null) {
          ontologyIrisToPaths.put(ontologyIri, ex.getOntologyDocumentIRI());
        } else {
          ontologyIrisToPaths.put(ex.getOntologyDocumentIRI(), ontologySource.getAsIri());
        }

        ontologyLoadingProblems.add(new OntologyLoadingProblem(ontologySource, ex.getMessage(), ex.getClass()));
      } catch (Exception ex) {
        var message = String.format("Exception occurred while loading ontology from source '%s'. Cause: %s (%s)",
            ontologySource, ex.getCause(), ex.getMessage());
        LOGGER.warn(message);
        ontologyLoadingProblems.add(new OntologyLoadingProblem(ontologySource, ex.getMessage(), ex.getClass()));
      }
    }
    LOGGER.info("Ontology loading problems that occurred: {}", ontologyLoadingProblems);
    return umbrellaOntology;
  }

  private IRI getOntologyIriFromDocumentIri(List<OntologyMapping> ontologyMappings, IRI ontologyDocumentIri) {
    // The first branch of the following if handles Windows' paths
    Path ontologyDocumentPath = PathUtils.getPathWithoutFilePrefix(ontologyDocumentIri.toString());

    for (OntologyMapping ontologyMapping : ontologyMappings) {
      if (ontologyMapping.getPath().equals(ontologyDocumentPath)) {
        return ontologyMapping.getIri();
      }
    }
    return null;
  }

  public MissingImportListenerImpl getMissingImportListenerImpl() {
    return missingImportListenerImpl;
  }

  private void mappingDirectory(Path ontologiesDirPath, Set<OntologySource> ontologySources) {
    if (ontologiesDirPath == null || Files.notExists(ontologiesDirPath)) {
      LOGGER.warn("Directory with path '{}' doesn't exist.", ontologiesDirPath);
      return;
    }

    try (var pathsStream = Files.walk(ontologiesDirPath, FileVisitOption.FOLLOW_LINKS)) {
      var paths = pathsStream.collect(Collectors.toSet());

      for (Path path : paths) {
        if (path.equals(ontologiesDirPath)) {
          continue;
        }

        if (Files.isRegularFile(path)) {
          String fileExtension = FilenameUtils.getExtension(path.toString());
          if (SUPPORTED_EXTENSIONS.contains(fileExtension)) {
            ontologySources.add(new OntologySource(path.toString(), SourceType.FILE));
          } else {
            LOGGER.debug("File with extension '{}' is not supported. Supported extensions: {}",
                fileExtension, SUPPORTED_EXTENSIONS);
          }
        } else if (Files.isDirectory(path)) {
          mappingDirectory(path, ontologySources);
        }
      }
    } catch (IOException ex) {
      LOGGER.error(String.format("Exception thrown while iterating through directory '%s'.", ontologiesDirPath));
    }
  }

  public static class LoadedOntologyData {

    private final OWLOntology ontology;
    private final Map<IRI, IRI> iriToPathMapping;

    public LoadedOntologyData(OWLOntology ontology, Map<IRI, IRI> iriToPathMapping) {
      this.ontology = ontology;
      this.iriToPathMapping = iriToPathMapping;
    }

    public OWLOntology getOntology() {
      return ontology;
    }

    public Map<IRI, IRI> getIriToPathMapping() {
      return iriToPathMapping;
    }
  }
}
