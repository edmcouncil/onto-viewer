package org.edmcouncil.spec.ontoviewer.toolkit.options;

public enum OptionDefinition {
  INPUT("input", true, true, "path to the input ontology"),
  OUTPUT("output", true, true,
      "path where the result will be saved");

  private final String argName;
  private final boolean hasArg;
  private final boolean required;
  private final String description;

  OptionDefinition(String argName, boolean hasArg, boolean required, String description) {
    this.argName = argName;
    this.hasArg = hasArg;
    this.required = required;
    this.description = description;
  }

  public String argName() {
    return argName;
  }

  public boolean hasArg() {
    return hasArg;
  }

  public boolean isRequired() {
    return required;
  }

  public String description() {
    return description;
  }
}