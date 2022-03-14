package org.edmcouncil.spec.ontoviewer.configloader.configuration.model;

import java.util.List;
import java.util.Map;

public class ConfigurationData {

  private GroupsConfig groupsConfig;
  private SearchConfig searchConfig;
  private OntologiesConfig ontologiesConfig;

  public void setGroupsConfig(GroupsConfig groupsConfig) {
    this.groupsConfig = groupsConfig;
  }

  public void setSearchConfig(SearchConfig searchConfig) {
    this.searchConfig = searchConfig;
  }

  public void setOntologiesConfig(OntologiesConfig ontologiesConfig) {
    this.ontologiesConfig = ontologiesConfig;
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

    public OntologiesConfig(List<String> urls, List<String> paths) {
      this.urls = urls;
      this.paths = paths;
    }

    public List<String> getUrls() {
      return urls;
    }

    public List<String> getPaths() {
      return paths;
    }
  }
}

