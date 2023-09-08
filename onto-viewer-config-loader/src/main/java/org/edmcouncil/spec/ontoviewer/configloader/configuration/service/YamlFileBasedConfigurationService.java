package org.edmcouncil.spec.ontoviewer.configloader.configuration.service;

import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey.byName;

import java.io.IOException;
import java.net.URL;
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

import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.ApplicationConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.GroupsConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.IntegrationsConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.LabelConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.OntologiesConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.SearchConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationKey;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.exception.UnableToLoadConfigurationException;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class YamlFileBasedConfigurationService extends AbstractYamlConfigurationService {

  private static final Logger LOGGER =
          LoggerFactory.getLogger(YamlFileBasedConfigurationService.class);
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

  private static final String TEST_FILE_NAME = "test_config.yaml";

  private static final String FILE_PREFIX = "file:/";
  private static final String HTTP_PROTOCOL = "http";
  private static final String HTTPS_PROTOCOL = "https";

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
  public void init() throws IOException {
    reloadConfiguration();
  }

  @Override
  public void reloadConfiguration() throws IOException {

    LOGGER.debug("Start loading configuration...");
    ConfigChecklist configChecklist = new ConfigChecklist();
    URL configURL = getConfigURL(configChecklist);

    checkUrlProtocol(configURL, configChecklist);

    if (configChecklist.isRemotePathIsSet()) {
      loadRemoteConfig();
    } else {
      loadConfigFromFiles(configChecklist, configURL);
    }
    LOGGER.debug("End loading configuration.");
  }

  @NotNull
  private URL getConfigURL(ConfigChecklist configChecklist) throws IOException {
    URL configURL;
    if (StringUtils.hasText(updateUrl)) {
      configURL = new URL(updateUrl);
    } else {
      configURL = getDefaultConfig(configChecklist);
    }
    return configURL;
  }

  @NotNull
  private URL getDefaultConfig(ConfigChecklist configChecklist) throws IOException {
    var defaultConfigLocation = fileSystemService.getPathToDefaultConfigDirectory();
    LOGGER.warn("Config URL is not set. Using default value: `{}`", defaultConfigLocation);
    configChecklist.setConfigPathIsSet(false);
    return new URL(FILE_PREFIX + defaultConfigLocation);
  }

  private void checkUrlProtocol(URL configURL, ConfigChecklist configChecklist) {
    if ( (configURL.getProtocol().startsWith(HTTP_PROTOCOL)
            || configURL.getProtocol().startsWith(HTTPS_PROTOCOL))) {
      configChecklist.setRemotePathIsSet(true);
    }
  }

  private void loadConfigFromFiles(ConfigChecklist configChecklist, URL configURL) {
    try {
      String configURLString = configURL.toString().replace(FILE_PREFIX, "");
      Path configPath = fileSystemService.getPathToFile(configURLString);
      if (Files.notExists(configPath)) {
        configChecklist.setDefaultConfigPathIsSet(false);
        if (configChecklist.isConfigPathIsSet()) {
          throw new UnableToLoadConfigurationException(
                  "The configuration folder specified by the user does not exist");
        }
        LOGGER.warn("Config directory not exists: `{}`. Load default config files.", configPath);
        this.configurationData = readDefaultConfiguration();
        return;
      }

      String configContent = loadConfigFromLocalFilesOrDefaultConfigIfNotExist(configPath);

      var yaml = new Yaml();
      Map<String, Object> configuration = yaml.load(configContent);

      this.configurationData = populateConfiguration(configuration);

    } catch (IOException ex) {
      throw new IllegalStateException("Exception was thrown while loading config file.", ex);
    } catch (UnableToLoadConfigurationException ex) {
      throw new IllegalStateException(ex.getMessage());
    }
  }

  private String loadConfigFromLocalFilesOrDefaultConfigIfNotExist(Path configPath) throws IOException {
    StringBuilder sb = new StringBuilder();
    if (Files.isDirectory(configPath)) {
      try (Stream<Path> configFilePathsStream = Files.walk(configPath, FileVisitOption.FOLLOW_LINKS)) {
        Set<Path> configFilePaths = configFilePathsStream.collect(Collectors.toSet());
        Set<String> notExistingFiles = getNotExistingAndEmptyFiles(configFilePaths);
        if (!notExistingFiles.isEmpty()) {
          LOGGER.warn("Missing config file(s): `{}`. Reading for them default files.", notExistingFiles);
          for (var file : notExistingFiles) {
            var fileContent = readDefaultConfigContent(file);
            if (StringUtils.hasText(fileContent)) {
              sb.append(fileContent);
            } else {
              LOGGER.warn("The `{}` file does not have a default configuration file.", file);
            }
          }
        }

        for (Path configFilePath : configFilePaths) {
          if (configFilePath.toString().contains(".")) {
            if (Files.isRegularFile(configFilePath)
                    && SUPPORTED_EXTENSIONS.contains(getExtension(configFilePath.toString()))) {
              String configContent = Files.readString(configFilePath);
              sb.append(configContent).append("\n");
            } else {
              LOGGER.warn(
                      "Config path '{}' is not a regular file or doesn't end with '{}'.",
                      configFilePath,
                      SUPPORTED_EXTENSIONS);
            }
          } else {
            if (!Files.isDirectory(configFilePath))
              LOGGER.warn("Config path '{}' doesn't end with a file extension.", configFilePath);
          }
        }
      }
    } else {
      sb.append(Files.readString(configPath));
    }
    return sb.toString();
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

  private void loadRemoteConfig() {
    String updateUrlPath = updateUrl;
    if (!updateUrlPath.endsWith("/")) {
      updateUrlPath += "/";
    }

    Map<String, String> configFileToUrl = new HashMap<>();
    for (String configFileName : CONFIG_FILES_NAMES) {
      configFileToUrl.put(configFileName, updateUrlPath + configFileName);
    }

    StringBuilder sb = new StringBuilder();
    Map<String, String> notExistingRemoteConfigFiles = new HashMap<>();
    for (Entry<String, String> configEntry : configFileToUrl.entrySet()) {
      var content = readRemoteConfigContent(configEntry);
      if (content.isBlank()) {
        notExistingRemoteConfigFiles.put(configEntry.getKey(), configEntry.getValue());
      } else {
        sb.append(content).append("\n");
      }
    }

    if (notExistingRemoteConfigFiles.size() == configFileToUrl.size()) {
      String msg =
              String.format(
                      "The remote configuration specified by the user does not contains any config files:"
                              + " %s",
                      notExistingRemoteConfigFiles.toString());
      LOGGER.error(msg);
      throw new IllegalStateException(msg);
    } else if (notExistingRemoteConfigFiles.size() > 0) {
      LOGGER.warn(
              "Missing remote config file(s): `{}`. Reading default file.",
              notExistingRemoteConfigFiles);
      for (Entry<String, String> entry : notExistingRemoteConfigFiles.entrySet()) {
        var fileContent = readDefaultConfigContent(entry.getKey());
        if (StringUtils.hasText(fileContent)) {
          sb.append(fileContent).append("\n");
        } else {
          LOGGER.warn("The `{}` file does not have a default configuration file.", entry.getKey());
        }
      }
    }
    var yaml = new Yaml();
    Map<String, Object> configuration = yaml.load(sb.toString());

    this.configurationData = populateConfiguration(configuration);
  }

  private String readRemoteConfigContent(Entry<String, String> configEntry) {
    var configContent = downloadYamlFileContent(configEntry.getValue());

    try {
      // We want to check if the input config content is a valid YAML
      var yaml = new Yaml();
      // We need to add 'foo: bar' because without that, YAML scanner may not raise exception for
      // incorrect input
      yaml.load(configContent + "\n\nfoo: bar");
    } catch (RuntimeException ex) {
      LOGGER.warn(
              "YAML config file '{}' from URL '{}' isn't correct. Ignoring it. YAML reading exception:"
                      + " {}",
              configEntry.getKey(),
              configEntry.getValue(),
              ex.getMessage());
      configContent = "";
    }
    if (!configContent.isEmpty()) // configContent != null && !configContent.isEmpty()
      overrideConfigContent(configEntry.getKey(), configContent);
    return configContent;
  }

  private String downloadYamlFileContent(String url) {
    Request request =
            new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader(ACCEPT_HEADER, YAML_MIME_TYPE)
                    .build();

    try (Response response = httpClient.newCall(request).execute()) {
      var responseCode = response.code();
      var responseBody = response.body().string();
      if (!response.isSuccessful()) {
        LOGGER.warn(
                "Request downloading configuration file from URL '{}' wasn't successful. "
                        + "The response ended with code {}",
                url,
                responseCode);

        return "";
      }

      return responseBody;
    } catch (Exception ex) {
      LOGGER.warn(
              "Exception occurred while handling configuration request from URL '{}' data.world"
                      + " describe query: {}",
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
      var configPath = fileSystemService.getPathToConfigDownloadDirectory();
      var configFilePath = configPath.resolve(configFileName);
      Files.write(
              configFilePath,
              configContent.getBytes(StandardCharsets.UTF_8),
              StandardOpenOption.CREATE,
              StandardOpenOption.WRITE,
              StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException ex) {
      LOGGER.warn(
              "Exception thrown while overriding configuration file '{}' with a new content.",
              configFileName);
      LOGGER.warn(ex.toString());
    }
  }

  private ConfigurationData populateConfiguration(Map<String, Object> configuration) {
    ConfigurationData configurationDataCandidate = readDefaultConfiguration();

    if (configuration != null) {
      for (Entry<String, Object> configEntry : configuration.entrySet()) {
        if (!ConfigurationKey.isDefined(configEntry.getKey())) {
          continue;
        }
        ConfigurationKey configKey = byName(configEntry.getKey());
        Object configEntryValue = configEntry.getValue();

        if (configEntryValue instanceof Map) {
          @SuppressWarnings("unchecked")
          var configMap = (Map<String, Object>) configEntryValue;

          switch (configKey) {
            case GROUPS_CONFIG:
            {
              GroupsConfig groupsConfig = handleGroupsConfig(configMap);
              configurationDataCandidate.setGroupsConfig(groupsConfig);
              break;
            }
            case LABEL_CONFIG:
            {
              LabelConfig labelConfig = handleLabelConfig(configMap);
              configurationDataCandidate.setLabelConfig(labelConfig);
              break;
            }
            case SEARCH_CONFIG:
            {
              SearchConfig searchConfig = handleSearchConfig(configMap);
              configurationDataCandidate.setSearchConfig(searchConfig);
              break;
            }
            case APPLICATION_CONFIG:
            {
              ApplicationConfig applicationConfig = handleApplicationConfig(configMap);
              configurationDataCandidate.setApplicationConfig(applicationConfig);
              break;
            }
            case ONTOLOGIES:
            {
              OntologiesConfig ontologiesConfig =
                      handleOntologyConfig(
                              configMap, configurationDataCandidate.getOntologiesConfig());
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
      if (!ontologiesConfig.getZipUrls().isEmpty()) {
        String downloadDirectory =
                ontologiesConfig.getDownloadDirectory().stream().findFirst().orElseGet(() -> { LOGGER.warn("use default 'download' directory"); return "download"; });
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

  private static Set<String> getNotExistingAndEmptyFiles(Set<Path> configFilePaths)
          throws IOException {
    Map<String, Boolean> validFiles = new HashMap<>();

    for (String configFilesName : CONFIG_FILES_NAMES) {
      validFiles.put(configFilesName, false);
      for (Path filesPath : configFilePaths) {
        var filesName = filesPath.getFileName().toString();
        if (filesName.equals(configFilesName)) {
          // empty config file is not valid config file
          if (StringUtils.hasText(Files.readString(filesPath)))
            validFiles.put(configFilesName, true);
        } else if (filesName.equals(TEST_FILE_NAME)) {
          for (String name : CONFIG_FILES_NAMES) {
            validFiles.put(name, true);
          }
        }
      }
    }
    return validFiles.entrySet().stream()
            .filter(f -> !f.getValue())
            .map(Entry::getKey)
            .collect(Collectors.toSet());
  }

  class ConfigChecklist {

    private boolean configPathIsSet = true;

    private boolean defaultConfigPathIsSet = true;

    private boolean remotePathIsSet = false;

    public void setConfigPathIsSet(boolean b) {
      this.configPathIsSet = b;
    }


    public void setDefaultConfigPathIsSet(boolean b) {
      this.defaultConfigPathIsSet = b;
    }
    
    public void setRemotePathIsSet(boolean b) {
      this.remotePathIsSet = b;
    }

    public boolean isConfigPathIsSet() {
      return configPathIsSet;
    }

    public boolean isDefaultConfigPathIsSet() {
      return defaultConfigPathIsSet;
    }

    public boolean isRemotePathIsSet() {
      return remotePathIsSet;
    }
  }
}