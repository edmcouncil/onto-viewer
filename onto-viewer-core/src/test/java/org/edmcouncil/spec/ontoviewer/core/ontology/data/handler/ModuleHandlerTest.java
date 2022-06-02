package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.YamlFileBasedConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.model.module.OntologyModule;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.BaseTest;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevel;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevelDefinition;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevelFactory;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

class ModuleHandlerTest extends BaseTest {

  private static final String ROOT_IRI = "http://www.example.com/modules/";
  private static final MaturityLevel PROVISIONAL = MaturityLevelFactory.get(MaturityLevelDefinition.PROVISIONAL);
  private static final MaturityLevel RELEASE = MaturityLevelFactory.get(MaturityLevelDefinition.RELEASE);
  private static final MaturityLevel INFORMATIVE = MaturityLevelFactory.get(MaturityLevelDefinition.INFORMATIVE);
  private static final MaturityLevel MIXED = MaturityLevelFactory.get(MaturityLevelDefinition.MIXED);
  private static final MaturityLevel NOT_SET = MaturityLevelFactory.get(MaturityLevelDefinition.NOT_SET);

  @Test
  void shouldReturnListOfModulesDefinedInOntologyWithoutMaturityLevel() {
    var moduleHandler = prepareModuleHandler("/ontology/modules.rdf");

    var actualModules = moduleHandler.getModules();

    var expectedModules = List.of(
        createModule("ModuleA",
            List.of(
                createModule("ModuleA_1",
                    List.of(
                        createModule("ModuleA_1_a", emptyList(), NOT_SET)
                    ),
                    NOT_SET
                ),
                createModule("ModuleA_2", emptyList(), NOT_SET),
                createModule("ModuleA_3", emptyList(), NOT_SET)
            ),
            NOT_SET
        ),
        createModule("ModuleB",
            List.of(
                createModule("ModuleB_1", emptyList(), NOT_SET),
                createModule("ModuleB_2", emptyList(), NOT_SET)
            ),
            NOT_SET
        ),
        createModule("ModuleC", emptyList(), NOT_SET)
    );

    assertEquals(expectedModules, actualModules);
  }

  @Test
  void shouldReturnListOfModulesFromOntologyFiles() {
    var moduleHandler = prepareModuleHandler(
        "/ontology/modules/ModuleA.rdf",
        "/ontology/modules/ModuleA_1.rdf",
        "/ontology/modules/ModuleA_1_a.rdf",
        "/ontology/modules/ModuleA_2.rdf",
        "/ontology/modules/ModuleB.rdf",
        "/ontology/modules/ModuleB_1.rdf",
        "/ontology/modules/ModuleB_2.rdf",
        "/ontology/modules/modules.rdf");

    var actualModules = moduleHandler.getModules();

    var expectedModules = List.of(
        createModuleWithIriAsLabel("ModuleA/",
            List.of(
                createModuleWithIriAsLabel("ModuleA_1/",
                    List.of(
                        createModuleWithIriAsLabel("ModuleA_1_a/", emptyList(), PROVISIONAL)
                    ),
                    PROVISIONAL
                ),
                createModuleWithIriAsLabel("ModuleA_2/", emptyList(), INFORMATIVE)
            ),
            RELEASE
        ),
        createModuleWithIriAsLabel("ModuleB/",
            List.of(
                createModuleWithIriAsLabel("ModuleB_1/", emptyList(), PROVISIONAL),
                createModuleWithIriAsLabel("ModuleB_2/", emptyList(), RELEASE)
            ),
            MIXED
        )
    );

    assertEquals(expectedModules, actualModules);
  }

  @Test
  void shouldReturnListOfModulesWhenNotDefinedInOntology() {
    var moduleHandler = prepareModuleHandler("/ontology/MortgageLoansWithoutImports.rdf");
    var actualModules = moduleHandler.getModules();
    var expectedModules = List.of(
        new OntologyModule("https://spec.edmcouncil.org/fibo/ontology/LOAN/LoanTypes/MortgageLoans/",
            "MortgageLoans",
            emptyList(),
            PROVISIONAL)
    );
    assertEquals(expectedModules, actualModules);
  }

  private ModuleHandler prepareModuleHandler(String... ontologyPaths) {
    var configurationService = new YamlFileBasedConfigurationService(prepareFileSystem());
    configurationService.init();

    var ontologyManager = getOntologyManager(ontologyPaths);
    var configurationData = configurationService.getConfigurationData();
    configurationData.getOntologiesConfig().setAutomaticCreationOfModules(true);

    ontologyManager.setIriToPathMapping(
        Map.of(
            IRI.create("https://spec.edmcouncil.org/fibo/ontology/LOAN/LoanTypes/MortgageLoans/"),
            IRI.create("file://some_random_path.rdf")));
    var labelProvider = new LabelProvider(configurationService, ontologyManager);
    var individualDataHandler = new IndividualDataHandler(labelProvider);

    return new ModuleHandler(ontologyManager,
        individualDataHandler,
        labelProvider,
        configurationService);
  }

  private OntologyManager getOntologyManager(String... ontologyPaths) {
    try {
      var owlOntologyManager = OWLManager.createOWLOntologyManager();
      var umbrellaOntology = owlOntologyManager.createOntology(IRI.create(ROOT_IRI));

      for (String ontologyPath : ontologyPaths) {
        var exampleOntologyPath = getClass().getResourceAsStream(ontologyPath);
        if (exampleOntologyPath == null) {
          throw new IllegalStateException(
              String.format("Example ontology in path '%s' not found.", ontologyPath));
        }
        var ontology = owlOntologyManager.loadOntologyFromOntologyDocument(exampleOntologyPath);
        var importDeclaration = owlOntologyManager
            .getOWLDataFactory()
            .getOWLImportsDeclaration(ontology.getOntologyID().getOntologyIRI().orElseThrow());

        var addImport = new AddImport(umbrellaOntology, importDeclaration);
        umbrellaOntology.applyDirectChange(addImport);
      }

      var ontologyManager = new OntologyManager();
      ontologyManager.updateOntology(umbrellaOntology);
      return ontologyManager;
    } catch (OWLOntologyCreationException ex) {
      throw new IllegalStateException(ex);
    }
  }

  private OntologyModule createModule(String name, List<OntologyModule> subModules, MaturityLevel maturityLevel) {
    return new OntologyModule(ROOT_IRI + name, name, subModules, maturityLevel);
  }

  private OntologyModule createModuleWithIriAsLabel(String name, List<OntologyModule> subModules,
      MaturityLevel maturityLevel) {
    return new OntologyModule(ROOT_IRI + name, ROOT_IRI + name, subModules, maturityLevel);
  }
}
