package org.edmcouncil.spec.fibo.weasel.ontology.searcher.details;

import org.edmcouncil.spec.fibo.weasel.exception.NotFoundElementInOntologyException;
import org.edmcouncil.spec.fibo.weasel.exception.ViewerException;
import org.edmcouncil.spec.fibo.weasel.model.details.OwlDetails;
import org.edmcouncil.spec.fibo.weasel.ontology.DetailsManager;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.ViewerSearcher;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.SearcherResult;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.result.ResultFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class OntologySearcher implements ViewerSearcher {

  @Autowired
  private DetailsManager detailsManager;

  public SearcherResult search(String query, Integer max) throws NotFoundElementInOntologyException {

    OwlDetails owd = detailsManager.getDetailsByIri(query);

    return ResultFactory.cresteDetailsResult(owd);
  }

  @Override
  public SearcherResult search(String query, Integer max, Integer currentPage) throws ViewerException {
    OwlDetails owd = detailsManager.getDetailsByIri(query);

    return ResultFactory.cresteDetailsResult(owd);
  }

}
