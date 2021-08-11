import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.loader.ConfigLoader;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.ViewerCoreConfiguration;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.DefaultLabelItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.LabelPriority;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.MissingLanguageItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.StringItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.util.CollectionUtils;

/**
 *
 * @author Patrycja Miazek (patrycja.miazek@makolab.com) 
 */
public class ConfigLoaderTest {

  private ViewerCoreConfiguration testViewerCoreConfig;

  @BeforeAll
  public static void setUpClass() throws Exception {

  }

  @AfterAll
  public static void tearDownClass() throws Exception {
  }

  @AfterEach
  public void tearDown() throws Exception {
  }

  @TempDir
  Path tempDir;

  @BeforeEach
  public void setUp() throws IOException {

    Path pathGroup = null, pathOntology = null, pathSearch = null, pathIgnore = null, pathLabel = null;

    try {
      pathGroup = tempDir.resolve("groups_config.xml");
      pathOntology = tempDir.resolve("ontology_config.xml");
      pathSearch = tempDir.resolve("search_config.xml");
      pathIgnore = tempDir.resolve("ignore_to_display.xml");
      pathLabel = tempDir.resolve("label_config.xml");
    } catch (InvalidPathException ipe) {
      fail("Validation not work correctly");
    }

    Path pathGroupInput = Paths.get("integration_tests", "config", "groups_config.xml");
    Path pathOntologyInput = Paths.get("integration_tests", "config", "ontology_config.xml");
    Path pathSearchInput = Paths.get("integration_tests", "config", "search_config.xml");
    Path pathIgnoreInput = Paths.get("integration_tests", "config", "ignore_to_display.xml");
    Path pathLabelInput = Paths.get("integration_tests", "config", "label_config.xml");

    Files.copy(pathSearchInput, pathSearch);
    Files.copy(pathOntologyInput, pathOntology);
    Files.copy(pathIgnoreInput, pathIgnore);
    Files.copy(pathLabelInput, pathLabel);
    Files.copy(pathGroupInput, pathGroup);

    var configLoader = new ConfigLoader();
    try {
      for (File file : tempDir.toFile().listFiles()) {
        if (file.isFile()) {
          configLoader.loadWeaselConfiguration(file.toPath());
        }
      }
      this.testViewerCoreConfig = configLoader.getConfiguration();

    } catch (Exception e) {
      fail("Validation not work correctly");
    }

  }

  @Test
  public void testConfig() throws Exception {
    var testConfig = this.testViewerCoreConfig;
    Assertions.assertNotNull(testConfig);
  }

  @Test
  public void testLabelFromConfig() throws Exception {
    var testConfig = this.testViewerCoreConfig;
    System.out.println(this.testViewerCoreConfig);
    String testLang = testConfig.getLabelLang();
    String expectedValue = "en";
    Assertions.assertEquals(testLang, expectedValue);
  }

  @Test
  public void testLabelPriorityFromConfig() throws Exception {
    var testConfig = this.testViewerCoreConfig;
    LabelPriority.Priority labelPriority = testConfig.getLabelPriority();
    LabelPriority.Priority expectedValue = LabelPriority.Priority.USER_DEFINED;
    Assertions.assertEquals(labelPriority, expectedValue);
  }

  @Test
  public void testOntologyPathFromConfig() throws Exception {
    var testConfig = this.testViewerCoreConfig;
    Map<String, Set<String>> testOntologyLocation = testConfig.getOntologyLocation();
    // String expectedValue = "integration_tests/ontologies";
    Set<String> expectedValue = Collections.singleton("integration_tests/ontologies");
    Assertions.assertTrue(testOntologyLocation.keySet().contains(ConfigKeys.ONTOLOGY_DIR));
    Assertions.assertFalse(testOntologyLocation.keySet().contains(ConfigKeys.ONTOLOGY_PATH));
    Assertions.assertFalse(testOntologyLocation.keySet().contains(ConfigKeys.ONTOLOGY_URL));
    Assertions.assertEquals(testOntologyLocation.get(ConfigKeys.ONTOLOGY_DIR), expectedValue);
  }

