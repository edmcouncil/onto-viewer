package org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class ExtendedResult {

  private List<SearchItem> result;
  private Integer page;
  private boolean hasMorePage;
  private String query;
  private Integer maxPage;
  private Integer totalResult;

  public ExtendedResult() {
    result = new ArrayList<>(0);
  }

  public List<SearchItem> getResult() {
    return result;
  }

  public void setResult(List<SearchItem> result) {
    this.result = result;
  }

  public Integer getPage() {
    return page;
  }

  public void setPage(Integer page) {
    this.page = page;
  }

  public boolean hasMorePage() {
    return hasMorePage;
  }

  public void setHasMorePage(boolean hasMorePage) {
    this.hasMorePage = hasMorePage;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public void setMaxPage(Integer i) {
    this.maxPage = i;
  }

  public Integer getMaxPage() {
    return maxPage;
  }

  public Integer getTotalResult() {
    return totalResult;
  }

  public void setTotalResult(Integer i) {
    this.totalResult = i;
  }

  @Override
  public String toString() {
    return "{" + result + ", page=" + page + ", hasMorePage=" + hasMorePage + ", query=" + query + ", maxPage=" + maxPage + '}';
  }
}
