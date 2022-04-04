package org.edmcouncil.spec.ontoviewer.core.ontology.loader;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.CoreConfiguration;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.listener.MissingImportListenerImpl;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.slf4j.Logger;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 * @author Michal Daniel (michal.daniel@makolab.com)
 */
public class AutoOntologyLoader {

  private static final Logger LOGGER = getLogger(AutoOntologyLoader.class);
  private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("rdf", "owl", "ttl");

  private final CoreConfiguration coreConfiguration;
  private final FileSystemManager fileSystemManager;
  private final MissingImportListenerImpl missingImportListenerImpl;

  public AutoOntologyLoader(CoreConfiguration viewerCoreConfiguration, FileSystemManager fileSystemManager) {
    this.coreConfiguration = viewerCoreConfiguration;
    this.fileSystemManager = fileSystemManager;
    this.missingImportListenerImpl = new MissingImportListenerImpl();
  }

  public LoadedOntologyData load() throws OWLOntologyCreationException {
    var owlOntologyManager = OWLManager.createOWLOntologyManager();

    loadMappersToOntologyManager(owlOntologyManager);

    Set<IRI> ontologyIrisToLoad = new HashSet<>();

    var ontologyLocations = coreConfiguration.getOntologyLocation();
    for (Map.Entry<String, Set<String>> ontologyLocation : ontologyLocations.entrySet()) {
      switch (ontologyLocation.getKey()) {
        case ConfigKeys.ONTOLOGY_DIR: {
          for (String dir : ontologyLocation.getValue()) {
            Path directoryPath = Path.of(dir);

            if (!directoryPath.isAbsolute()) {
              // If the provided directory path is not absolute, we want to resolve it based on the app's home dir
              directoryPath = fileSystemManager.getViewerHomeDir().resolve(directoryPath);
            }

            if (Files.isDirectory(directoryPath)) {
              mappingDirectory(directoryPath, ontologyIrisToLoad);
            } else {
              LOGGER.warn("Expected '{}' to be a directory but it isn't.", directoryPath);
            }
          }
          break;
        }
        case ConfigKeys.ONTOLOGY_URL: {
          var ontologyIris = ontologyLocation.getValue().stream()
              .map(IRI::create)
              .collect(Collectors.toSet());
          ontologyIrisToLoad.addAll(ontologyIris);
          break;
        }
        case ConfigKeys.ONTOLOGY_PATH: {
          var documentIris = ontologyLocation.getValue().stream()
              .map(rawPath -> IRI.create(Path.of(rawPath).toFile()))
              .collect(Collectors.toSet());
          ontologyIrisToLoad.addAll(documentIris);
          break;
        }
        default:
          LOGGER.warn("Unknown key '{}' for ontology location, value: {}",
              ontologyLocation.getKey(),
              ontologyLocation.getValue());
      }
    }

    ontologyIrisToLoad
        .forEach(ontologyIri -> {
          if (Files.exists(Path.of(ontologyIri.toString()))) {
            owlOntologyManager
                .getIRIMappers()
                .add(new SimpleIRIMapper(ontologyIri, IRI.create(new File(ontologyIri.toString()))));
          } else {
            LOGGER.warn("File '{}' that should map to an ontology doesn't exist.", ontologyIri);
          }
        });

    Map<IRI, IRI> ontologyIrisToPaths = new HashMap<>();
    OWLOntology umbrellaOntology = loadOntologiesFromIris(owlOntologyManager, ontologyIrisToLoad, ontologyIrisToPaths);
    return new LoadedOntologyData(umbrellaOntology, ontologyIrisToPaths);
  }

  private void loadMappersToOntologyManager(OWLOntologyManager manager) {
    var ontologyMapping = coreConfiguration.getOntologyMapping();
    ontologyMapping.forEach((ontologyIri, ontologyPath)
        -> manager.getIRIMappers().add(
        new SimpleIRIMapper(
            IRI.create(ontologyIri),
            IRI.create(new File(ontologyPath.toString())))));
  }

  private OWLOntology loadOntologiesFromIris(OWLOntologyManager ontologyManager,
      Set<IRI> ontologyDocumentIris,
      Map<IRI, IRI> ontologyIrisToPaths)
      throws OWLOntologyCreationException {
    var umbrellaOntology = ontologyManager.createOntology();

    OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
    config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
    ontologyManager.setOntologyLoaderConfiguration(config);
    ontologyManager.addMissingImportListener(missingImportListenerImpl);

    for (IRI ontologyDocumentIri : ontologyDocumentIris) {
      try {
        var currentOntology = ontologyManager.loadOntology(ontologyDocumentIri);
        var ontologyIri = currentOntology.getOntologyID().getOntologyIRI().orElse(ontologyDocumentIri);
        ontologyIrisToPaths.put(ontologyIri, ontologyDocumentIri);

        LOGGER.debug("Loaded '{}' ontology with {} axioms.",
            ontologyIri,
            currentOntology.getLogicalAxiomCount());

        var importDeclaration
            = ontologyManager.getOWLDataFactory().getOWLImportsDeclaration(ontologyIri);

        var addImport = new AddImport(umbrellaOntology, importDeclaration);
        umbrellaOntology.applyDirectChange(addImport);
        ontologyManager.makeLoadImportRequest(importDeclaration);
      } catch (OWLOntologyCreationException | OWLRuntimeException ex) {
        var message = String.format("Exception occurred while loading ontology with IRI '%s'. Cause: %s (%s)",
            ontologyDocumentIri, ex.getCause(), ex.getMessage());
        LOGGER.warn(message);
      }
    }
    LOGGER.info("Missing imports: {}", missingImportListenerImpl.getNotImportUri().toString());
    return umbrellaOntology;
  }

  public MissingImportListenerImpl getMissingImportListenerImpl() {
    return missingImportListenerImpl;
  }

  private void mappingDirectory(Path ontologiesDirPath, Set<IRI> ontologyIrisToLoad) {
    if (ontologiesDirPath == null || Files.notExists(ontologiesDirPath)) {
      LOGGER.warn("Path '{}' doesn't exist.", ontologiesDirPath);
      return;
    }

    try (var pathsStream = Files.walk(ontologiesDirPath)) {
      var paths = pathsStream.collect(Collectors.toSet());

      for (Path path : paths) {
        if (path.equals(ontologiesDirPath)) {
          continue;
        }

        if (Files.isRegularFile(path)) {
          String fileExtension = FilenameUtils.getExtension(path.toString());
          if (SUPPORTED_EXTENSIONS.contains(fileExtension)) {
            ontologyIrisToLoad.add(IRI.create(path.toString()));
          } else {
            LOGGER.debug("File with extension '{}' is not supported. Supported extensions: {}",
                fileExtension, SUPPORTED_EXTENSIONS);
          }
        } else if (Files.isDirectory(path)) {
          mappingDirectory(path, ontologyIrisToLoad);
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
