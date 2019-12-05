package org.edmcouncil.spec.fibo.weasel.ontology.searcher.model;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @param <T> Type of result
 */
public abstract class SearcherResult<T> {

  private Type type;
  private T result;

  public Type getType() {
    return type;
  }

  public T getResult() {
    return result;
  }

  enum Type {
    list, details
  }
}
