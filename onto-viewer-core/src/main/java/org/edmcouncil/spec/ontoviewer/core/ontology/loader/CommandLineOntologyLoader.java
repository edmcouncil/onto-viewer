package org.edmcouncil.spec.ontoviewer.core.ontology.loader;

import com.google.common.collect.Streams;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.CoreConfiguration;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyDocumentAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLineOntologyLoader {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommandLineOntologyLoader.class);

  private final CoreConfiguration coreConfiguration;

  public CommandLineOntologyLoader(CoreConfiguration coreConfiguration) {
    this.coreConfiguration = coreConfiguration;
  }

  public OWLOntology load() throws OWLOntologyCreationException {
    var owlOntologyManager = OWLManager.createOWLOntologyManager();

    setOntologyMapping(owlOntologyManager);

    var ontologyIrisToLoad = new HashSet<IRI>();
    var ontologyMappings = new HashMap<IRI, String>();

    var ontologyLocations = coreConfiguration.getOntologyLocation();
    for (Map.Entry<String, Set<String>> ontologyLocation : ontologyLocations.entrySet()) {
      switch (ontologyLocation.getKey()) {
        case ConfigKeys.ONTOLOGY_DIR: {
          LOGGER.warn("Loading by ontology dir not implemented!");

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

          var mappings = Streams.zip(documentIris.stream(),
                  ontologyLocation.getValue().stream(),
                  ImmutablePair::new)
              .collect(Collectors.toMap(pair -> pair.left, pair -> pair.right));
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
            .add(new SimpleIRIMapper(documentIri, IRI.create(new File(fileIri))));
      } else {
        LOGGER.warn("File ('{}') that should map to an ontology ('{}') doesn't exist.",
            documentIri, fileIri);
      }
    });

    return loadOntologiesFromIris(owlOntologyManager, ontologyIrisToLoad);
  }

  private void setOntologyMapping(OWLOntologyManager owlOntologyManager) {
    var ontologyMapping = coreConfiguration.getOntologyMapping();
    ontologyMapping.forEach((ontologyIri, ontologyPath) ->
        owlOntologyManager.getIRIMappers().add(
            new SimpleIRIMapper(
                IRI.create(ontologyIri),
                IRI.create(new File(ontologyPath.toString())))));
  }

  private OWLOntology loadOntologiesFromIris(
      OWLOntologyManager ontologyManager,
      Set<IRI> ontologyIrisToLoad) throws OWLOntologyCreationException {
    ontologyManager.getOntologyLoaderConfiguration()
        .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);

    var umbrellaOntology = ontologyManager.createOntology();

    for (IRI ontologyIri : ontologyIrisToLoad) {
      LOGGER.debug("Loading ontology from IRI '{}'...", ontologyIri);

      try {
        var currentOntology = ontologyManager.loadOntology(ontologyIri);

        LOGGER.debug("Loaded '{}' ontology with {} axioms.",
            ontologyIri,
            currentOntology.getLogicalAxiomCount());

        var importDeclaration =
            ontologyManager.getOWLDataFactory().getOWLImportsDeclaration(ontologyIri);

        var addImport = new AddImport(umbrellaOntology, importDeclaration);
        umbrellaOntology.applyChange(addImport);
      } catch (OWLOntologyAlreadyExistsException | OWLOntologyDocumentAlreadyExistsException ex) {
        LOGGER.warn(String.format("Ontology '%s' has already been loaded.", ontologyIri));
      }
    }

    return umbrellaOntology;
  }
}
