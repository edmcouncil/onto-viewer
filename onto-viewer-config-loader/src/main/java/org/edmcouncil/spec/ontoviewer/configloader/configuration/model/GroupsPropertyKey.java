package org.edmcouncil.spec.ontoviewer.configloader.configuration.model;

public enum GroupsPropertyKey {

  DEFINITION("definition"),
  EXAMPLE("example"),
  EXPLANATORY_NOTE("explanatory note"),
  GENERATED_DESCRIPTION("generated description"),
  GLOSSARY("Glossary"),
  ONTOLOGICAL_CHARACTERISTIC("Ontological characteristic"),
  SYNONYM("synonym"),
  THIS_ONTOLOGY_CONTAINS("This ontology contains");

  private final String key;

  GroupsPropertyKey(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
