package org.edmcouncil.spec.fibo.view.service;

import java.util.List;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.SearcherResult;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.hint.HintItem;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.text.TextSearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class TextSearchService implements SearchService {

  @Autowired
  private TextSearcher searcher;

  @Override
  public SearcherResult search(String query, int maxValues) {
    return searcher.search(query, maxValues);
  }

  public List<HintItem> getHints(String query, Integer maxHintCount) {
    return searcher.getHints(query, maxHintCount);
  }

}
