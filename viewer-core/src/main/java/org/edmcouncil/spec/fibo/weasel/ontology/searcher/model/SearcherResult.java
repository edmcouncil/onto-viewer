package org.edmcouncil.spec.fibo.weasel.ontology.searcher.model;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public abstract class SearcherResult {

  private Type type;

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  enum Type {
    list, element
  }
}