  @Test
  public void testUseLabelsFromConfig() throws Exception {
    var testConfig = this.testViewerCoreConfig;
    boolean testUseLabel = testConfig.useLabels();
    Assertions.assertTrue(testUseLabel);
  }

  @Test
  public void testForceLabelLangFromConfig() throws Exception {
    var testConfig = this.testViewerCoreConfig;
    boolean forceLabelLang = testConfig.isForceLabelLang();
    Assertions.assertFalse(forceLabelLang);
  }

  @Test
  public void testIsUriIriFromConfig() throws Exception {
    var testConfig = this.testViewerCoreConfig;
    boolean isUriIri = testConfig.isUriIri(ConfigKeys.URI_NAMESPACE);
    Assertions.assertFalse(isUriIri);
  }

  @Test
  public void testgetDefaultLabelsFromConfig() throws Exception {

    var testConfig = this.testViewerCoreConfig;

    Set<DefaultLabelItem> getDefaultLabels = testConfig.getDefaultLabels();
    Set<DefaultLabelItem> expectedValue = new HashSet<>();

    expectedValue.add(new DefaultLabelItem("http://www.w3.org/2000/01/rdf-schema#Literal", "literal"));
    expectedValue.add(new DefaultLabelItem("http://www.w3.org/2001/XMLSchema#string", "string"));
    expectedValue.add(new DefaultLabelItem("http://www.omg.org/techprocess/ab/SpecificationMetadata/fileAbbreviation", "file abbreviation"));
    expectedValue.add(new DefaultLabelItem("http://www.omg.org/techprocess/ab/SpecificationMetadata/filename", "file name"));
    expectedValue.add(new DefaultLabelItem("@viewer.axiom.EquivalentClasses", "Equivalent classes (necessary and sufficient criteria)"));
    expectedValue.add(new DefaultLabelItem("@viewer.axiom.DataPropertyAssertion", "Data property assertion"));
    expectedValue.add(new DefaultLabelItem("@viewer.axiom.DataPropertyRange", "Data property range"));
    expectedValue.add(new DefaultLabelItem("http://www.w3.org/2004/02/skos/core#definition", "definition"));
    expectedValue.add(new DefaultLabelItem("@viewer.external.annotationProperty", "external annotation property"));

    CollectionUtils.containsAny(getDefaultLabels, expectedValue);
  }

  @Test
  public void testIgnoredElementsFromConfig() throws Exception {
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

    Assertions.assertEquals(ignoredElements, expectedValue);
  }

  @Test
  public void testMissingLanguageActionFromConfig() throws Exception {
    var testConfig = this.testViewerCoreConfig;
    MissingLanguageItem.Action missingLanguageAction = testConfig.getMissingLanguageAction();
    MissingLanguageItem.Action expectedValue = MissingLanguageItem.Action.FIRST;
    Assertions.assertEquals(missingLanguageAction, expectedValue);
  }

  @Test
  public void testGetConfigValFromConfig() throws Exception {
    var testConfig = this.testViewerCoreConfig;
    Set<ConfigItem> getConfigVal = testConfig.getConfigVal(ConfigKeys.ONTOLOGY_DIR);
    Set<ConfigItem> expectedValue = Collections.singleton(new StringItem("integration_tests/ontologies"));
    Assertions.assertEquals(getConfigVal, expectedValue);

    Set<ConfigItem> getConfigValPath = testConfig.getConfigVal(ConfigKeys.ONTOLOGY_PATH);
    Assertions.assertNull(getConfigValPath);

    Set<ConfigItem> getConfigValUrl = testConfig.getConfigVal(ConfigKeys.ONTOLOGY_URL);
    Assertions.assertNull(getConfigValUrl);
  }
}
