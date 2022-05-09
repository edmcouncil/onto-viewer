package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.YamlFileBasedConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.model.module.FiboModule;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.BaseTest;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.extractor.OwlDataExtractor;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo.AppFiboMaturityLevel;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo.FiboMaturityLevel;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo.FiboOntologyHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.CustomDataFactory;
import org.edmcouncil.spec.ontoviewer.core.ontology.scope.ScopeIriOntology;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

class ModuleHandlerTest extends BaseTest {

  private static final String ROOT_IRI = "http://www.example.com/modules#";
  private static final FiboMaturityLevel DEV_MATURITY_LEVEL = new AppFiboMaturityLevel("dev");
  private static final FiboMaturityLevel PROD_MATURITY_LEVEL = new AppFiboMaturityLevel("prod");

  @Test
  void shouldReturnListOfModulesDefinedInOntology() {
    var moduleHandler = prepareModuleHandler("/ontology/modules.rdf");

    var actualModules = moduleHandler.getModules();

    var expectedModules = List.of(
        createModule("ModuleA",
            List.of(
                createModule("ModuleA_1",
                    List.of(
                        createModule("ModuleA_1_a", emptyList(), PROD_MATURITY_LEVEL)
                    ),
                    PROD_MATURITY_LEVEL
                ),
                createModule("ModuleA_2", emptyList(), PROD_MATURITY_LEVEL),
                createModule("ModuleA_3", emptyList(), PROD_MATURITY_LEVEL)
            ),
            PROD_MATURITY_LEVEL
        ),
        createModule("ModuleB",
            List.of(
                createModule("ModuleB_1", emptyList(), PROD_MATURITY_LEVEL),
                createModule("ModuleB_2", emptyList(), PROD_MATURITY_LEVEL)
            ),
            PROD_MATURITY_LEVEL
        ),
        createModule("ModuleC", emptyList(), PROD_MATURITY_LEVEL)
    );

    assertEquals(expectedModules, actualModules);
  }

  @Test
  void shouldReturnListOfModulesWhenNotDefinedInOntology() {
    var moduleHandler = prepareModuleHandler("/ontology/MortgageLoansWithoutImports.rdf");

    var actualModules = moduleHandler.getModules();

    var expectedModules = List.of(
        new FiboModule("https://spec.edmcouncil.org/fibo/ontology/LOAN/LoanTypes/MortgageLoans/",
            "MortgageLoans",
            emptyList(),
            DEV_MATURITY_LEVEL)
    );

    assertEquals(expectedModules, actualModules);
  }

  private ModuleHandler prepareModuleHandler(String ontologyPath) {
    var configurationService = new YamlFileBasedConfigurationService(prepareFileSystem());
    configurationService.init();

    var ontologyManager = getOntologyManager(ontologyPath);
    ontologyManager.setIriToPathMapping(
        Map.of(
            IRI.create("https://spec.edmcouncil.org/fibo/ontology/LOAN/LoanTypes/MortgageLoans/"),
            IRI.create("file://some_random_path.rdf")));
    var owlDataExtractor = new OwlDataExtractor();
    var customDataFactory = new CustomDataFactory();
    var labelProvider = new LabelProvider(configurationService, ontologyManager);
    var individualDataHandler = new IndividualDataHandler(labelProvider);
    var scopeIriOntology = new ScopeIriOntology();
    var annotationsDataHandler = new AnnotationsDataHandler(owlDataExtractor, customDataFactory, scopeIriOntology);
    var fiboOntologyHandler = new FiboOntologyHandler(ontologyManager, labelProvider, annotationsDataHandler);

    return new ModuleHandler(ontologyManager,
        individualDataHandler,
        labelProvider,
        fiboOntologyHandler,
        configurationService);
  }

  private OntologyManager getOntologyManager(String ontologyPath) {
    try {
      var exampleOntologyPath = getClass().getResourceAsStream(ontologyPath);
      if (exampleOntologyPath == null) {
        throw new IllegalStateException(
            String.format("Example ontology in path '%s' not found.", ontologyPath));
      }
      var owlOntologyManager = OWLManager.createOWLOntologyManager();
      var ontology = owlOntologyManager.loadOntologyFromOntologyDocument(exampleOntologyPath);

      var ontologyManager = new OntologyManager();
      ontologyManager.updateOntology(ontology);
      return ontologyManager;
    } catch (OWLOntologyCreationException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private FiboModule createModule(String name, List<FiboModule> subModules, FiboMaturityLevel maturityLevel) {
    return new FiboModule(ROOT_IRI + name, name, subModules, maturityLevel);
  }
}