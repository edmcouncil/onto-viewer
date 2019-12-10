package org.edmcouncil.spec.fibo.weasel.ontology.searcher.model;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @param <T> Type of result
 */
public abstract class SearcherResult<T> {

  private final Type type;
  private final T result;

  public SearcherResult(Type type, T result) {
    this.type = type;
    this.result = result;
  }

  public Type getType() {
    return type;
  }

  public T getResult() {
    return result;
  }

  public static enum Type {
    list, details
  }
}
