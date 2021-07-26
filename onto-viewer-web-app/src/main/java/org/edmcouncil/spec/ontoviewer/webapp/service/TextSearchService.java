package org.edmcouncil.spec.ontoviewer.webapp.service;

import java.util.List;
import org.edmcouncil.spec.ontoviewer.core.exception.ViewerException;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.SearcherResult;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.hint.HintItem;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.text.TextSearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class TextSearchService   {

  @Autowired
  private TextSearcher searcher;

  public SearcherResult search(String query, Integer maxValues, Integer currentPage) throws ViewerException {
    return searcher.search(query, maxValues, currentPage);
  }

  public List<HintItem> getHints(String query, Integer maxHintCount) {
    return searcher.getHints(query, maxHintCount);
  }

  public SearcherResult search(String query, int maxValues) throws ViewerException {
    return searcher.search(query, maxValues, 1);
  }
}