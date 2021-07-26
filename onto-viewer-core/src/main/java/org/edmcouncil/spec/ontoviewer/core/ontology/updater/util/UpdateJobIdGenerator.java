package org.edmcouncil.spec.ontoviewer.core.ontology.updater.util;

public class UpdateJobIdGenerator {

  private static long id = 0;

  public static long last() {
    return id;
  }

  void setMinId(long newId) {
    this.id = newId;
  }

  public static long nextId() {
    return id++;
  }

  public static String nextStringId() {
    long tmp = id++;
    return String.valueOf(tmp);
  }

}
