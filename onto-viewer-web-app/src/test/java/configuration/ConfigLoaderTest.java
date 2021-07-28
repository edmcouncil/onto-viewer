package configuration;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.loader.ConfigLoader;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.loader.saxparser.ViewerCoreConfigurationHandler;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.ViewerCoreConfiguration;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.DefaultLabelItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.LabelPriority;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.MissingLanguageItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.StringItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.properties.AppProperties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class ConfigLoaderTest {

  private ViewerCoreConfiguration testViewerCoreConfig;

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {

    var configLoader = new ConfigLoader();
    try {

      Path path = Paths.get("integration_tests", "config");
      System.out.println(path.toAbsolutePath());
      for (File file : path.toFile().listFiles()) {
        if (file.isFile()) {
          configLoader.loadWeaselConfiguration(file.toPath());
        }
      }
      this.testViewerCoreConfig = configLoader.getConfiguration();

    } catch (Exception e) {
      fail("Validation not work correctly");
    }
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testConfig() throws Exception {
    var testConfig = this.getConfig();
    Assertions.assertNotNull(testConfig);

  }

  @Test
  public void testLabelFromConfig() throws Exception {
 var testConfig = this.getConfig();
    System.out.println(this.testViewerCoreConfig);
    String testLang = testConfig.getLabelLang();
    String expectedValue = "en";
    Assertions.assertEquals(testLang, expectedValue);

  }

  @Test
  public void testLabelPriorityFromConfig() throws Exception {
    var testConfig = this.getConfig();
    LabelPriority.Priority labelPriority = testConfig.getLabelPriority();
    LabelPriority.Priority expectedValue = LabelPriority.Priority.USER_DEFINED;
    Assertions.assertEquals(labelPriority, expectedValue);

  }

  @Test
  public void testOntologyPathFromConfig() throws Exception {
    var testConfig = this.getConfig();
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
    var testConfig = this.getConfig();
    boolean testUseLabel = testConfig.useLabels();
    Assertions.assertTrue(testUseLabel);
  }

  @Test
  public void testForceLabelLangFromConfig() throws Exception {
    var testConfig = this.getConfig();
    boolean forceLabelLang = testConfig.isForceLabelLang();
    Assertions.assertFalse(forceLabelLang);
  }

  @Test
  public void testIsUriIriFromConfig() throws Exception {
    var testConfig = this.getConfig();
    boolean isUriIri = testConfig.isUriIri(ConfigKeys.URI_NAMESPACE);
    Assertions.assertFalse(isUriIri);
  }

  @Test
  public void testIgnoredElementsFromConfig() throws Exception {
    var testConfig = this.getConfig();
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
    var testConfig = this.getConfig();
    MissingLanguageItem.Action missingLanguageAction = testConfig.getMissingLanguageAction();
    MissingLanguageItem.Action expectedValue = MissingLanguageItem.Action.FIRST;
    Assertions.assertEquals(missingLanguageAction, expectedValue);

  }

  @Test
  public void testGetConfigValFromConfig() throws Exception {
    var testConfig = this.getConfig();
    Set<ConfigItem> getConfigVal = testConfig.getConfigVal(ConfigKeys.ONTOLOGY_DIR);
    Set<ConfigItem> expectedValue = Collections.singleton(new StringItem("integration_tests/ontologies"));
    Assertions.assertEquals(getConfigVal, expectedValue);

    Set<ConfigItem> getConfigValPath = testConfig.getConfigVal(ConfigKeys.ONTOLOGY_PATH);
    Assertions.assertNull(getConfigValPath);

    Set<ConfigItem> getConfigValUrl = testConfig.getConfigVal(ConfigKeys.ONTOLOGY_URL);
    Assertions.assertNull(getConfigValUrl);
  }

  private ViewerCoreConfiguration getConfig() {
    var configLoader = new ConfigLoader();
    try {

      Path path = Paths.get("integration_tests", "config");
      for (File file : path.toFile().listFiles()) {
        if (file.isFile()) {
          configLoader.loadWeaselConfiguration(file.toPath());
        }
      }
      return configLoader.getConfiguration();

    } catch (Exception e) {
      fail("Validation not work correctly");
    }
    return null;

  }
};
