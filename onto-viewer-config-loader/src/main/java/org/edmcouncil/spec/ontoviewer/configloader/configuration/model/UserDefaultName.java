package org.edmcouncil.spec.ontoviewer.configloader.configuration.model;

public class UserDefaultName {

  private final String id;
  private final String name;

  public UserDefaultName(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
