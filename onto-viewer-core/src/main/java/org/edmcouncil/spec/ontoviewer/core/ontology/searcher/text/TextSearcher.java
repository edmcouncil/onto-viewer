package org.edmcouncil.spec.ontoviewer.core.ontology.searcher.text;

import java.util.List;
import org.edmcouncil.spec.ontoviewer.core.exception.ViewerException;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.ViewerSearcher;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.ExtendedResult;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.SearcherResult;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.hint.HintItem;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.result.ResultFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class TextSearcher implements ViewerSearcher {

  @Autowired
  private TextSearcherDb db;

  @Override
  public SearcherResult search(String query, Integer maxResultCount, Integer currentPage) throws ViewerException {

    ExtendedResult result = db.getSearchResult(query, maxResultCount, currentPage);
    
    return ResultFactory.createSearchResult(result);
  }

  public List<HintItem> getHints(String query, Integer maxHintCount) {
    
    List<HintItem> result = db.getHints(query, maxHintCount);

    return result;
  }

}
