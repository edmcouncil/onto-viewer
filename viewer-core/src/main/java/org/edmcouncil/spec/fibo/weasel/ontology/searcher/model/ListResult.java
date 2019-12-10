package org.edmcouncil.spec.fibo.weasel.ontology.searcher.model;

import java.util.List;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class ListResult extends SearcherResult<List<SearchItem>> {

  public ListResult(Type type, List<SearchItem> result) {
    super(type, result);
  }

}
