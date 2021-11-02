package org.edmcouncil.spec.ontoviewer.core.ontology.loader;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.CoreConfiguration;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.MissingImportHandlingStrategy;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
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

    var ontologyIrisToLoad = new HashSet<IRI>();

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

          break;
        }
        default:
          LOGGER.warn("Unknown key '{}' for ontology location, value: {}",
              ontologyLocation.getKey(),
              ontologyLocation.getValue());
      }
    }

    return loadOntologiesFromIris(owlOntologyManager, ontologyIrisToLoad);
  }

  private OWLOntology loadOntologiesFromIris(
      OWLOntologyManager ontologyManager,
      Set<IRI> ontologyIrisToLoad) throws OWLOntologyCreationException {
    ontologyManager.getOntologyLoaderConfiguration()
        .setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);

    var umbrellaOntology = ontologyManager.createOntology();

    for (IRI ontologyIri : ontologyIrisToLoad) {
      LOGGER.debug("Loading ontology from IRI '{}'...", ontologyIri);

      var currentOntology = ontologyManager.loadOntology(ontologyIri);
//      var currentOntology = ontologyManager.loadOntologyFromOntologyDocument(ontologyIri);

      LOGGER.debug("Loaded '{}' ontology with {} axioms.",
          ontologyIri,
          currentOntology.getLogicalAxiomCount());

      var importDeclaration =
          ontologyManager.getOWLDataFactory().getOWLImportsDeclaration(ontologyIri);

      var addImport = new AddImport(umbrellaOntology, importDeclaration);
      umbrellaOntology.applyChange(addImport);
    }

    return umbrellaOntology;
  }
}
