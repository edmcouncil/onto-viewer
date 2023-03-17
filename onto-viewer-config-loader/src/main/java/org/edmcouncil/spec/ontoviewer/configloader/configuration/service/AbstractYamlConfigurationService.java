package org.edmcouncil.spec.ontoviewer.configloader.configuration.service;

import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.*;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.ApplicationConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.GroupsConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.Integration;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.IntegrationsConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.LabelConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.OntologiesConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.SearchConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;

public abstract class AbstractYamlConfigurationService implements ApplicationConfigurationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractYamlConfigurationService.class);
  private static final int FUZZY_DISTANCE_DEFAULT = 3;
  private static final boolean REINDEX_ON_START_DEFAULT = false;
  private static final boolean DISPLAY_LABEL_DEFAULT = true;
  private static final String LABEL_PRIORITY_DEFAULT = "USER_DEFINED";
  private static final boolean FORCE_LABEL_LANG_DEFAULT = false;
  private static final String LABEL_LANG_DEFAULT = "en";
  private static final String MISSING_LANGUAGE_ACTION_DEFAULT = "FIRST";
  private static final boolean AUTOMATIC_CREATION_OF_MODULES_DEFULT = false;
  private static final boolean DISPLAY_LICENSE_DEFAULT = true;
  private static final boolean DISPLAY_COPYRIGHT_DEFAULT = true;
  private static final boolean DISPLAY_QNAME_DEFAULT = true;

  @Override
  public void init() {
    // Default empty implementation
  }

  @Override
  public void reloadConfiguration() {

  }

  protected ConfigurationData readDefaultConfiguration() {
    String defaultConfigContent = readDefaultConfigContent();

    var yaml = new Yaml();
    Map<String, Object> defaultConfiguration = yaml.load(defaultConfigContent);

    var configuration = new ConfigurationData();

    for (Entry<String, Object> entry : defaultConfiguration.entrySet()) {
      var configurationKey = byName(entry.getKey());
      switch (configurationKey) {
        case GROUPS_CONFIG:
          @SuppressWarnings("unchecked")
          var groupsConfig = handleGroupsConfig((Map<String, Object>) entry.getValue());
          configuration.setGroupsConfig(groupsConfig);
          break;
        case LABEL_CONFIG:
          @SuppressWarnings("unchecked")
          var labelConfig = handleLabelConfig((Map<String, Object>) entry.getValue());
          configuration.setLabelConfig(labelConfig);
          break;

        case SEARCH_CONFIG:
          @SuppressWarnings("unchecked")
          var searchConfig = handleSearchConfig((Map<String, Object>) entry.getValue());
          configuration.setSearchConfig(searchConfig);
          break;
        case APPLICATION_CONFIG:
          @SuppressWarnings("unchecked")
          var applicationConfig = handleApplicationConfig((Map<String, Object>) entry.getValue());
          configuration.setApplicationConfig(applicationConfig);
          break;
        case ONTOLOGIES:
          @SuppressWarnings("unchecked")
          var ontologiesConfig = handleOntologyConfig((Map<String, Object>) entry.getValue(), new OntologiesConfig());
          configuration.setOntologiesConfig(ontologiesConfig);
          break;
        case INTEGRATIONS:
          @SuppressWarnings("unchecked")
          var integrationsMap = (List<Map<String, Object>>) entry.getValue();
          var integrationsConfig = handleIntegrationsConfig(integrationsMap);
          configuration.setIntegrationsConfig(integrationsConfig);
          break;
        default:
          LOGGER.warn("Unknown top-level configuration key {}.", configurationKey);
      }
    }

    return configuration;
  }

  private String readDefaultConfigContent() {
    StringBuilder sb = new StringBuilder();

    try {
      ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
      Resource[] resources = patternResolver.getResources("classpath*:/default_*_config.yaml");
      for (Resource resource : resources) {
        if (resource == null) {
          continue;
        }
        sb.append(IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8))
            .append("\n");
      }
    } catch (IOException ex) {
      throw new IllegalStateException("Exception thrown while reading default configuration", ex);
    }

    return sb.toString();
  }

  protected GroupsConfig handleGroupsConfig(Map<String, Object> groupsConfig) {
    Map<String, List<String>> groups = new HashMap<>();

    var priorityListObject = groupsConfig.get(PRIORITY_LIST.getLabel());
    List<String> priorityList = getListOfStringsFromObject(priorityListObject);

    var groupsObject = groupsConfig.get(GROUPS.getLabel());
    if (groupsObject instanceof List) {
      var rawGroupsList = (List<?>) groupsObject;
      groups = mapToMapOfList(rawGroupsList);
    }

    return new GroupsConfig(priorityList, groups);
  }

  protected ApplicationConfig handleApplicationConfig(Map<String, Object> applicationConfig) {

    var licenseObject = applicationConfig.get(LICENSE.getLabel());
    List<String> license = getListOfStringsFromObject(licenseObject);

    var copyrightObject = applicationConfig.get(COPYRIGHT.getLabel());
    List<String> copyright = getListOfStringsFromObject(copyrightObject);

    var displayLicenseObject = applicationConfig.get(DISPLAY_LICENSE.getLabel());
    boolean displayLicense = getBooleanFromObject(displayLicenseObject, DISPLAY_LICENSE_DEFAULT);

    var displayCopyrightObject = applicationConfig.get(DISPLAY_COPYRIGHT.getLabel());
    boolean displayCopyright = getBooleanFromObject(displayCopyrightObject, DISPLAY_COPYRIGHT_DEFAULT);

    var displayQNameObject = applicationConfig.get(DISPLAY_QNAME.getLabel());
    boolean displayQName = getBooleanFromObject(displayQNameObject, DISPLAY_QNAME_DEFAULT);

    return new ApplicationConfig(license, copyright, displayLicense, displayCopyright, displayQName);
  }

  protected LabelConfig handleLabelConfig(Map<String, Object> labelConfig) {
    var displayLabelObject = labelConfig.get(DISPLAY_LABEL.getLabel());
    boolean displayLabel = getBooleanFromObject(displayLabelObject, DISPLAY_LABEL_DEFAULT);

    var labelPriorityObject = labelConfig.get(LABEL_PRIORITY.getLabel());
    String labelPriorityString = labelPriorityObject != null ? labelPriorityObject.toString() : LABEL_PRIORITY_DEFAULT;
    LabelPriority labelPriority = LabelPriority.USER_DEFINED;
    try {
      labelPriority = LabelPriority.valueOf(labelPriorityString);
    } catch (IllegalArgumentException ex) {
      LOGGER.warn("'{}' string is not a valid value for label priority. Using the default value: USER_DEFINED",
          labelPriorityString);
    }

    var forceLabelLangObject = labelConfig.get(FORCE_LABEL_LANG.getLabel());
    boolean forceLabelLang = getBooleanFromObject(forceLabelLangObject, FORCE_LABEL_LANG_DEFAULT);

    var labelLangObject = labelConfig.get(LABEL_LANG.getLabel());
    String labelLang = labelLangObject != null ? labelLangObject.toString() : LABEL_LANG_DEFAULT;

    var missingLanguageActionObject = labelConfig.get(MISSING_LANGUAGE_ACTION.getLabel());
    String missingLanguageActionString =
        missingLanguageActionObject != null ? missingLanguageActionObject.toString() : MISSING_LANGUAGE_ACTION_DEFAULT;
    MissingLanguageAction missingLanguageAction = MissingLanguageAction.FIRST;
    try {
      missingLanguageAction = MissingLanguageAction.valueOf(missingLanguageActionString);
    } catch (IllegalArgumentException ex) {
      LOGGER.warn("'{}' string is not a valid value for missing language action. Using the default value: FIRST",
          missingLanguageActionString);
    }

    var userDefaultNameListObject = labelConfig.get(USER_DEFAULT_NAME_LIST.getLabel());
    List<UserDefaultName> userDefaultNameList = getUserDefaultNameList(userDefaultNameListObject);

    return new LabelConfig(
        displayLabel,
        labelPriority,
        forceLabelLang,
        labelLang,
        missingLanguageAction,
        userDefaultNameList);
  }

  protected SearchConfig handleSearchConfig(Map<String, Object> searchConfig) {
    var searchDescriptionsObject = searchConfig.get(SEARCH_DESCRIPTIONS.getLabel());
    List<String> searchDescriptions = getListOfStringsFromObject(searchDescriptionsObject);

    var fuzzyDistanceObject = searchConfig.get(FUZZY_DISTANCE.getLabel());
    int fuzzyDistance = getIntFromObject(fuzzyDistanceObject, FUZZY_DISTANCE_DEFAULT);

    var reindexOnStartObject = searchConfig.get(REINDEX_ON_START.getLabel());
    boolean reindexOnStart = getBooleanFromObject(reindexOnStartObject, REINDEX_ON_START_DEFAULT);

    var findPropertiesObject = searchConfig.get(FIND_PROPERTIES.getLabel());
    List<FindProperty> findProperties = getFindPropertiesList(findPropertiesObject);

    return new SearchConfig(searchDescriptions, fuzzyDistance, reindexOnStart, findProperties);
  }

  protected OntologiesConfig handleOntologyConfig(Map<String, Object> ontologies, OntologiesConfig ontologiesConfig) {
    List<String> urls = new ArrayList<>();
    List<String> paths = new ArrayList<>();
    List<String> zips = new ArrayList<>();

    @SuppressWarnings("unchecked")
    var sources = (List<Map<String, String>>) ontologies.get(SOURCE.getLabel());
    if (sources != null) {
      for (Map<String, String> source : sources) {
        for (Entry<String, String> sourceEntry : source.entrySet()) {
          var sourceEntryKey = sourceEntry.getKey();

          if (sourceEntryKey.equals(PATH.getLabel())) {
            paths.add(sourceEntry.getValue());
          } else if (sourceEntryKey.equals(ConfigurationKey.URL.getLabel())) {
            urls.add(sourceEntry.getValue());
          } else if (sourceEntryKey.equals(ConfigurationKey.ZIP.getLabel())) {
            zips.add(sourceEntry.getValue());
          } else {
            LOGGER.warn("Unknown key '{}' with value '{}' in the ontologies source configuration.",
                sourceEntry.getKey(), sourceEntry.getValue());
          }
        }
      }
    }

    List<String> catalogPaths = getListOfStringsFromObject(ontologies.get(CATALOG_PATH.getLabel()));
    List<String> moduleIgnorePatterns = getListOfStringsFromObject(ontologies.get(MODULE_IGNORE_PATTERN.getLabel()));
    List<String> moduleToIgnore = getListOfStringsFromObject(ontologies.get(MODULE_TO_IGNORE.getLabel()));
    boolean automaticCreationOfModules =
        getBooleanFromObject(ontologies.get(AUTOMATIC_CREATION_OF_MODULES.getLabel()),
            AUTOMATIC_CREATION_OF_MODULES_DEFULT);
    List<String> downloadDirectory = getListOfStringsFromObject(ontologies.get(DOWNLOAD_DIRECTORY.getLabel()));
    String moduleClassIri = getStringsFromObject(
        ontologies.get(MODULE_CLASS_IRI.getLabel()),
        ontologiesConfig.getModuleClassIri());
    List<Pair> maturityLevelDefinition = getMaturityLevelDefinitionNameList(
        ontologies.get(MATURITY_LEVEL_DEFINITION.getLabel()));
    String maturityLevelProperty = getStringsFromObject(
        ontologies.get(MATURITY_LEVEL_PROPERTY.getLabel()),
        ontologiesConfig.getMaturityLevelProperty());

    return new OntologiesConfig(urls, paths, catalogPaths, downloadDirectory, zips, moduleIgnorePatterns,
        moduleToIgnore, maturityLevelDefinition, automaticCreationOfModules, moduleClassIri, maturityLevelProperty);
  }

  protected IntegrationsConfig handleIntegrationsConfig(List<Map<String, Object>> integrationsList) {
    Map<String, Integration> integrations = new HashMap<>();
    if (integrationsList == null) {
      return new IntegrationsConfig(integrations);
    }

    for (Map<String, Object> integrationMap : integrationsList) {
      var integrationId = integrationMap.get(INTEGRATION_ID.getLabel());
      var integrationUrl = integrationMap.get(INTEGRATION_URL.getLabel());
      var integrationAccessToken = integrationMap.get(INTEGRATION_ACCESS_TOKEN.getLabel());

      if (integrationId == null || integrationUrl == null) {
        LOGGER.error("Missing data while reading integrations config: "
            + "integrationId={}, integrationUrl={}",
            integrationId, integrationUrl);
        continue;
      }

      if (integrationAccessToken == null) {
        integrationAccessToken = "";
      }

      var integration = new Integration(
          integrationId.toString(),
          integrationUrl.toString(),
          integrationAccessToken.toString());
      integrations.put(integrationId.toString(), integration);
    }

    return new IntegrationsConfig(integrations);
  }

  protected List<Pair> getMaturityLevelDefinitionNameList(Object maturityLevelDefinitionNameList) {
    List<Pair> maturityLevelDefinition = new ArrayList<>();

    if (maturityLevelDefinitionNameList instanceof List) {
      List<?> rawMaturityLevelDefinition = (List<?>) maturityLevelDefinitionNameList;
      for (Object rawMaturityLevel : rawMaturityLevelDefinition) {
        if (rawMaturityLevel instanceof Map) {
          var maturityLevelDefinitionMap = (Map<String, Object>) rawMaturityLevel;
          var label = maturityLevelDefinitionMap.get(ConfigurationKey.LABEL.getLabel());
          var iri = maturityLevelDefinitionMap.get(IRI.getLabel());
          if (label != null && iri != null) {
            maturityLevelDefinition.add(new Pair(label.toString(), iri.toString()));
          }
        }
      }
    }
    return maturityLevelDefinition;
  }

  protected Map<String, List<String>> mapToMapOfList(List<?> rawGroupsList) {
    Map<String, List<String>> groupsMap = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    var rawGroupsMap = (List<Map<String, Object>>) rawGroupsList;
    for (Map<String, Object> groupItem : rawGroupsMap) {
      var groupName = groupItem.get(NAME.getLabel());
      if (groupName != null) {
        var groupItems = groupItem.getOrDefault(ITEMS.getLabel(), new ArrayList<>());
        if (groupItems instanceof List) {
          var rawGroupItemsList = (List<?>) groupItems;
          var groupItemsList = rawGroupItemsList.stream()
              .map(Object::toString)
              .collect(Collectors.toList());
          groupsMap.put(groupName.toString(), groupItemsList);
        }
      } else {
        LOGGER.warn("For a group list item the expected key '{}' was not present. Item details: {}",
            NAME.getLabel(), groupItem);
      }
    }

    return groupsMap;
  }

  protected List<String> getListOfStringsFromObject(Object listOfStringsAsObject) {
    if (listOfStringsAsObject instanceof List) {
      var rawPriorityList = (List<?>) listOfStringsAsObject;
      return rawPriorityList.stream().map(Object::toString).collect(Collectors.toList());
    }
    return new ArrayList<>();
  }

  protected List<UserDefaultName> getUserDefaultNameList(Object userDefaultNameListObject) {
    List<UserDefaultName> userDefaultNames = new ArrayList<>();

    if (userDefaultNameListObject instanceof List) {
      List<?> rawUserDefaultNameList = (List<?>) userDefaultNameListObject;
      for (Object rawUserDefaultName : rawUserDefaultNameList) {
        if (rawUserDefaultName instanceof Map) {
          @SuppressWarnings("unchecked")
          var userDefaultNameMap = (Map<String, Object>) rawUserDefaultName;
          var id = userDefaultNameMap.get(ConfigurationKey.ID.getLabel());
          var name = userDefaultNameMap.get(ConfigurationKey.NAME.getLabel());

          if (id != null && name != null) {
            userDefaultNames.add(new UserDefaultName(id.toString(), name.toString()));
          }
        }
      }
    }

    return userDefaultNames;
  }

  private boolean getBooleanFromObject(Object booleanAsObject, boolean defaultValue) {
    if (booleanAsObject != null) {
      var booleanValue = booleanAsObject.toString().strip().toLowerCase();
      if ("true".equals(booleanValue)) {
        return true;
      } else if ("false".equals(booleanValue)) {
        return false;
      }
    }

    return defaultValue;
  }

  private int getIntFromObject(Object intAsString, int defaultValue) {
    if (intAsString != null) {
      try {
        return Integer.parseInt(intAsString.toString());
      } catch (NumberFormatException ex) {
        // Ignore
      }
    }

    return defaultValue;
  }

  private List<FindProperty> getFindPropertiesList(Object findPropertiesObject) {
    List<FindProperty> findProperties = new ArrayList<>();

    if (findPropertiesObject instanceof List) {
      List<?> rawFindPropertiesList = (List<?>) findPropertiesObject;
      for (Object rawFindProperty : rawFindPropertiesList) {
        if (rawFindProperty instanceof Map) {
          @SuppressWarnings("unchecked")
          var findPropertyMap = (Map<String, Object>) rawFindProperty;
          var label = findPropertyMap.get(LABEL.getLabel());
          var identifier = findPropertyMap.get(IDENTIFIER.getLabel());
          var iri = findPropertyMap.get(IRI.getLabel());

          if (label != null && identifier != null && iri != null) {
            findProperties.add(
                new FindProperty(label.toString(), identifier.toString(), iri.toString()));
          }
        }
      }
    }

    return findProperties;
  }

  private String getStringsFromObject(Object stringAsObject, String defaultValue) {
    if (stringAsObject != null) {
      return stringAsObject.toString();
    }
    return defaultValue;
  }
}
