package org.edmcouncil.spec.ontoviewer.configloader.configuration.service;

import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.byName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.ApplicationConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.GroupsConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.IntegrationsConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.LabelConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.OntologiesConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.SearchConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

public class YamlFileBasedConfigurationService extends AbstractYamlConfigurationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(YamlFileBasedConfigurationService.class);
  private static final String ACCEPT_HEADER = "Accept";
  private static final String YAML_MIME_TYPE = "application/yaml";
  private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("yaml", "yml");
  private static final Set<String> CONFIG_FILES_NAMES =
      Set.of(
          "groups_config.yaml",
          "label_config.yaml",
          "ontology_config.yaml",
          "search_config.yaml",
          "application_config.yaml",
          "integration_config.yaml");

  private final OkHttpClient httpClient = new OkHttpClient();
  private final FileSystemService fileSystemService;

  @Value("${app.config.ontologies.catalog_path:}")
  private String catalogPath;
  @Value("${app.config.ontologies.download_directory:}")
  private String downloadDirectory;
  @Value("${app.config.ontologies.zip_url:}")
  private String[] zipUrl;
  @Value("${app.config.updateUrl:}")
  private String updateUrl;

  private ConfigurationData configurationData;

  public YamlFileBasedConfigurationService(FileSystemService fileSystemService) {
    this.fileSystemService = fileSystemService;
  }

  @Override
  @PostConstruct
  public void init() {
    LOGGER.debug("Loading configuration from YAML file...");

    try {
      var configPath = fileSystemService.getPathToConfigFile();

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
        var content = readRemoteConfigContent(configEntry);
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
      configFilePath = fileSystemService.getPathToConfigFile().resolve(configFileName);
      return Files.readString(configFilePath);
    } catch (IOException ex) {
      LOGGER.warn("Exception thrown while reading config content from path '{}'. Details: {}",
          configFilePath, ex.getMessage(), ex);
    }
    return "";
  }

  private String readRemoteConfigContent(Entry<String, String> configEntry) {
    var configContent = downloadYamlFileContent(configEntry.getValue());

    try {
      // We want to check if the input config content is a valid YAML
      var yaml = new Yaml();
      // We need to add 'foo: bar' because without that, YAML scanner may not raise exception for incorrect input
      yaml.load(configContent + "\n\nfoo: bar");
    } catch (RuntimeException ex) {
      LOGGER.warn("YAML config file '{}' from URL '{}' isn't correct. Ignoring it. YAML reading exception: {}",
          configEntry.getKey(),
          configEntry.getValue(),
          ex.getMessage());
      configContent = "";
    }

    overrideConfigContent(configEntry.getKey(), configContent);
    return configContent;
  }

  private String downloadYamlFileContent(String url) {
    Request request = new Request.Builder()
        .url(url)
        .get()
        .addHeader(ACCEPT_HEADER, YAML_MIME_TYPE)
        .build();

    try (Response response = httpClient.newCall(request).execute()) {
      var responseCode = response.code();
      var responseBody = response.body().string();
      if (!response.isSuccessful()) {
        LOGGER.warn("Request downloading configuration file from URL '{}' wasn't successful. "
                + "The response ended with code {}",
            url,
            responseCode);

        return "";
      }

      return responseBody;
    } catch (Exception ex) {
      LOGGER.warn("Exception occurred while handling configuration request from URL '{}' data.world describe query: {}",
          url,
          ex.getMessage());
    }

    return "";
  }

  private void overrideConfigContent(String configFileName, String configContent) {
    if (configContent.isBlank()) {
      return;
    }

    try {
      var configPath = fileSystemService.getPathToConfigFile();
      var configFilePath = configPath.resolve(configFileName);
      Files.write(configFilePath,
          configContent.getBytes(StandardCharsets.UTF_8),
          StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException ex) {
      LOGGER.warn("Exception thrown while overriding configuration file '{}' with a new content.", configFileName);
    }
  }

  private ConfigurationData populateConfiguration(Map<String, Object> configuration) {
    ConfigurationData configurationDataCandidate = readDefaultConfiguration();

    if (configuration != null) {
      for (Entry<String, Object> configEntry : configuration.entrySet()) {
        ConfigurationKey configKey = byName(configEntry.getKey());
        Object configEntryValue = configEntry.getValue();

        if (configEntryValue instanceof Map) {
          @SuppressWarnings("unchecked")
          var configMap = (Map<String, Object>) configEntryValue;

          switch (configKey) {
            case GROUPS_CONFIG: {
              GroupsConfig groupsConfig = handleGroupsConfig(configMap);
              configurationDataCandidate.setGroupsConfig(groupsConfig);
              break;
            }
            case LABEL_CONFIG: {
              LabelConfig labelConfig = handleLabelConfig(configMap);
              configurationDataCandidate.setLabelConfig(labelConfig);
              break;
            }
            case SEARCH_CONFIG: {
              SearchConfig searchConfig = handleSearchConfig(configMap);
              configurationDataCandidate.setSearchConfig(searchConfig);
              break;
            }
            case APPLICATION_CONFIG: {
              ApplicationConfig applicationConfig = handleApplicationConfig(configMap);
              configurationDataCandidate.setApplicationConfig(applicationConfig);
              break;
            }
            case ONTOLOGIES: {
              OntologiesConfig ontologiesConfig = handleOntologyConfig(
                  configMap,
                  configurationDataCandidate.getOntologiesConfig());
              configurationDataCandidate.setOntologiesConfig(ontologiesConfig);

              if (catalogPath != null && !catalogPath.isBlank()) {
                ontologiesConfig.getCatalogPaths().clear();
                ontologiesConfig.getCatalogPaths().add(catalogPath);
              }
              if (downloadDirectory != null && !downloadDirectory.isBlank()) {
                ontologiesConfig.getDownloadDirectory().clear();
                ontologiesConfig.getDownloadDirectory().add(downloadDirectory);
              }
              if (zipUrl != null && zipUrl.length > 0) {
                ontologiesConfig.getZipUrls().addAll(Arrays.asList(zipUrl));
              }
              break;
            }
            default:
              LOGGER.warn("Config key '{}' is not expected.", configKey);
          }
        } else if (configEntryValue instanceof List) {
          @SuppressWarnings("unchecked")
          var configList = (List<Map<String, Object>>) configEntryValue;

          switch (configKey) {
            case INTEGRATIONS:
              IntegrationsConfig integrationsConfig = handleIntegrationsConfig(configList);
              configurationDataCandidate.setIntegrationsConfig(integrationsConfig);
              break;
            default:
              LOGGER.warn("Config key '{}' is not expected.", configKey);
          }
        }
      }
      OntologiesConfig ontologiesConfig = configurationDataCandidate.getOntologiesConfig();
      if (!ontologiesConfig.getZipUrls().isEmpty() && !ontologiesConfig.getDownloadDirectory().isEmpty()) {
        String downloadDirectory = ontologiesConfig.getDownloadDirectory().stream().findFirst().orElse("");
        for (String fileUrl : ontologiesConfig.getZipUrls()) {
          String[] zip = fileUrl.split("#");
          if ((zip.length > 1) && (zip[1].length() > 0)) {
            Path path = Paths.get(downloadDirectory.concat("/").concat(zip[1]));
            LOGGER.info("CATALOG_FILE={}", path.normalize());
            ontologiesConfig.getCatalogPaths().add(path.normalize().toString());
          }
        }
      }
    }

    return configurationDataCandidate;
  }
}