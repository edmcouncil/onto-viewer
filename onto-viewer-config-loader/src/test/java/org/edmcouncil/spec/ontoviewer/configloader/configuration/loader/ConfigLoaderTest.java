package org.edmcouncil.spec.ontoviewer.configloader.configuration.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.CoreConfiguration;
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
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
class ConfigLoaderTest {

  private CoreConfiguration testViewerCoreConfig;

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
  void testLabelPriorityFromConfig() {
    LabelPriority.Priority actualResult = this.testViewerCoreConfig.getLabelPriority();
    LabelPriority.Priority expectedResult = LabelPriority.Priority.USER_DEFINED;
    assertEquals(expectedResult, actualResult);
  }

  @Test
  void testOntologyPathFromConfig() {
    Map<String, Set<String>> testOntologyLocation = this.testViewerCoreConfig.getOntologyLocation();
    Set<String> expectedResult = Collections.singleton("integration_tests/ontologies");

    assertTrue(testOntologyLocation.containsKey(ConfigKeys.ONTOLOGY_DIR));
    assertFalse(testOntologyLocation.containsKey(ConfigKeys.ONTOLOGY_PATH));
    assertFalse(testOntologyLocation.containsKey(ConfigKeys.ONTOLOGY_URL));
    assertEquals(testOntologyLocation.get(ConfigKeys.ONTOLOGY_DIR), expectedResult);
  }

  @Test
  void testUseLabelsFromConfig() {
    boolean testUseLabel = this.testViewerCoreConfig.useLabels();
    assertTrue(testUseLabel);
  }

  @Test
  void testForceLabelLangFromConfig() {
    boolean forceLabelLang = this.testViewerCoreConfig.isForceLabelLang();
    assertFalse(forceLabelLang);
  }

  @Test
  void testIsUriIriFromConfig() {
    boolean isUriIri = this.testViewerCoreConfig.isUriIri(ConfigKeys.URI_NAMESPACE);
    assertFalse(isUriIri);
  }

  @Test
  void testGetDefaultLabelsFromConfig() {
    Set<DefaultLabelItem> actualResult = this.testViewerCoreConfig.getDefaultLabels();
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

    assertTrue(CollectionUtils.containsAny(expectedResult, actualResult));
  }

  @Test
  void testIgnoredElementsFromConfig() {
    Set<String> actualResult = this.testViewerCoreConfig.getIgnoredElements();
    Set<String> expectedResult = new HashSet<>();
    expectedResult.add("@viewer.axiom.SubObjectPropertyOf");
    expectedResult.add("http://www.w3.org/2000/01/rdf-schema#isDefinedBy");
    expectedResult.add("http://spec.edmcouncil.org/owlnames#definition");
    expectedResult.add("http://spec.edmcouncil.org/owlnames#label");
    expectedResult.add("http://spec.edmcouncil.org/owlnames#synonym");
    expectedResult.add("http://spec.edmcouncil.org/owlnames#example");
    expectedResult.add("http://spec.edmcouncil.org/owlnames#explanatoryNote");

    assertEquals(expectedResult, actualResult);
  }

  @Test
  void testMissingLanguageActionFromConfig() {
    MissingLanguageItem.Action actualResult = this.testViewerCoreConfig.getMissingLanguageAction();
    MissingLanguageItem.Action expectedResult = MissingLanguageItem.Action.FIRST;
    assertEquals(expectedResult, actualResult);
  }

  @Test
  void testGetConfigValFromConfig() {
    Set<ConfigItem> actualResult = this.testViewerCoreConfig.getValue(ConfigKeys.ONTOLOGY_DIR);
    Set<ConfigItem> expectedResult = Collections.singleton(new StringItem("integration_tests/ontologies"));
    assertEquals(expectedResult, actualResult);

    Set<ConfigItem> getConfigValPath = this.testViewerCoreConfig.getValue(ConfigKeys.ONTOLOGY_PATH);
    assertNull(getConfigValPath);

    Set<ConfigItem> getConfigValUrl = this.testViewerCoreConfig.getValue(ConfigKeys.ONTOLOGY_URL);
    assertNull(getConfigValUrl);
  }

  private void prepareConfigFiles(String configFileName) throws IOException, URISyntaxException {
    var configLocationPath = getClass().getResource("/integration_tests/config/");
    if (configLocationPath == null) {
      fail("Unable to find config test files.");
    }
    var configLocation = Path.of(configLocationPath.toURI());
    Files.copy(
        configLocation.resolve(configFileName),
        tempDir.resolve(configFileName));
  }
}
