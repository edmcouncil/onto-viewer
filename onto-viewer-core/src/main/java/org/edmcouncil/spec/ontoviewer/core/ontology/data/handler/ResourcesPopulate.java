package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import com.google.common.base.Stopwatch;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.module.ModuleHelper;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.module.ModuleHandler;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResourcesPopulate {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResourcesPopulate.class);

  private ModuleHandler moduleHandler;
  private OntologyManager ontologyManager;
  private ModuleHelper moduleHelper;

  public ResourcesPopulate(ModuleHandler moduleHandler, OntologyManager ontologyManager,
      ModuleHelper moduleHelper) {
    this.moduleHandler = moduleHandler;
    this.ontologyManager = ontologyManager;
    this.moduleHelper = moduleHelper;
  }

  public void populateOntologyResources() {
    LOGGER.info("Start populating ontology mapping of entity IRIs to ontology IRIs...");
    var stopwatch = Stopwatch.createStarted();

    moduleHelper.setEntityIriToOntologyIriMap(populateEntityIriToOntologyIriMap());
    this.moduleHandler.updateModules();

    LOGGER.info("Finished populating mapping entity IRIs to ontology IRIs in {} seconds.",
        stopwatch.elapsed(TimeUnit.SECONDS));
  }

  private Map<IRI, IRI> populateEntityIriToOntologyIriMap() {
    Map<IRI, IRI> entityIriToOntologyIri = new HashMap<>();

    ontologyManager.getOntologyWithImports()
        .forEach(owlOntology -> {
          var ontologyIriOptional = owlOntology.getOntologyID().getOntologyIRI();

          if (ontologyIriOptional.isPresent()) {
            var ontologyIri = ontologyIriOptional.get();

            owlOntology.signature(Imports.EXCLUDED)
                .forEach(owlEntity -> {
                  var entityIri = owlEntity.getIRI();
                  entityIriToOntologyIri.put(entityIri, ontologyIri);
                });
          }
        });

    return entityIriToOntologyIri;
  }
}
