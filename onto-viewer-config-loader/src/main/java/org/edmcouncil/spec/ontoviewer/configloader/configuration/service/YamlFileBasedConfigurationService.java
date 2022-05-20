package org.edmcouncil.spec.ontoviewer.configloader.configuration.service;

import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.byName;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.apache.commons.io.IOUtils;
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
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

public class YamlFileBasedConfigurationService extends AbstractYamlConfigurationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(YamlFileBasedConfigurationService.class);
  private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("yaml", "yml");
  private static final Set<String> CONFIG_FILES_NAMES =
      Set.of("groups_config.yaml", "label_config.yaml", "ontology_config.yaml", "search_config.yaml");

  private final FileSystemManager fileSystemManager;

  @Value("${app.config.ontologies.catalog_path:}")
  private String catalogPath;
  @Value("${app.config.ontologies.download_directory:}")
  private String downloadDirectory;
  @Value("${app.config.ontologies.zip_url:}")
  private String[] zipUrl;
  @Value("${app.config.updateUrl:}")
  private String updateUrl;

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
            if (configFilePath.toString().contains(".")) {
              if (Files.isRegularFile(configFilePath)
                  && SUPPORTED_EXTENSIONS.contains(getExtension(configFilePath.toString()))) {
                String configContent = Files.readString(configFilePath);
                sb.append(configContent).append("\n");
              } else {
                LOGGER.warn("Config path '{}' is not a regular file or doesn't end with '{}'.",
                    configFilePath, SUPPORTED_EXTENSIONS);
              }
            } else {
              LOGGER.warn("Config path '{}' doesn't end with a file extension.", configFilePath);
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

  @Override
  public void reloadConfiguration() {
    if (StringUtils.hasText(updateUrl)) {
      String updateUrlPath = updateUrl;
      if (!updateUrlPath.endsWith("/")) {
        updateUrlPath += "/";
      }

      Map<String, String> configFileToUrl = new HashMap<>();
      for (String configFileName : CONFIG_FILES_NAMES) {
        configFileToUrl.put(configFileName, updateUrlPath + configFileName);
      }

      StringBuilder sb = new StringBuilder();
      for (Entry<String, String> configEntry : configFileToUrl.entrySet()) {
        var content = readConfigContent(configEntry);
        if (content.isBlank()) {
          content = readConfigForFileName(configEntry.getKey());
        }
        sb.append(content).append("\n");
      }

      var yaml = new Yaml();
      Map<String, Object> configuration = yaml.load(sb.toString());

      this.configurationData = populateConfiguration(configuration);
    }
  }

  private String readConfigForFileName(String configFileName) {
    Path configFilePath = Path.of(configFileName);
    try {
      configFilePath = fileSystemManager.getPathToConfigFile().resolve(configFileName);
      return Files.readString(configFilePath);
    } catch (IOException ex) {
      LOGGER.warn("Exception thrown while reading config content from path '{}'. Details: {}",
          configFilePath, ex.getMessage(), ex);
    }
    return "";
  }

  private String readConfigContent(Entry<String, String> configEntry) {
    try {
      var configContent = IOUtils.toString(new URL(configEntry.getValue()), StandardCharsets.UTF_8);
      overrideConfigContent(configEntry.getKey(), configContent);
    } catch (IOException ex) {
      LOGGER.warn("Exception thrown while loading configuration from URL '{}'. Details: {}",
          configEntry.getValue(), ex.getMessage(), ex);
    }
    return "";
  }

  private void overrideConfigContent(String configFileName, String configContent) {
    try {
      var configPath = fileSystemManager.getPathToConfigFile();
      var configFilePath = configPath.resolve(configFileName);
      Files.write(configFilePath,
          configContent.getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.CREATE, StandardOpenOption.WRITE);
    } catch (IOException ex) {
      LOGGER.warn("Exception thrown while overriding configuration file '{}' with a new content.", configFileName);
    }
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
            if (downloadDirectory != null && !downloadDirectory.isBlank()) {
              ontologiesConfig.getDownloadDirectory().clear();
              ontologiesConfig.getDownloadDirectory().add(downloadDirectory);
            }
            if (zipUrl != null && zipUrl.length>0) {
              ontologiesConfig.getZipUrls().clear();
              ontologiesConfig.getZipUrls().addAll(Arrays.asList(zipUrl));
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