package org.edmcouncil.spec.ontoviewer.webapp.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class FindResults {

  private final int page;
  private final int pageSize;
  private final int totalHits;
  private final List<FindResult> results;

  public static FindResults empty() {
    return new FindResults(0, 0, 0, new ArrayList<>(0));
  }

  public FindResults(int page, int pageSize, int totalHits, List<FindResult> results) {
    this.page = page;
    this.pageSize = pageSize;
    this.totalHits = totalHits;
    this.results = results;
  }

  public int getPage() {
    return page;
  }

  public int getPageSize() {
    return pageSize;
  }

  public int getTotalHits() {
    return totalHits;
  }

  public List<FindResult> getResults() {
    return results;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FindResults)) {
      return false;
    }
    FindResults that = (FindResults) o;
    return page == that.page && pageSize == that.pageSize && totalHits == that.totalHits && Objects.equals(
        results, that.results);
  }

  @Override
  public int hashCode() {
    return Objects.hash(page, pageSize, totalHits, results);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", FindResults.class.getSimpleName() + "[", "]")
        .add("page=" + page)
        .add("pageSize=" + pageSize)
        .add("totalHits=" + totalHits)
        .add("results=" + results)
        .toString();
  }
}