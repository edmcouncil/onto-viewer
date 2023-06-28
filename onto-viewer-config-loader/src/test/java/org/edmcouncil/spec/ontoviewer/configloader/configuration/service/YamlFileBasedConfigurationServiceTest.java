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
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class YamlFileBasedConfigurationServiceTest {

  public static final String CONFIG_DIR = "config";

  @TempDir
  Path homeDir;

  @Test
  void shouldHaveDefaultConfigIfCustomConfigWasNotProvided() throws IOException {
    var fileSystemManager = prepareFileSystem();

    var yamlConfigurationService = new YamlFileBasedConfigurationService(fileSystemManager);
    yamlConfigurationService.init();
    var configurationData = yamlConfigurationService.getConfigurationData();

    // Groups Config
    assertEquals(5, configurationData.getGroupsConfig().getGroups().size());

    // Label Config
    assertTrue(configurationData.getLabelConfig().isDisplayLabel());
    assertEquals(LabelPriority.USER_DEFINED, configurationData.getLabelConfig().getLabelPriority());
    assertFalse(configurationData.getLabelConfig().isForceLabelLang());
    assertEquals("en", configurationData.getLabelConfig().getLabelLang());
    assertEquals(MissingLanguageAction.FIRST, configurationData.getLabelConfig().getMissingLanguageAction());
    assertEquals(45, configurationData.getLabelConfig().getDefaultNames().size());

    // Ontology Config
    assertEquals("ontologies", configurationData.getOntologiesConfig().getPaths().get(0));

    // Search Config
    assertEquals(2, configurationData.getSearchConfig().getSearchDescriptions().size());
    assertEquals(3, configurationData.getSearchConfig().getFuzzyDistance());
    assertTrue(configurationData.getSearchConfig().isReindexOnStart());
    assertEquals(6, configurationData.getSearchConfig().getFindProperties().size());
    
    // Application Config
    assertFalse(configurationData.getApplicationConfig().isDisplayCopyright());
    assertFalse(configurationData.getApplicationConfig().isDisplayLicense());
    assertEquals("http://purl.org/dc/terms/license", configurationData.getApplicationConfig().getLicense().get(0));
    assertEquals("http://www.omg.org/techprocess/ab/SpecificationMetadata/copyright", configurationData.getApplicationConfig().getCopyright().get(0));
  }

  @Test
  void shouldHaveConfigMixedBothFromDefaultsAndCustomGroupsConfig() throws IOException {
    var fileSystemManager = prepareFileSystem();
    prepareTestConfiguration("/configuration_yaml/groups_config1.yaml");

    var yamlConfigurationService = new YamlFileBasedConfigurationService(fileSystemManager);
    yamlConfigurationService.init();
    var configurationData = yamlConfigurationService.getConfigurationData();

    // Groups Config
    assertEquals(5, configurationData.getGroupsConfig().getGroups().size());
    assertEquals("http://www.w3.org/2000/01/rdf-schema#description",
        configurationData.getGroupsConfig().getGroups().get("Glossary").get(1));

    // Label Config
    assertTrue(configurationData.getLabelConfig().isDisplayLabel());
    assertEquals(LabelPriority.USER_DEFINED, configurationData.getLabelConfig().getLabelPriority());
    assertFalse(configurationData.getLabelConfig().isForceLabelLang());
    assertEquals("en", configurationData.getLabelConfig().getLabelLang());
    assertEquals(MissingLanguageAction.FIRST, configurationData.getLabelConfig().getMissingLanguageAction());
    assertEquals(45, configurationData.getLabelConfig().getDefaultNames().size());

    // Ontology Config
    assertEquals("ontologies", configurationData.getOntologiesConfig().getPaths().get(0));

    // Search Config
    assertEquals(2, configurationData.getSearchConfig().getSearchDescriptions().size());
    assertEquals(3, configurationData.getSearchConfig().getFuzzyDistance());
    assertTrue(configurationData.getSearchConfig().isReindexOnStart());
    assertEquals(6, configurationData.getSearchConfig().getFindProperties().size());
    
    // Application Config
    assertFalse(configurationData.getApplicationConfig().isDisplayCopyright());
    assertFalse(configurationData.getApplicationConfig().isDisplayLicense());
    assertEquals("http://purl.org/dc/terms/license", configurationData.getApplicationConfig().getLicense().get(0));
    assertEquals("http://www.omg.org/techprocess/ab/SpecificationMetadata/copyright", configurationData.getApplicationConfig().getCopyright().get(0));
  }

  @Test
  void shouldHaveConfigMixedBothFromDefaultsAndCustomLabelConfig() throws IOException {
    var fileSystemManager = prepareFileSystem();
    prepareTestConfiguration("/configuration_yaml/label_config1.yaml");

    var yamlConfigurationService = new YamlFileBasedConfigurationService(fileSystemManager);
    yamlConfigurationService.init();
    var configurationData = yamlConfigurationService.getConfigurationData();

    // Groups Config
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
    
    // Application Config
    assertFalse(configurationData.getApplicationConfig().isDisplayCopyright());
    assertFalse(configurationData.getApplicationConfig().isDisplayLicense());
    assertEquals("http://purl.org/dc/terms/license", configurationData.getApplicationConfig().getLicense().get(0));
    assertEquals("http://www.omg.org/techprocess/ab/SpecificationMetadata/copyright", configurationData.getApplicationConfig().getCopyright().get(0));
  }

  @Test
  void shouldHaveConfigMixedBothFromDefaultsAndCustomOntologiesConfigWithMissingCustomConfigs() throws IOException {
    var fileSystemManager = prepareFileSystem();
    prepareTestConfiguration("/configuration_yaml/ontologies_config1.yaml");

    var yamlConfigurationService = new YamlFileBasedConfigurationService(fileSystemManager);
    yamlConfigurationService.init();
    var configurationData = yamlConfigurationService.getConfigurationData();

    // Groups Config
    assertEquals(5, configurationData.getGroupsConfig().getGroups().size());

    // Label Config
    assertTrue(configurationData.getLabelConfig().isDisplayLabel());
    assertEquals(LabelPriority.USER_DEFINED, configurationData.getLabelConfig().getLabelPriority());
    assertFalse(configurationData.getLabelConfig().isForceLabelLang());
    assertEquals("en", configurationData.getLabelConfig().getLabelLang());
    assertEquals(MissingLanguageAction.FIRST, configurationData.getLabelConfig().getMissingLanguageAction());
    assertEquals(45, configurationData.getLabelConfig().getDefaultNames().size());

    // Ontology Config
    var ontologiesConfig = configurationData.getOntologiesConfig();
    assertEquals("foo/bar/my_dir", ontologiesConfig.getPaths().get(0));
    assertEquals("foo/my_ontology.rdf", ontologiesConfig.getPaths().get(1));
    assertEquals("http://example.com", ontologiesConfig.getUrls().get(0));
    assertEquals("ontologies/catalog-v001.xml", ontologiesConfig.getCatalogPaths().get(0));
    assertEquals("^(About|Metadata).*", ontologiesConfig.getModuleIgnorePatterns().get(0));
    assertEquals("http://example.com/ontology", ontologiesConfig.getModuleToIgnore().get(0));
    assertEquals(
        "https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/hasMaturityLevel",
        ontologiesConfig.getMaturityLevelProperty());

    // Search Config
    assertEquals(2, configurationData.getSearchConfig().getSearchDescriptions().size());
    assertEquals(3, configurationData.getSearchConfig().getFuzzyDistance());
    assertTrue(configurationData.getSearchConfig().isReindexOnStart());
    assertEquals(6, configurationData.getSearchConfig().getFindProperties().size());
    
    // Application Config
    assertFalse(configurationData.getApplicationConfig().isDisplayCopyright());
    assertFalse(configurationData.getApplicationConfig().isDisplayLicense());
    assertEquals("http://purl.org/dc/terms/license", configurationData.getApplicationConfig().getLicense().get(0));
    assertEquals("http://www.omg.org/techprocess/ab/SpecificationMetadata/copyright", configurationData.getApplicationConfig().getCopyright().get(0));
  }

  @Test
  void shouldHaveConfigMixedBothFromDefaultsAndCustomOntologiesConfig() throws IOException {
    var fileSystemManager = prepareFileSystem();
    prepareTestConfiguration("/configuration_yaml/ontologies_config2.yaml");

    var yamlConfigurationService = new YamlFileBasedConfigurationService(fileSystemManager);
    yamlConfigurationService.init();
    var configurationData = yamlConfigurationService.getConfigurationData();

    // Groups Config
    assertEquals(5, configurationData.getGroupsConfig().getGroups().size());

    // Label Config
    assertTrue(configurationData.getLabelConfig().isDisplayLabel());
    assertEquals(LabelPriority.USER_DEFINED, configurationData.getLabelConfig().getLabelPriority());
    assertFalse(configurationData.getLabelConfig().isForceLabelLang());
    assertEquals("en", configurationData.getLabelConfig().getLabelLang());
    assertEquals(MissingLanguageAction.FIRST, configurationData.getLabelConfig().getMissingLanguageAction());
    assertEquals(45, configurationData.getLabelConfig().getDefaultNames().size());

    // Ontology Config
    var ontologiesConfig = configurationData.getOntologiesConfig();
    assertEquals("foo/bar/my_dir", ontologiesConfig.getPaths().get(0));
    assertEquals("foo/my_ontology.rdf", ontologiesConfig.getPaths().get(1));
    assertEquals("http://example.com", ontologiesConfig.getUrls().get(0));
    assertEquals("ontologies/catalog-v001.xml", ontologiesConfig.getCatalogPaths().get(0));
    assertEquals("^(About|Metadata).*", ontologiesConfig.getModuleIgnorePatterns().get(0));
    assertEquals("http://example.com/ontology", ontologiesConfig.getModuleToIgnore().get(0));
    assertEquals(
        "https://example.com/test/hasMaturityLevel",
        ontologiesConfig.getMaturityLevelProperty());

    // Search Config
    assertEquals(2, configurationData.getSearchConfig().getSearchDescriptions().size());
    assertEquals(3, configurationData.getSearchConfig().getFuzzyDistance());
    assertTrue(configurationData.getSearchConfig().isReindexOnStart());
    assertEquals(6, configurationData.getSearchConfig().getFindProperties().size());

    // Application Config
    assertFalse(configurationData.getApplicationConfig().isDisplayCopyright());
    assertFalse(configurationData.getApplicationConfig().isDisplayLicense());
    assertEquals("http://purl.org/dc/terms/license", configurationData.getApplicationConfig().getLicense().get(0));
    assertEquals("http://www.omg.org/techprocess/ab/SpecificationMetadata/copyright", configurationData.getApplicationConfig().getCopyright().get(0));
  }

  @Test
  void shouldHaveConfigMixedBothFromDefaultsAndCustomSearchConfig() throws IOException {
    var fileSystemManager = prepareFileSystem();
    prepareTestConfiguration("/configuration_yaml/search_config1.yaml");

    var yamlConfigurationService = new YamlFileBasedConfigurationService(fileSystemManager);
    yamlConfigurationService.init();
    var configurationData = yamlConfigurationService.getConfigurationData();

    // Groups Config
    assertEquals(5, configurationData.getGroupsConfig().getGroups().size());

    // Label Config
    assertTrue(configurationData.getLabelConfig().isDisplayLabel());
    assertEquals(LabelPriority.USER_DEFINED, configurationData.getLabelConfig().getLabelPriority());
    assertFalse(configurationData.getLabelConfig().isForceLabelLang());
    assertEquals("en", configurationData.getLabelConfig().getLabelLang());
    assertEquals(MissingLanguageAction.FIRST, configurationData.getLabelConfig().getMissingLanguageAction());
    assertEquals(45, configurationData.getLabelConfig().getDefaultNames().size());

    // Ontology Config
    assertEquals("ontologies", configurationData.getOntologiesConfig().getPaths().get(0));

    // Search Config
    var searchConfig = configurationData.getSearchConfig();
    assertEquals(3, searchConfig.getSearchDescriptions().size());
    assertEquals("http://purl.org/dc/terms/concrete", searchConfig.getSearchDescriptions().get(2));
    assertEquals(5, searchConfig.getFuzzyDistance());
    assertFalse(searchConfig.isReindexOnStart());
    assertEquals(2, searchConfig.getFindProperties().size());
   
    // Application Config
    assertFalse(configurationData.getApplicationConfig().isDisplayCopyright());
    assertFalse(configurationData.getApplicationConfig().isDisplayLicense());
    assertEquals("http://purl.org/dc/terms/license", configurationData.getApplicationConfig().getLicense().get(0));
    assertEquals("http://www.omg.org/techprocess/ab/SpecificationMetadata/copyright", configurationData.getApplicationConfig().getCopyright().get(0));
  }
  
  @Test
  void shouldHaveConfigMixedBothFromDefaultsAndCustomApplicationConfig() throws IOException {
    var fileSystemManager = prepareFileSystem();
    prepareTestConfiguration("/configuration_yaml/application_config1.yaml");

    var yamlConfigurationService = new YamlFileBasedConfigurationService(fileSystemManager);
    yamlConfigurationService.init();
    var configurationData = yamlConfigurationService.getConfigurationData();

    // Groups Config
    assertEquals(5, configurationData.getGroupsConfig().getGroups().size());

    // Label Config
    assertTrue(configurationData.getLabelConfig().isDisplayLabel());
    assertEquals(LabelPriority.USER_DEFINED, configurationData.getLabelConfig().getLabelPriority());
    assertFalse(configurationData.getLabelConfig().isForceLabelLang());
    assertEquals("en", configurationData.getLabelConfig().getLabelLang());
    assertEquals(MissingLanguageAction.FIRST, configurationData.getLabelConfig().getMissingLanguageAction());
    assertEquals(45, configurationData.getLabelConfig().getDefaultNames().size());

    // Ontology Config
    assertEquals("ontologies", configurationData.getOntologiesConfig().getPaths().get(0));

    // Search Config
    assertEquals(2, configurationData.getSearchConfig().getSearchDescriptions().size());
    assertEquals(3, configurationData.getSearchConfig().getFuzzyDistance());
    assertTrue(configurationData.getSearchConfig().isReindexOnStart());
    assertEquals(6, configurationData.getSearchConfig().getFindProperties().size());
   
    // Application Config
    assertTrue(configurationData.getApplicationConfig().isDisplayCopyright());
    assertTrue(configurationData.getApplicationConfig().isDisplayLicense());
    assertEquals("http://purl.org/dc/terms/license", configurationData.getApplicationConfig().getLicense().get(0));
    assertEquals("http://www.omg.org/techprocess/ab/SpecificationMetadata/copyright", configurationData.getApplicationConfig().getCopyright().get(0));
    assertEquals("https://www.omg.org/spec/Commons/AnnotationVocabulary/copyright", configurationData.getApplicationConfig().getCopyright().get(1));
  }

  private FileSystemService prepareFileSystem() {
    Path configDir = homeDir.resolve(CONFIG_DIR).toAbsolutePath();
    try {
      Files.createDirectory(configDir);
    } catch (IOException ex) {
      throw new IllegalStateException("Unable to create test config dir.", ex);
    }

    var appProperties = new AppProperties();
    appProperties.setConfigDownloadPath(configDir.toString());
    appProperties.setDefaultHomePath(homeDir.toAbsolutePath().toString());
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