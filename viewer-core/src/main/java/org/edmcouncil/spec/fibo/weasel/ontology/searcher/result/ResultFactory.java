package org.edmcouncil.spec.fibo.weasel.ontology.searcher.result;

import org.edmcouncil.spec.fibo.weasel.model.details.OwlDetails;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.DetailsResult;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.ExtendedResult;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.ListResult;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.SearcherResult;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class ResultFactory {

  public static SearcherResult cresteSearchResult(ExtendedResult result) {

    return new ListResult(SearcherResult.Type.list, result);
  }

  public static SearcherResult cresteDetailsResult(OwlDetails result) {
    // SearcherResult.Type.details.
    return new DetailsResult(SearcherResult.Type.details, result);
  }
  
}
