package org.edmcouncil.spec.ontoviewer.configloader.configuration.model;

public enum ConfigurationKey {

  GROUPS_CONFIG("groups_config"),
  PRIORITY_LIST("priority_list"),
  GROUPS("groups"),
  NAME("name"),
  ITEMS("items"),
  SEARCH_CONFIG("search_config"),
  SEARCH_DESCRIPTIONS("search_descriptions"),
  FUZZY_DISTANCE("fuzzy_distance"),
  REINDEX_ON_START("reindex_on_start"),
  FIND_PROPERTIES("find_properties"),
  LABEL("label"),
  IDENTIFIER("identifier"),
  IRI("iri"),
  ONTOLOGIES("ontologies"),
  PATH("path"),
  URL("url");

  private final String label;

  ConfigurationKey(String label) {
    this.label = label;
  }

  public String getLabel() {
    return label;
  }

  public static ConfigurationKey byName(String name) {
    for (ConfigurationKey configurationKey : values()) {
      if (configurationKey.label.equals(name)) {
        return configurationKey;
      }
    }

    throw new IllegalArgumentException(String.format("ConfigurationName enum with name '%s' not found.", name));
  }
}
