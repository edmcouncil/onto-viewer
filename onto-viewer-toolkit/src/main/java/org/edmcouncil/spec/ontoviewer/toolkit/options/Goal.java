package org.edmcouncil.spec.ontoviewer.toolkit.options;

public enum Goal {

  CONSISTENCY_CHECK("consistency-check"),
  EXTRACT_DATA("extract-data"),
  MERGE_IMPORTS("merge-imports");

  private final String name;

  Goal(String name) {
    this.name = name;
  }

  public static Goal byName(String goal) {
    for (Goal value : values()) {
      if (value.getName().equals(goal)) {
        return value;
      }
    }

    throw new IllegalArgumentException(
        String.format("Unable to find goal '%s' within the defined goals.", goal));
  }

  public String getName() {
    return name;
  }
}
