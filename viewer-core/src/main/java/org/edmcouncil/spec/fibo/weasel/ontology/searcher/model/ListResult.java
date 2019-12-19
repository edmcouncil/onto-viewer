package org.edmcouncil.spec.fibo.weasel.ontology.searcher.model;

import java.util.List;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class ListResult extends SearcherResult<List<SearchItem>> {

  private String query;
  private Integer page;
  private Boolean hasMore;
  private Integer maxPage;

  public ListResult(Type type, List<SearchItem> result) {
    super(type, result);
  }

  public ListResult(Type type, ExtendedResult result) {
    super(type, result.getResult());
    this.query = result.getQuery();
    this.page = result.getPage();
    this.hasMore = result.hasMorePage();
    this.maxPage = result.getMaxPage();
  }

  public String getQuery() {
    return query;
  }

  public Integer getPage() {
    return page;
  }

  public Boolean getHasMore() {
    return hasMore;
  }

  public Integer getMaxPage() {
    return maxPage;
  }

}
