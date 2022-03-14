package org.edmcouncil.spec.ontoviewer.configloader.configuration.service;

import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.FIND_PROPERTIES;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.FUZZY_DISTANCE;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.IDENTIFIER;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.IRI;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.ITEMS;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.LABEL;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.NAME;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.REINDEX_ON_START;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.SEARCH_DESCRIPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.GroupsConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.OntologiesConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.SearchConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.FindProperty;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

public class YamlFileBasedConfigurationService {

  private static final String DEFAULT_CONFIG_PATH = "/default_config.yaml";
  private static final int FUZZY_DISTANCE_DEFAULT = 3;
  private static final Logger LOGGER = LoggerFactory.getLogger(YamlFileBasedConfigurationService.class);
  private static final boolean REINDEX_ON_START_DEFAULT = false;

  private final FileSystemManager fileSystemManager;

  private ConfigurationData configurationData;

  public YamlFileBasedConfigurationService(FileSystemManager fileSystemManager) {
    this.fileSystemManager = fileSystemManager;
  }

  @PostConstruct
  public void init() {
    LOGGER.debug("Loading configuration from YAML file...");

    try {
      var configPath = fileSystemManager.getPathToConfigFile();

      LOGGER.debug("Configuration location: {} (isDirectory?={})", configPath, Files.isDirectory(configPath));

      StringBuilder sb = new StringBuilder();
      if (Files.isDirectory(configPath)) {
        try (Stream<Path> configFilePathsStream = Files.walk(configPath)) {
          Set<Path> configFilePaths = configFilePathsStream.collect(Collectors.toSet());
          for (Path configFilePath : configFilePaths) {
            if (Files.isRegularFile(configFilePath)) {
              String configContent = Files.readString(configFilePath);
              sb.append(configContent).append("\n");
            }
          }
        }
      } else {
        sb.append(Files.readString(configPath));
      }

      String configContent = sb.toString();

      var yaml = new Yaml();
      Map<String, Object> configuration = yaml.load(configContent);

      this.configurationData = populateConfiguration(configuration);
    } catch (IOException ex) {
      throw new IllegalStateException("Exception was thrown while loading config file.", ex);
    }
  }

  private ConfigurationData populateConfiguration(Map<String, Object> configuration) {
    ConfigurationData configurationData = readDefaultConfiguration();

    return configurationData;
  }

  private ConfigurationData readDefaultConfiguration() {
    Map<String, Object> defaultConfiguration;
    try (InputStream defaultConfigInputStream = getClass().getResourceAsStream(DEFAULT_CONFIG_PATH)) {
      var yaml = new Yaml();
      defaultConfiguration = yaml.load(defaultConfigInputStream);
    } catch (IOException ex) {
      throw new IllegalStateException(
          "Unable to load the default config file from classpath: " + DEFAULT_CONFIG_PATH,
          ex);
    }

    var configuration = new ConfigurationData();

    for (Entry<String, Object> entry : defaultConfiguration.entrySet()) {
      var configurationKey = ConfigurationKey.byName(entry.getKey());
      switch (configurationKey) {
        case GROUPS_CONFIG:
          @SuppressWarnings("unchecked")
          var groupsConfig = handleGroupsConfig((Map<String, Object>) entry.getValue());
          configuration.setGroupsConfig(groupsConfig);
          break;

        case SEARCH_CONFIG:
          @SuppressWarnings("unchecked")
          var searchConfig = handleSearchConfig((Map<String, Object>) entry.getValue());
          configuration.setSearchConfig(searchConfig);
          break;

        case ONTOLOGIES:
          @SuppressWarnings("unchecked")
          var ontologiesConfig = handleOntologies((List<Map<String, Object>>) entry.getValue());
          configuration.setOntologiesConfig(ontologiesConfig);
          break;

        default:
          LOGGER.warn("Unknown top-level configuration key {}.", configurationKey);
      }
    }

    return configuration;
  }

  private GroupsConfig handleGroupsConfig(Map<String, Object> groupsConfig) {
    List<String> priorityList;
    Map<String, List<String>> groups = new HashMap<>();

    var priorityListObject = groupsConfig.get(ConfigurationKey.PRIORITY_LIST.getLabel());
    priorityList = getStringListFromObject(priorityListObject);

    var groupsObject = groupsConfig.get(ConfigurationKey.GROUPS.getLabel());
    if (groupsObject instanceof List) {
      var rawGroupsList = (List<?>) groupsObject;
      groups = mapToMapOfList(rawGroupsList);
    }

    return new GroupsConfig(priorityList, groups);
  }

  private Map<String, List<String>> mapToMapOfList(List<?> rawGroupsList) {
    Map<String, List<String>> groupsMap = new HashMap<>();

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

  private SearchConfig handleSearchConfig(Map<String, Object> searchConfig) {
    var searchDescriptionsObject = searchConfig.get(SEARCH_DESCRIPTIONS.getLabel());
    List<String> searchDescriptions = getStringListFromObject(searchDescriptionsObject);

    var fuzzyDistanceObject = searchConfig.get(FUZZY_DISTANCE.getLabel());
    int fuzzyDistance = getIntFromObject(fuzzyDistanceObject, FUZZY_DISTANCE_DEFAULT);

    var reindexOnStartObject = searchConfig.get(REINDEX_ON_START.getLabel());
    boolean reindexOnStart = getBooleanFromObject(reindexOnStartObject, REINDEX_ON_START_DEFAULT);

    var findPropertiesObject = searchConfig.get(FIND_PROPERTIES.getLabel());
    List<FindProperty> findProperties = getFindPropertiesList(findPropertiesObject);

    return new SearchConfig(searchDescriptions, fuzzyDistance, reindexOnStart, findProperties);
  }

  private List<String> getStringListFromObject(Object stringListAsObject) {
    if (stringListAsObject instanceof List) {
      var rawPriorityList = (List<?>) stringListAsObject;
      return rawPriorityList.stream().map(Object::toString).collect(Collectors.toList());
    }
    return new ArrayList<>();
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

  private boolean getBooleanFromObject(Object booleanAsObject, boolean defaultValue) {
    if (booleanAsObject != null) {
      var booleanValue = booleanAsObject.toString().toLowerCase();
      if ("true".equals(booleanValue)) {
        return true;
      } else if ("false".equals(booleanValue)) {
        return false;
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

  private OntologiesConfig handleOntologies(List<Map<String, Object>> ontologies) {
    List<String> urls = new ArrayList<>();
    List<String> paths = new ArrayList<>();

    for (Map<String, Object> ontologyConfig : ontologies) {
      if (ontologyConfig.containsKey(ConfigurationKey.PATH.getLabel())) {
        paths.add(ontologyConfig.get(ConfigurationKey.PATH.getLabel()).toString());
      } else if (ontologyConfig.containsKey(ConfigurationKey.URL.getLabel())) {
        urls.add(ontologyConfig.get(ConfigurationKey.URL.getLabel()).toString());
      } else {
        LOGGER.warn("Unknown keys '{}' in the ontologies location configuration.", ontologyConfig.keySet());
      }
    }

    return new OntologiesConfig(urls, paths);
  }
}
