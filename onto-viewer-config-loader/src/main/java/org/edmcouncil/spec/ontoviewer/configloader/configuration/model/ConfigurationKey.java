package org.edmcouncil.spec.ontoviewer.configloader.configuration.model;

public enum ConfigurationKey {

  // Groups Config
  GROUPS_CONFIG("groups_config"),
  PRIORITY_LIST("priority_list"),
  GROUPS("groups"),
  NAME("name"),
  ITEMS("items"),
  // Label Config
  LABEL_CONFIG("label_config"),
  DISPLAY_LABEL("display_label"),
  LABEL_PRIORITY("label_priority"),
  FORCE_LABEL_LANG("force_label_lang"),
  LABEL_LANG("label_lang"),
  MISSING_LANGUAGE_ACTION("missing_language_action"),
  USER_DEFAULT_NAME_LIST("user_default_name_list"),
  ID("id"),
  // Search Config
  SEARCH_CONFIG("search_config"),
  SEARCH_DESCRIPTIONS("search_descriptions"),
  FUZZY_DISTANCE("fuzzy_distance"),
  REINDEX_ON_START("reindex_on_start"),
  FIND_PROPERTIES("find_properties"),
  LABEL("label"),
  IDENTIFIER("identifier"),
  IRI("iri"),
  // Ontology Config
  ONTOLOGIES("ontologies"),
  SOURCE("source"),
  PATH("path"),
  URL("url"),
  ZIP("zip"),
  DOWNLOAD_DIRECTORY("download_directory"),
  CATALOG_PATH("catalog_path"),
  MODULE_IGNORE_PATTERN("module_ignore_pattern"),
  MODULE_TO_IGNORE("module_to_ignore"),
  AUTOMATIC_CREATION_OF_MODULES("automatic_creation_of_modules"),
  MODULE_CLASS_IRI("module_class_iri"),
  MATURITY_LEVEL_DEFINITION("maturity_level_definition"),
  MATURITY_LEVEL_PROPERTY("maturity_level_property"),
  //ApplicationConfig
  APPLICATION_CONFIG("application_config"),
  LICENSE("license"),
  COPYRIGHT("copyright"),
  DISPLAY_LICENSE("display_license"),
  DISPLAY_COPYRIGHT("display_copyright"),
  DISPLAY_QNAME("display_qname"),
  // Integration Config
  INTEGRATIONS("integrations"),
  INTEGRATION("integration"),
  INTEGRATION_ID("id"),
  INTEGRATION_URL("url"),
  INTEGRATION_ACCESS_TOKEN("accessToken");

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

  public static boolean isDefined(String name) {
    for (ConfigurationKey configurationKey : values()) {
      if (configurationKey.label.equals(name)) {
        return true;
      }
    }

    return false;
  }
}
