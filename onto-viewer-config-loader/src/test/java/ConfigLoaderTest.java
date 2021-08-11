import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.loader.ConfigLoader;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.ViewerCoreConfiguration;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.DefaultLabelItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.LabelPriority;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.MissingLanguageItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.StringItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.CollectionUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Patrycja Miazek (patrycja.miazek@makolab.com) 
 */
class ConfigLoaderTest {

  private ViewerCoreConfiguration testViewerCoreConfig;

  @TempDir
  Path tempDir;

  @BeforeEach
  public void setUp() throws IOException, URISyntaxException {
    var configFileNames = Set.of(
        "groups_config.xml",
        "ontology_config.xml",
        "search_config.xml",
        "ignore_to_display.xml",
        "label_config.xml");

    for (String configFileName : configFileNames) {
      prepareConfigFiles(configFileName);
    }

    var configLoader = new ConfigLoader();
    try {
      Files.list(tempDir)
          .filter(Files::isRegularFile)
          .forEach(configLoader::loadWeaselConfiguration);

      this.testViewerCoreConfig = configLoader.getConfiguration();
    } catch (Exception e) {
      fail("Validation not work correctly");
    }
  }

  @Test
  void testConfig() {
    assertNotNull(testViewerCoreConfig);
  }

  @Test
  void testLangLabelFromConfig() {
    String expectedResult = "en";

    String actualResult = testViewerCoreConfig.getLabelLang();

    assertEquals(expectedResult, actualResult);
  }

  @Test
  void testLabelPriorityFromConfig() throws Exception {
    var testConfig = this.testViewerCoreConfig;
    LabelPriority.Priority labelPriority = testConfig.getLabelPriority();
    LabelPriority.Priority expectedValue = LabelPriority.Priority.USER_DEFINED;
    assertEquals(labelPriority, expectedValue);
  }

  @Test
  void testOntologyPathFromConfig() throws Exception {
    var testConfig = this.testViewerCoreConfig;
    Map<String, Set<String>> testOntologyLocation = testConfig.getOntologyLocation();
    Set<String> expectedValue = Collections.singleton("integration_tests/ontologies");

    assertTrue(testOntologyLocation.containsKey(ConfigKeys.ONTOLOGY_DIR));
    assertFalse(testOntologyLocation.containsKey(ConfigKeys.ONTOLOGY_PATH));
    assertFalse(testOntologyLocation.containsKey(ConfigKeys.ONTOLOGY_URL));
    assertEquals(testOntologyLocation.get(ConfigKeys.ONTOLOGY_DIR), expectedValue);
  }

  @Test
  void testUseLabelsFromConfig() throws Exception {
    var testConfig = this.testViewerCoreConfig;
    boolean testUseLabel = testConfig.useLabels();
    assertTrue(testUseLabel);
  }

  @Test
  void testForceLabelLangFromConfig() throws Exception {
    var testConfig = this.testViewerCoreConfig;
    boolean forceLabelLang = testConfig.isForceLabelLang();
    assertFalse(forceLabelLang);
  }

  @Test
  void testIsUriIriFromConfig() throws Exception {
    var testConfig = this.testViewerCoreConfig;
    boolean isUriIri = testConfig.isUriIri(ConfigKeys.URI_NAMESPACE);
    assertFalse(isUriIri);
  }

  @Test
  void testGetDefaultLabelsFromConfig() throws Exception {

    var testConfig = this.testViewerCoreConfig;

    Set<DefaultLabelItem> actualResult = testConfig.getDefaultLabels();
    Set<DefaultLabelItem> expectedResult = new HashSet<>();

    expectedResult.add(new DefaultLabelItem("http://www.w3.org/2000/01/rdf-schema#Literal", "literal"));
    expectedResult.add(new DefaultLabelItem("http://www.w3.org/2001/XMLSchema#string", "string"));
    expectedResult.add(new DefaultLabelItem("http://www.omg.org/techprocess/ab/SpecificationMetadata/fileAbbreviation", "file abbreviation"));
    expectedResult.add(new DefaultLabelItem("http://www.omg.org/techprocess/ab/SpecificationMetadata/filename", "file name"));
    expectedResult.add(new DefaultLabelItem("@viewer.axiom.EquivalentClasses", "Equivalent classes (necessary and sufficient criteria)"));
    expectedResult.add(new DefaultLabelItem("@viewer.axiom.DataPropertyAssertion", "Data property assertion"));
    expectedResult.add(new DefaultLabelItem("@viewer.axiom.DataPropertyRange", "Data property range"));
    expectedResult.add(new DefaultLabelItem("http://www.w3.org/2004/02/skos/core#definition", "definition"));
    expectedResult.add(new DefaultLabelItem("@viewer.external.annotationProperty", "external annotation property"));

    CollectionUtils.containsAny(actualResult, expectedResult);
  }

  @Test
  void testIgnoredElementsFromConfig() throws Exception {
    var testConfig = this.testViewerCoreConfig;
    Set<String> ignoredElements = testConfig.getIgnoredElements();
    Set<String> expectedValue = new HashSet<>();
    expectedValue.add("@viewer.axiom.SubObjectPropertyOf");
    expectedValue.add("http://www.w3.org/2000/01/rdf-schema#isDefinedBy");
    expectedValue.add("http://spec.edmcouncil.org/owlnames#definition");
    expectedValue.add("http://spec.edmcouncil.org/owlnames#label");
    expectedValue.add("http://spec.edmcouncil.org/owlnames#synonym");
    expectedValue.add("http://spec.edmcouncil.org/owlnames#example");
    expectedValue.add("http://spec.edmcouncil.org/owlnames#explanatoryNote");

    assertEquals(ignoredElements, expectedValue);
  }

  @Test
  void testMissingLanguageActionFromConfig() throws Exception {
    var testConfig = this.testViewerCoreConfig;
    MissingLanguageItem.Action missingLanguageAction = testConfig.getMissingLanguageAction();
    MissingLanguageItem.Action expectedValue = MissingLanguageItem.Action.FIRST;
    assertEquals(missingLanguageAction, expectedValue);
  }

  @Test
  void testGetConfigValFromConfig() throws Exception {
    var testConfig = this.testViewerCoreConfig;
    Set<ConfigItem> getConfigVal = testConfig.getConfigVal(ConfigKeys.ONTOLOGY_DIR);
    Set<ConfigItem> expectedValue = Collections.singleton(new StringItem("integration_tests/ontologies"));
    assertEquals(getConfigVal, expectedValue);

    Set<ConfigItem> getConfigValPath = testConfig.getConfigVal(ConfigKeys.ONTOLOGY_PATH);
    assertNull(getConfigValPath);

    Set<ConfigItem> getConfigValUrl = testConfig.getConfigVal(ConfigKeys.ONTOLOGY_URL);
    assertNull(getConfigValUrl);
  }

  private void prepareConfigFiles(String configFileName) throws IOException, URISyntaxException {
    var configLocationPath = getClass().getResource("/integration_tests/config/");
    if (configLocationPath == null) {
      fail("Unable to find config test files.");
    }
    var configLocation = Path.of(configLocationPath.getPath());
    Files.copy(
        configLocation.resolve(configFileName),
        tempDir.resolve(configFileName));
  }
}
