package org.edmcouncil.spec.ontoviewer.configloader.configuration.service;

import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.byName;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.GroupsConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.LabelConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.OntologiesConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.SearchConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.yaml.snakeyaml.Yaml;

public class YamlFileBasedConfigurationService extends AbstractYamlConfigurationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(YamlFileBasedConfigurationService.class);

  private final FileSystemManager fileSystemManager;

  @Value("${app.config.ontologies.catalog_path:}")
  private String catalogPath;
  private ConfigurationData configurationData;

  public YamlFileBasedConfigurationService(FileSystemManager fileSystemManager) {
    this.fileSystemManager = fileSystemManager;
  }

  @Override
  @PostConstruct
  public void init() {
    LOGGER.debug("Loading configuration from YAML file...");

    try {
      var configPath = fileSystemManager.getPathToConfigFile();

      LOGGER.debug("Configuration location: {} (isDirectory?={})", configPath, Files.isDirectory(configPath));

      StringBuilder sb = new StringBuilder();
      if (Files.isDirectory(configPath)) {
        try (Stream<Path> configFilePathsStream = Files.walk(configPath, FileVisitOption.FOLLOW_LINKS)) {
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

  @Override
  public ConfigurationData getConfigurationData() {
    return configurationData;
  }

  @Override
  public boolean hasConfiguredGroups() {
    return configurationData.getGroupsConfig().getGroups() != null
        && !configurationData.getGroupsConfig().getGroups().isEmpty();
  }

  private ConfigurationData populateConfiguration(Map<String, Object> configuration) {
    ConfigurationData configurationDataCandidate = readDefaultConfiguration();

    if (configuration != null) {
      for (Entry<String, Object> configEntry : configuration.entrySet()) {
        ConfigurationKey configKey = byName(configEntry.getKey());

        @SuppressWarnings("unchecked")
        var configEntryValue = (Map<String, Object>) configEntry.getValue();

        switch (configKey) {
          case GROUPS_CONFIG: {
            GroupsConfig groupsConfig = handleGroupsConfig(configEntryValue);
            configurationDataCandidate.setGroupsConfig(groupsConfig);
            break;
          }
          case LABEL_CONFIG: {
            LabelConfig labelConfig = handleLabelConfig(configEntryValue);
            configurationDataCandidate.setLabelConfig(labelConfig);
            break;
          }
          case SEARCH_CONFIG: {
            SearchConfig searchConfig = handleSearchConfig(configEntryValue);
            configurationDataCandidate.setSearchConfig(searchConfig);
            break;
          }
          case ONTOLOGIES: {
            OntologiesConfig ontologiesConfig = handleOntologies(configEntryValue);
            configurationDataCandidate.setOntologiesConfig(ontologiesConfig);

            if (catalogPath != null && !catalogPath.isBlank()) {
              ontologiesConfig.getCatalogPaths().clear();
              ontologiesConfig.getCatalogPaths().add(catalogPath);
            }
            break;
          }
          default:
            LOGGER.warn("Config key '{}' is not expected.", configKey);
        }
      }
    }

    return configurationDataCandidate;
  }
}