package org.edmcouncil.spec.ontoviewer.core.ontology.searcher.result;

import java.util.List;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlDetails;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.DetailsResult;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.ExtendedResult;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.ListResult;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.SearchItem;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.SearcherResult;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class ResultFactory {

  private ResultFactory() {
  }

  public static SearcherResult<List<SearchItem>> createSearchResult(ExtendedResult result) {
    return new ListResult(SearcherResult.Type.list, result);
  }

  public static <T extends OwlDetails> SearcherResult<T> createDetailsResult(T result) {
    return new DetailsResult<>(SearcherResult.Type.details, result);
  }
}