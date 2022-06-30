package org.edmcouncil.spec.ontoviewer.toolkit.options;

public enum OptionDefinition {
  DATA("data", true, false, "path to the input ontology(ies)"),
  OUTPUT("output", true, false, "path where the result will be saved"),
  FILTER_PATTERN("filter-pattern", true, false,
      "string that should be within entity's IRI to include it", ""),
  GOAL("goal", true, false,
    "specify which goal should be executed by toolkit", Goal.EXTRACT_DATA.getName()),
  ONTOLOGY_IRI("ontology-iri", true, false, "new IRI for merged ontology"),
  ONTOLOGY_MAPPING("ontology-mapping", true, false,
      "path to the catalog file with ontology mapping", null),
  VERSION("version", false, false, "displays version info", null);

  private final String argName;
  private final boolean hasArg;
  private final boolean required;
  private final String description;
  private final String defaultValue;

  OptionDefinition(String argName, boolean hasArg, boolean required, String description) {
    this(argName, hasArg, required, description, null);
  }

  OptionDefinition(String argName, boolean hasArg, boolean required, String description,
      String defaultValue) {
    this.argName = argName;
    this.hasArg = hasArg;
    this.required = required;
    this.description = description;
    this.defaultValue = defaultValue;
  }

  public String argName() {
    return argName;
  }

  public boolean hasArg() {
    return hasArg;
  }

  public boolean isNotRequired() {
    return !isRequired();
  }

  public boolean isRequired() {
    return required;
  }

  public String description() {
    return description;
  }

  public String defaultValue() {
    return defaultValue;
  }

  public boolean hasDefaultValue() {
    return defaultValue != null;
  }
}