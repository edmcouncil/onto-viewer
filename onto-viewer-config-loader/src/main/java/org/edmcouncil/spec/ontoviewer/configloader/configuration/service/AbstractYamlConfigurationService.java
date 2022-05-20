package org.edmcouncil.spec.ontoviewer.configloader.configuration.service;

import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.CATALOG_PATH;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.AUTOMATIC_CREATION_OF_MODULES;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.DISPLAY_LABEL;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.FIND_PROPERTIES;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.FORCE_LABEL_LANG;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.FUZZY_DISTANCE;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.GROUPS;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.IDENTIFIER;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.IRI;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.ITEMS;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.LABEL;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.LABEL_LANG;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.LABEL_PRIORITY;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.MISSING_LANGUAGE_ACTION;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.MODULE_IGNORE_PATTERN;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.MODULE_TO_IGNORE;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.NAME;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.PATH;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.PRIORITY_LIST;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.REINDEX_ON_START;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.SEARCH_DESCRIPTIONS;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.SOURCE;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.USER_DEFAULT_NAME_LIST;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.byName;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.DOWNLOAD_DIRECTORY;

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
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.GroupsConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.LabelConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.OntologiesConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.SearchConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.FindProperty;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.LabelPriority;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.MissingLanguageAction;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.UserDefaultName;
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

        case ONTOLOGIES:
          @SuppressWarnings("unchecked")
          var ontologiesConfig = handleOntologies((Map<String, Object>) entry.getValue());
          configuration.setOntologiesConfig(ontologiesConfig);
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

  protected OntologiesConfig handleOntologies(Map<String, Object> ontologies) {
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
        getBooleanFromObject(ontologies.get(AUTOMATIC_CREATION_OF_MODULES.getLabel()), AUTOMATIC_CREATION_OF_MODULES_DEFULT);
     List<String> downloadDirectory = getListOfStringsFromObject(ontologies.get(DOWNLOAD_DIRECTORY.getLabel()));
    
    return new OntologiesConfig(urls, paths, catalogPaths, downloadDirectory, zips, moduleIgnorePatterns, moduleToIgnore, automaticCreationOfModules);
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
}
