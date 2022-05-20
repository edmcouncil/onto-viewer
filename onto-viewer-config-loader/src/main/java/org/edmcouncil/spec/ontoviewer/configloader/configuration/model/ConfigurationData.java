package org.edmcouncil.spec.ontoviewer.configloader.configuration.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationData {

  private GroupsConfig groupsConfig;
  private LabelConfig labelConfig;
  private SearchConfig searchConfig;
  private OntologiesConfig ontologiesConfig;
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

  public ToolkitConfig getToolkitConfig() {
    return toolkitConfig;
  }

  public void setToolkitConfig(ToolkitConfig toolkitConfig) {
    this.toolkitConfig = toolkitConfig;
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
        MissingLanguageAction missingLanguageAction,
        List<UserDefaultName> defaultNames) {
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

    public OntologiesConfig(List<String> urls,
        List<String> paths,
        List<String> catalogPaths,
        List<String> downloadDirectory,
        List<String> zipUrls,
        List<String> moduleIgnorePatterns,
        List<String> moduleToIgnore,
        boolean automaticCreationOfModules ) {
      this.urls = urls;
      this.paths = paths;
      this.catalogPaths = catalogPaths;
      this.moduleIgnorePatterns = moduleIgnorePatterns;
      this.moduleToIgnore = moduleToIgnore;
      this.ontologyMappings = new HashMap<>();
      this.automaticCreationOfModules = automaticCreationOfModules;
      this.downloadDirectory = downloadDirectory;
      this.zipUrls = zipUrls;
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
  }

  public static class ToolkitConfig {

    private String filterPattern;
    private String goal;
    private boolean locationInModulesEnabled;
    private boolean usageEnabled;
    private boolean ontologyGraphEnabled;
    private boolean individualsEnabled;

    public ToolkitConfig() {
      this.locationInModulesEnabled = true;
      this.usageEnabled = true;
      this.ontologyGraphEnabled = true;
      this.individualsEnabled = true;
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
  }
}