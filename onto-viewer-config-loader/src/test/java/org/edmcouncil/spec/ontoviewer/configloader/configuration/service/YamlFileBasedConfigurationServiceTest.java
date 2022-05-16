package org.edmcouncil.spec.ontoviewer.configloader.configuration.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.LabelPriority;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.MissingLanguageAction;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.properties.AppProperties;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class YamlFileBasedConfigurationServiceTest {

  public static final String CONFIG_DIR = "config";

  @TempDir
  Path homeDir;

  @Test
  void shouldHaveDefaultConfigIfCustomConfigWasNotProvided() {
    var fileSystemManager = prepareFileSystem();

    var yamlConfigurationService = new YamlFileBasedConfigurationService(fileSystemManager);
    yamlConfigurationService.init();
    var configurationData = yamlConfigurationService.getConfigurationData();

    // Groups Config
    assertEquals(2, configurationData.getGroupsConfig().getPriorityList().size());
    assertEquals(5, configurationData.getGroupsConfig().getGroups().size());

    // Label Config
    assertTrue(configurationData.getLabelConfig().isDisplayLabel());
    assertEquals(LabelPriority.USER_DEFINED, configurationData.getLabelConfig().getLabelPriority());
    assertFalse(configurationData.getLabelConfig().isForceLabelLang());
    assertEquals("en", configurationData.getLabelConfig().getLabelLang());
    assertEquals(MissingLanguageAction.FIRST, configurationData.getLabelConfig().getMissingLanguageAction());
    assertEquals(44, configurationData.getLabelConfig().getDefaultNames().size());

    // Ontology Config
    assertEquals("ontologies", configurationData.getOntologiesConfig().getPaths().get(0));

    // Search Config
    assertEquals(2, configurationData.getSearchConfig().getSearchDescriptions().size());
    assertEquals(3, configurationData.getSearchConfig().getFuzzyDistance());
    assertTrue(configurationData.getSearchConfig().isReindexOnStart());
    assertEquals(6, configurationData.getSearchConfig().getFindProperties().size());
  }

  @Test
  void shouldHaveConfigMixedBothFromDefaultsAndCustomGroupsConfig() {
    var fileSystemManager = prepareFileSystem();
    prepareTestConfiguration("/configuration_yaml/groups_config1.yaml");

    var yamlConfigurationService = new YamlFileBasedConfigurationService(fileSystemManager);
    yamlConfigurationService.init();
    var configurationData = yamlConfigurationService.getConfigurationData();

    // Groups Config
    assertEquals(4, configurationData.getGroupsConfig().getPriorityList().size());
    assertEquals("http://www.w3.org/2000/01/rdf-schema#somethingElse2",
        configurationData.getGroupsConfig().getPriorityList().get(3));
    assertEquals(4, configurationData.getGroupsConfig().getGroups().size());
    assertEquals("http://www.w3.org/2000/01/rdf-schema#description",
        configurationData.getGroupsConfig().getGroups().get("Glossary").get(1));

    // Label Config
    assertTrue(configurationData.getLabelConfig().isDisplayLabel());
    assertEquals(LabelPriority.USER_DEFINED, configurationData.getLabelConfig().getLabelPriority());
    assertFalse(configurationData.getLabelConfig().isForceLabelLang());
    assertEquals("en", configurationData.getLabelConfig().getLabelLang());
    assertEquals(MissingLanguageAction.FIRST, configurationData.getLabelConfig().getMissingLanguageAction());
    assertEquals(44, configurationData.getLabelConfig().getDefaultNames().size());

    // Ontology Config
    assertEquals("ontologies", configurationData.getOntologiesConfig().getPaths().get(0));

    // Search Config
    assertEquals(2, configurationData.getSearchConfig().getSearchDescriptions().size());
    assertEquals(3, configurationData.getSearchConfig().getFuzzyDistance());
    assertTrue(configurationData.getSearchConfig().isReindexOnStart());
    assertEquals(6, configurationData.getSearchConfig().getFindProperties().size());
  }

  @Test
  void shouldHaveConfigMixedBothFromDefaultsAndCustomLabelConfig() {
    var fileSystemManager = prepareFileSystem();
    prepareTestConfiguration("/configuration_yaml/label_config1.yaml");

    var yamlConfigurationService = new YamlFileBasedConfigurationService(fileSystemManager);
    yamlConfigurationService.init();
    var configurationData = yamlConfigurationService.getConfigurationData();

    // Groups Config
    assertEquals(2, configurationData.getGroupsConfig().getPriorityList().size());
    assertEquals(5, configurationData.getGroupsConfig().getGroups().size());

    // Label Config
    assertFalse(configurationData.getLabelConfig().isDisplayLabel());
    assertEquals(LabelPriority.USER_DEFINED, configurationData.getLabelConfig().getLabelPriority());
    assertTrue(configurationData.getLabelConfig().isForceLabelLang());
    assertEquals("pl", configurationData.getLabelConfig().getLabelLang());
    assertEquals(MissingLanguageAction.FIRST, configurationData.getLabelConfig().getMissingLanguageAction());
    assertEquals(1, configurationData.getLabelConfig().getDefaultNames().size());

    // Ontology Config
    assertEquals("ontologies", configurationData.getOntologiesConfig().getPaths().get(0));

    // Search Config
    assertEquals(2, configurationData.getSearchConfig().getSearchDescriptions().size());
    assertEquals(3, configurationData.getSearchConfig().getFuzzyDistance());
    assertTrue(configurationData.getSearchConfig().isReindexOnStart());
    assertEquals(6, configurationData.getSearchConfig().getFindProperties().size());
  }

  @Test
  void shouldHaveConfigMixedBothFromDefaultsAndCustomOntologiesConfig() {
    var fileSystemManager = prepareFileSystem();
    prepareTestConfiguration("/configuration_yaml/ontologies_config1.yaml");

    var yamlConfigurationService = new YamlFileBasedConfigurationService(fileSystemManager);
    yamlConfigurationService.init();
    var configurationData = yamlConfigurationService.getConfigurationData();

    // Groups Config
    assertEquals(2, configurationData.getGroupsConfig().getPriorityList().size());
    assertEquals(5, configurationData.getGroupsConfig().getGroups().size());

    // Label Config
    assertTrue(configurationData.getLabelConfig().isDisplayLabel());
    assertEquals(LabelPriority.USER_DEFINED, configurationData.getLabelConfig().getLabelPriority());
    assertFalse(configurationData.getLabelConfig().isForceLabelLang());
    assertEquals("en", configurationData.getLabelConfig().getLabelLang());
    assertEquals(MissingLanguageAction.FIRST, configurationData.getLabelConfig().getMissingLanguageAction());
    assertEquals(44, configurationData.getLabelConfig().getDefaultNames().size());

    // Ontology Config
    var ontologiesConfig = configurationData.getOntologiesConfig();
    assertEquals("foo/bar/my_dir", ontologiesConfig.getPaths().get(0));
    assertEquals("foo/my_ontology.rdf", ontologiesConfig.getPaths().get(1));
    assertEquals("http://example.com", ontologiesConfig.getUrls().get(0));
    assertEquals("ontologies/catalog-v001.xml", ontologiesConfig.getCatalogPaths().get(0));
    assertEquals("^(About|Metadata).*", ontologiesConfig.getModuleIgnorePatterns().get(0));
    assertEquals("http://example.com/ontology", ontologiesConfig.getModuleToIgnore().get(0));

    // Search Config
    assertEquals(2, configurationData.getSearchConfig().getSearchDescriptions().size());
    assertEquals(3, configurationData.getSearchConfig().getFuzzyDistance());
    assertTrue(configurationData.getSearchConfig().isReindexOnStart());
    assertEquals(6, configurationData.getSearchConfig().getFindProperties().size());
  }

  @Test
  void shouldHaveConfigMixedBothFromDefaultsAndCustomSearchConfig() {
    var fileSystemManager = prepareFileSystem();
    prepareTestConfiguration("/configuration_yaml/search_config1.yaml");

    var yamlConfigurationService = new YamlFileBasedConfigurationService(fileSystemManager);
    yamlConfigurationService.init();
    var configurationData = yamlConfigurationService.getConfigurationData();

    // Groups Config
    assertEquals(2, configurationData.getGroupsConfig().getPriorityList().size());
    assertEquals(5, configurationData.getGroupsConfig().getGroups().size());

    // Label Config
    assertTrue(configurationData.getLabelConfig().isDisplayLabel());
    assertEquals(LabelPriority.USER_DEFINED, configurationData.getLabelConfig().getLabelPriority());
    assertFalse(configurationData.getLabelConfig().isForceLabelLang());
    assertEquals("en", configurationData.getLabelConfig().getLabelLang());
    assertEquals(MissingLanguageAction.FIRST, configurationData.getLabelConfig().getMissingLanguageAction());
    assertEquals(44, configurationData.getLabelConfig().getDefaultNames().size());

    // Ontology Config
    assertEquals("ontologies", configurationData.getOntologiesConfig().getPaths().get(0));

    // Search Config
    var searchConfig = configurationData.getSearchConfig();
    assertEquals(3, searchConfig.getSearchDescriptions().size());
    assertEquals("http://purl.org/dc/terms/concrete", searchConfig.getSearchDescriptions().get(2));
    assertEquals(5, searchConfig.getFuzzyDistance());
    assertFalse(searchConfig.isReindexOnStart());
    assertEquals(2, searchConfig.getFindProperties().size());
  }

  private FileSystemManager prepareFileSystem() {
    Path configDir = homeDir.resolve(CONFIG_DIR).toAbsolutePath();
    try {
      Files.createDirectory(configDir);
    } catch (IOException ex) {
      throw new IllegalStateException("Unable to create test config dir.", ex);
    }

    var appProperties = new AppProperties();
    appProperties.setConfigPath(configDir.toString());
    return new FileSystemManager(appProperties);
  }

  private void prepareTestConfiguration(String configPath) {
    try (InputStream configPathStream = getClass().getResourceAsStream(configPath)) {
      if (configPathStream != null) {
        var configContent = IOUtils.toString(configPathStream, StandardCharsets.UTF_8);
        var configOutputPath = homeDir.resolve(CONFIG_DIR).resolve("test_config.yaml");
        Files.write(configOutputPath, List.of(configContent), StandardCharsets.UTF_8);
      } else {
        throw new IllegalStateException(String.format("Config path '%s' returns null.", configPath));
      }
    } catch (IOException ex) {
      throw new IllegalStateException(
          String.format("Exception thrown while preparing test configuration files (%s).", configPath),
          ex);
    }
  }
}