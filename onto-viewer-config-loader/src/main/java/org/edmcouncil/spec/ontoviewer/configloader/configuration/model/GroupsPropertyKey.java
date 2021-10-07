package org.edmcouncil.spec.ontoviewer.configloader.configuration.model;

public enum GroupsPropertyKey {

  DEFINITION("definition"),
  EXAMPLE("example"),
  EXPLANATORY_NOTE("explanatory note"),
  GENERATED_DESCRIPTION("generated description"),
  GLOSSARY("Glossary"),
  SYNONYM("synonym");

  private final String key;

  GroupsPropertyKey(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
