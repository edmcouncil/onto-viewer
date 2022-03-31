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
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.CoreConfiguration;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.listener.MissingImportListenerImpl;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
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

  private final CoreConfiguration coreConfiguration;
  private final MissingImportListenerImpl missingImportListenerImpl;

  public AutoOntologyLoader(CoreConfiguration viewerCoreConfiguration) {
    this.coreConfiguration = viewerCoreConfiguration;
    this.missingImportListenerImpl = new MissingImportListenerImpl();
  }

  public LoadedOntologyData load() throws OWLOntologyCreationException, IOException {
    var owlOntologyManager = OWLManager.createOWLOntologyManager();

    loadMappersToOntologyManager(owlOntologyManager);

    var ontologyIrisToLoad = new HashSet<IRI>();
    Map<IRI, IRI> ontologiesIrisToPaths = new HashMap<>();
    var ontologyMappings = new HashMap<String, String>();

    var ontologyLocations = coreConfiguration.getOntologyLocation();
    for (Map.Entry<String, Set<String>> ontologyLocation : ontologyLocations.entrySet()) {
      switch (ontologyLocation.getKey()) {
        case ConfigKeys.ONTOLOGY_DIR: {
          for (String dir : ontologyLocation.getValue()) {
            File file = new File(dir);
            mappingDirectory(file, ontologyIrisToLoad);
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

          var mappings = documentIris.stream()
              .collect(Collectors.toMap(IRI::toString, IRI::toString));
          ontologyMappings.putAll(mappings);
          break;
        }
        default:
          LOGGER.warn("Unknown key '{}' for ontology location, value: {}",
              ontologyLocation.getKey(),
              ontologyLocation.getValue());
      }
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
    return new LoadedOntologyData(loadOntologiesFromIris(owlOntologyManager, ontologyIrisToLoad), ontologiesIrisToPaths);
  }

  private void loadMappersToOntologyManager(OWLOntologyManager manager) {
    var ontologyMapping = coreConfiguration.getOntologyMapping();
    ontologyMapping.forEach((ontologyIri, ontologyPath)
        -> manager.getIRIMappers().add(
            new SimpleIRIMapper(
                IRI.create(ontologyIri),
                IRI.create(new File(ontologyPath.toString())))));
  }

  private OWLOntology loadOntologiesFromIris(OWLOntologyManager ontologyManager, Set<IRI> iris)
      throws OWLOntologyCreationException {
    var umbrellaOntology = ontologyManager.createOntology();

    OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
    config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
    ontologyManager.setOntologyLoaderConfiguration(config);
    ontologyManager.addMissingImportListener(missingImportListenerImpl);

    for (IRI ontologyIri : iris) {
      try {
        var currentOntology = ontologyManager.loadOntology(ontologyIri);

        LOGGER.debug("Loaded '{}' ontology with {} axioms.",
            ontologyIri,
            currentOntology.getLogicalAxiomCount());

        var importDeclaration
            = ontologyManager.getOWLDataFactory().getOWLImportsDeclaration(ontologyIri);

        var addImport = new AddImport(umbrellaOntology, importDeclaration);
        umbrellaOntology.applyDirectChange(addImport);
        ontologyManager.makeLoadImportRequest(importDeclaration);
      } catch (OWLOntologyCreationException ex) {
        var message = String.format("Exception occurred while loading ontology with IRI '%s'. Cause: %s (%s)",
            ontologyIri, ex.getCause(), ex.getMessage());
        LOGGER.warn(message);
      }
    }
    LOGGER.info("Missing imports: {}", missingImportListenerImpl.getNotImportUri().toString());
    return umbrellaOntology;
  }

  public MissingImportListenerImpl getMissingImportListenerImpl() {
    return missingImportListenerImpl;
  }

  private void mappingDirectory(File ontologiesDir, HashSet<IRI> ontologyIrisToLoad) {
    for (File file : ontologiesDir.listFiles()) {
      LOGGER.debug("Load ontology file : {}", file.getName());

      if (file.isFile()) {
        if (!file.getName().contains("catalog-v001.xml") && !file.getName().contains("README.md")
            && !file.getName().contains("LICENSE")) {
          var ontologyIri = IRI.create(file);
          ontologyIrisToLoad.add(ontologyIri);
        }
      } else if (file.isDirectory()) {
        if (!file.getName().contains(".git")) {
          mappingDirectory(file, ontologyIrisToLoad);
        }
      }
    }
  }

  public static class LoadedOntologyData {

    private final OWLOntology ontology;
    private final Map<IRI, IRI> iriToPathMapping;

    public LoadedOntologyData(OWLOntology ontology, Map<IRI, IRI> iriToPathMapping) {
      this.ontology = ontology;
      this.iriToPathMapping = iriToPathMapping;
//      if (this.iriToPathMapping.isEmpty()) {
//        for (OWLOntology onto : ontology.getOWLOntologyManager().ontologies()
//            .collect(Collectors.toSet())) {
//          if (onto.getOntologyID().getOntologyIRI().isPresent()) {
//            onto.getOntologyID().getOntologyIRI();
//            iriToPathMapping.put(onto.getOntologyID().getOntologyIRI().get(),
//                onto.getOntologyID().getOntologyIRI().get());
//          }
//        }
//      }
    }

    public OWLOntology getOntology() {
      return ontology;
    }

    public Map<IRI, IRI> getIriToPathMapping() {
      return iriToPathMapping;
    }
  }
}
