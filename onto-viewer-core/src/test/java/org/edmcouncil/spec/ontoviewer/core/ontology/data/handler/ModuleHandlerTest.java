package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.Pair;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.YamlFileBasedConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.model.module.OntologyModule;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.BaseTest;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.individual.IndividualDataHelper;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevel;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevelFactory;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.module.ModuleHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

class ModuleHandlerTest extends BaseTest {

  private static final String ROOT_IRI = "http://www.example.com/modules/";

  private static final String ANNOTATION_PREFIX =
      "https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/";
  private static final String PROVISIONAL = "Provisional";
  private static final MaturityLevel PROVISIONAL_ML = new MaturityLevel(PROVISIONAL, ANNOTATION_PREFIX + PROVISIONAL);
  private static final String RELEASE = "Release";
  private static final MaturityLevel RELEASE_ML = new MaturityLevel(RELEASE, ANNOTATION_PREFIX + RELEASE);
  private static final String INFORMATIVE = "Informative";
  private static final MaturityLevel INFORMATIVE_ML = new MaturityLevel(INFORMATIVE, ANNOTATION_PREFIX + INFORMATIVE);

  @Test
  void shouldReturnListOfModulesDefinedInOntologyWithoutMaturityLevel() {
    var moduleHandler = prepareModuleHandler("/ontology/modules.rdf");

    var actualModules = moduleHandler.getModules();

    var expectedModules = List.of(
        createModule("ModuleA",
            List.of(
                createModule("ModuleA_1",
                    List.of(
                        createModule("ModuleA_1_a", emptyList(), MaturityLevelFactory.NOT_SET)
                    ),
                    MaturityLevelFactory.NOT_SET
                ),
                createModule("ModuleA_2", emptyList(), MaturityLevelFactory.NOT_SET),
                createModule("ModuleA_3", emptyList(), MaturityLevelFactory.NOT_SET)
            ),
            MaturityLevelFactory.NOT_SET
        ),
        createModule("ModuleB",
            List.of(
                createModule("ModuleB_1", emptyList(), MaturityLevelFactory.NOT_SET),
                createModule("ModuleB_2", emptyList(), MaturityLevelFactory.NOT_SET)
            ),
            MaturityLevelFactory.NOT_SET
        ),
        createModule("ModuleC", emptyList(), MaturityLevelFactory.NOT_SET)
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
    var configurationService = new YamlFileBasedConfigurationService(prepareFileSystem());
    configurationService.init();
    var expectedModules = List.of(
        createModuleWithIriAsLabel("ModuleA/",
            List.of(
                createModuleWithIriAsLabel("ModuleA_1/",
                    List.of(
                        createModuleWithIriAsLabel("ModuleA_1_a/", emptyList(), PROVISIONAL_ML)
                    ),
                    PROVISIONAL_ML
                ),
                createModuleWithIriAsLabel("ModuleA_2/", emptyList(), INFORMATIVE_ML)
            ),
            RELEASE_ML
        ),
        createModuleWithIriAsLabel("ModuleB/",
            List.of(
                createModuleWithIriAsLabel("ModuleB_1/", emptyList(), PROVISIONAL_ML),
                createModuleWithIriAsLabel("ModuleB_2/", emptyList(), RELEASE_ML)
            ),
            MaturityLevelFactory.MIXED
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
            PROVISIONAL_ML)
    );
    assertEquals(expectedModules, actualModules);
  }

  @Test
  void shouldReturnEntityMaturityLevelForEntityWhenExplicitlyStated() {
    var moduleHandler = prepareModuleHandler("/ontology/maturity_level.rdf");
    IRI entityIri = IRI.create(ROOT_IRI + "ClassA");

    MaturityLevel actualMaturityLevel = moduleHandler.getMaturityLevelForEntity(entityIri);

    assertEquals(RELEASE_ML, actualMaturityLevel);
  }

  @Test
  void shouldReturnEntityMaturityLevelForEntityFromOntology() {
    var moduleHandler = prepareModuleHandler("/ontology/maturity_level.rdf");
    IRI entityIri = IRI.create(ROOT_IRI + "ClassB");

    MaturityLevel actualMaturityLevel = moduleHandler.getMaturityLevelForEntity(entityIri);

    assertEquals(INFORMATIVE_ML, actualMaturityLevel);
  }

  private ModuleHandler prepareModuleHandler(String... ontologyPaths) {
    var configurationService = new YamlFileBasedConfigurationService(prepareFileSystem());
    configurationService.init();

    var ontologyManager = getOntologyManager(ontologyPaths);
    var configurationData = configurationService.getConfigurationData();
    configurationData.getOntologiesConfig().setAutomaticCreationOfModules(true);

    var configurationMaturity = configurationService.getConfigurationData();
    List<Pair> definition = new ArrayList<>();
    definition.add(new Pair(RELEASE, RELEASE_ML.getIri()));
    definition.add(new Pair(PROVISIONAL, PROVISIONAL_ML.getIri()));
    definition.add(new Pair(INFORMATIVE, INFORMATIVE_ML.getIri()));

    configurationMaturity.getOntologiesConfig().setMaturityLevelDefinition(definition);

    ontologyManager.setIriToPathMapping(
        Map.of(
            IRI.create("https://spec.edmcouncil.org/fibo/ontology/LOAN/LoanTypes/MortgageLoans/"),
            IRI.create("file://some_random_path.rdf")));
    var labelProvider = new LabelProvider(configurationService, ontologyManager);
    var individualDataHelper = new IndividualDataHelper(labelProvider);
    var maturityLevelFactory = new MaturityLevelFactory(configurationService);
    var maturityLevelHandler = new MaturityLevelHandler(configurationService, ontologyManager, maturityLevelFactory);
    var moduleHandler = new ModuleHandler(configurationService, ontologyManager, individualDataHelper, labelProvider,
        maturityLevelFactory, maturityLevelHandler);
    moduleHandler.refreshModulesHandlerData();
    return moduleHandler;
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
