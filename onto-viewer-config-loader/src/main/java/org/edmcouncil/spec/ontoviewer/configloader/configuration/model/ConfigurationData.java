package org.edmcouncil.spec.ontoviewer.configloader.configuration.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConfigurationData {

  private GroupsConfig groupsConfig;
  private LabelConfig labelConfig;
  private SearchConfig searchConfig;
  private OntologiesConfig ontologiesConfig;
  private ApplicationConfig applicationConfig;
  private IntegrationsConfig integrationsConfig = new IntegrationsConfig(Collections.emptyMap());
  private ToolkitConfig toolkitConfig = new ToolkitConfig();

  public GroupsConfig getGroupsConfig() {
    return groupsConfig;
  }

  public void setGroupsConfig(GroupsConfig groupsConfig) {
    this.groupsConfig = groupsConfig;
  }

  public LabelConfig getLabelConfig() {
    return labelConfig;
  }

  public void setLabelConfig(LabelConfig labelConfig) {
    this.labelConfig = labelConfig;
  }

  public SearchConfig getSearchConfig() {
    return searchConfig;
  }

  public void setSearchConfig(SearchConfig searchConfig) {
    this.searchConfig = searchConfig;
  }

  public OntologiesConfig getOntologiesConfig() {
    return ontologiesConfig;
  }

  public void setOntologiesConfig(OntologiesConfig ontologiesConfig) {
    this.ontologiesConfig = ontologiesConfig;
  }

  public ApplicationConfig getApplicationConfig() {
    return applicationConfig;
  }

  public void setApplicationConfig(ApplicationConfig applicationConfig) {
    this.applicationConfig = applicationConfig;
  }

  public ToolkitConfig getToolkitConfig() {
    return toolkitConfig;
  }

  public void setToolkitConfig(ToolkitConfig toolkitConfig) {
    this.toolkitConfig = toolkitConfig;
  }

  public IntegrationsConfig getIntegrationsConfig() {
    return integrationsConfig;
  }

  public void setIntegrationsConfig(IntegrationsConfig integrationsConfig) {
    this.integrationsConfig = integrationsConfig;
  }

  public static class ApplicationConfig {

    private final List<String> license;
    private final List<String> copyright;
    private final boolean displayQName;
    private final boolean displayLicense;
    private final boolean displayCopyright;

    public ApplicationConfig(List<String> license, List<String> copyright, boolean displayLicense,
        boolean displayCopyright, boolean displayQName) {
      this.license = license;
      this.copyright = copyright;
      this.displayLicense = displayLicense;
      this.displayCopyright = displayCopyright;
      this.displayQName = displayQName;
    }

    public List<String> getLicense() {
      return license;
    }

    public List<String> getCopyright() {
      return copyright;
    }

    public boolean isDisplayLicense() {
      return displayLicense;
    }

    public boolean isDisplayCopyright() {
      return displayCopyright;
    }

    public boolean isDisplayQName() {
      return displayQName;
    }
  }

  public static class GroupsConfig {

    private final List<String> priorityList;
    private final Map<String, List<String>> groups;

    public GroupsConfig(List<String> priorityList, Map<String, List<String>> groups) {
      this.priorityList = priorityList;
      this.groups = groups;
    }

    public List<String> getPriorityList() {
      return priorityList;
    }

    public Map<String, List<String>> getGroups() {
      return groups;
    }
  }

  public static class LabelConfig {

    private boolean displayLabel;
    private LabelPriority labelPriority;
    private boolean forceLabelLang;
    private String labelLang;
    private MissingLanguageAction missingLanguageAction;
    private List<UserDefaultName> defaultNames;

    public LabelConfig(boolean displayLabel, LabelPriority labelPriority, boolean forceLabelLang, String labelLang,
        MissingLanguageAction missingLanguageAction, List<UserDefaultName> defaultNames) {
      this.displayLabel = displayLabel;
      this.labelPriority = labelPriority;
      this.forceLabelLang = forceLabelLang;
      this.labelLang = labelLang;
      this.missingLanguageAction = missingLanguageAction;
      this.defaultNames = defaultNames;
    }

    public boolean isDisplayLabel() {
      return displayLabel;
    }

    public void setDisplayLabel(boolean displayLabel) {
      this.displayLabel = displayLabel;
    }

    public LabelPriority getLabelPriority() {
      return labelPriority;
    }

    public void setLabelPriority(LabelPriority labelPriority) {
      this.labelPriority = labelPriority;
    }

    public boolean isForceLabelLang() {
      return forceLabelLang;
    }

    public void setForceLabelLang(boolean forceLabelLang) {
      this.forceLabelLang = forceLabelLang;
    }

    public String getLabelLang() {
      return labelLang;
    }

    public void setLabelLang(String labelLang) {
      this.labelLang = labelLang;
    }

    public MissingLanguageAction getMissingLanguageAction() {
      return missingLanguageAction;
    }

    public void setMissingLanguageAction(MissingLanguageAction missingLanguageAction) {
      this.missingLanguageAction = missingLanguageAction;
    }

    public List<UserDefaultName> getDefaultNames() {
      return defaultNames;
    }

    public void setDefaultNames(List<UserDefaultName> defaultNames) {
      this.defaultNames = defaultNames;
    }
  }

  public static class SearchConfig {

    private final List<String> searchDescriptions;
    private final int fuzzyDistance;
    private final boolean reindexOnStart;
    private final List<FindProperty> findProperties;

    public SearchConfig(List<String> searchDescriptions, int fuzzyDistance, boolean reindexOnStart,
        List<FindProperty> findProperties) {
      this.searchDescriptions = searchDescriptions;
      this.fuzzyDistance = fuzzyDistance;
      this.reindexOnStart = reindexOnStart;
      this.findProperties = findProperties;
    }

    public List<String> getSearchDescriptions() {
      return searchDescriptions;
    }

    public int getFuzzyDistance() {
      return fuzzyDistance;
    }

    public boolean isReindexOnStart() {
      return reindexOnStart;
    }

    public List<FindProperty> getFindProperties() {
      return findProperties;
    }
  }

  public static class OntologiesConfig {

    private final List<String> urls;
    private final List<String> paths;
    private final List<String> catalogPaths;
    private final List<String> moduleIgnorePatterns;
    private final List<String> moduleToIgnore;
    private final Map<String, String> ontologyMappings;
    private boolean automaticCreationOfModules;
    private final List<String> downloadDirectory;
    private final List<String> zipUrls;
    private final String moduleClassIri;
    private List<Pair> maturityLevelDefinition;

    public OntologiesConfig(List<String> urls, List<String> paths, List<String> catalogPaths,
        List<String> downloadDirectory, List<String> zipUrls, List<String> moduleIgnorePatterns,
        List<String> moduleToIgnore, List<Pair> maturityLevelDefinition, boolean automaticCreationOfModules,
        String moduleClassIri) {
      this.urls = urls;
      this.paths = paths;
      this.catalogPaths = catalogPaths;
      this.moduleIgnorePatterns = moduleIgnorePatterns;
      this.moduleToIgnore = moduleToIgnore;
      this.maturityLevelDefinition = maturityLevelDefinition;
      this.ontologyMappings = new HashMap<>();
      this.automaticCreationOfModules = automaticCreationOfModules;
      this.downloadDirectory = downloadDirectory;
      this.zipUrls = zipUrls;
      this.moduleClassIri = moduleClassIri;
    }

    public List<String> getUrls() {
      return urls;
    }

    public List<String> getPaths() {
      return paths;
    }

    public List<String> getCatalogPaths() {
      return catalogPaths;
    }

    public List<String> getModuleIgnorePatterns() {
      return moduleIgnorePatterns;
    }

    public List<String> getModuleToIgnore() {
      return moduleToIgnore;
    }

    public List<Pair> getMaturityLevelDefinition() {
      return maturityLevelDefinition;
    }

    public void setMaturityLevelDefinition(List<Pair> maturityLevelDefinition) {
      this.maturityLevelDefinition = maturityLevelDefinition;
    }

    public Map<String, String> getOntologyMappings() {
      return ontologyMappings;
    }

    public boolean getAutomaticCreationOfModules() {
      return automaticCreationOfModules;
    }

    public void setAutomaticCreationOfModules(boolean automaticCreationOfModules) {
      this.automaticCreationOfModules = automaticCreationOfModules;
    }

    public List<String> getDownloadDirectory() {
      return downloadDirectory;
    }

    public List<String> getZipUrls() {
      return zipUrls;
    }

    public String getModuleClassIri() {
      return moduleClassIri;
    }
  }

  public static class ToolkitConfig {

    private boolean runningToolkit;
    private String filterPattern;
    private String goal;
    private boolean locationInModulesEnabled;
    private boolean usageEnabled;
    private boolean ontologyGraphEnabled;
    private boolean individualsEnabled;
    private Map<String, List<String>> extractDataColumns;

    public ToolkitConfig() {
      this.runningToolkit = false;
      this.locationInModulesEnabled = true;
      this.usageEnabled = true;
      this.ontologyGraphEnabled = true;
      this.individualsEnabled = true;
      this.extractDataColumns = new HashMap<>();
    }

    public boolean isRunningToolkit() {
      return runningToolkit;
    }

    public void setRunningToolkit(boolean runningToolkit) {
      this.runningToolkit = runningToolkit;
    }

    public String getFilterPattern() {
      return filterPattern;
    }

    public void setFilterPattern(String filterPattern) {
      this.filterPattern = filterPattern;
    }

    public String getGoal() {
      return goal;
    }

    public void setGoal(String goal) {
      this.goal = goal;
    }

    public boolean isLocationInModulesEnabled() {
      return locationInModulesEnabled;
    }

    public void setLocationInModulesEnabled(boolean locationInModulesEnabled) {
      this.locationInModulesEnabled = locationInModulesEnabled;
    }

    public boolean isUsageEnabled() {
      return usageEnabled;
    }

    public void setUsageEnabled(boolean usageEnabled) {
      this.usageEnabled = usageEnabled;
    }

    public boolean isOntologyGraphEnabled() {
      return ontologyGraphEnabled;
    }

    public void setOntologyGraphEnabled(boolean ontologyGraphEnabled) {
      this.ontologyGraphEnabled = ontologyGraphEnabled;
    }

    public boolean isIndividualsEnabled() {
      return individualsEnabled;
    }

    public void setIndividualsEnabled(boolean individualsEnabled) {
      this.individualsEnabled = individualsEnabled;
    }

    public Map<String, List<String>> getExtractDataColumns() {
      return extractDataColumns;
    }

    public void setExtractDataColumns(Map<String, List<String>> extractDataColumns) {
      this.extractDataColumns = extractDataColumns;
    }

  }

  public static class IntegrationsConfig {

    private Map<String, Integration> integrations;

    public IntegrationsConfig(Map<String, Integration> integrations) {
      this.integrations = integrations;
    }

    public Map<String, Integration> getIntegrations() {
      return integrations;
    }

    public void setIntegrations(Map<String, Integration> integrations) {
      this.integrations = integrations;
    }

    public Optional<Integration> getIntegration(String id) {
      var integration = integrations.get(id);
      if (integration == null) {
        return Optional.empty();
      }
      return Optional.of(integration);
    }

    public Optional<Integration> getIntegrationIgnoreCase(String id) {
      var keyOptional = integrations.keySet()
          .stream()
          .filter(key -> key.equalsIgnoreCase(id))
          .findFirst();

      return keyOptional.map(s -> integrations.get(s));
    }
  }

  public static class Integration {

    private String id;
    private String url;
    private String accessToken;

    public Integration(String id, String url, String accessToken) {
      this.id = id;
      this.url = url;
      this.accessToken = accessToken;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getAccessToken() {
      return accessToken;
    }

    public void setAccessToken(String accessToken) {
      this.accessToken = accessToken;
    }
  }
}
