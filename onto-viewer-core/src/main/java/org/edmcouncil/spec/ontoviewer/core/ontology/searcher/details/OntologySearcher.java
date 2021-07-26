package org.edmcouncil.spec.ontoviewer.core.ontology.searcher.details;

import org.edmcouncil.spec.ontoviewer.core.exception.NotFoundElementInOntologyException;
import org.edmcouncil.spec.ontoviewer.core.exception.ViewerException;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlDetails;
import org.edmcouncil.spec.ontoviewer.core.ontology.DetailsManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.ViewerSearcher;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.SearcherResult;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.result.ResultFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class OntologySearcher implements ViewerSearcher {

  private final DetailsManager detailsManager;

  public OntologySearcher(DetailsManager detailsManager) {
    this.detailsManager = detailsManager;
  }

  public SearcherResult search(String query, Integer max) throws NotFoundElementInOntologyException {
    OwlDetails owd = detailsManager.getDetailsByIri(query);

    return ResultFactory.createDetailsResult(owd);
  }

  @Override
  public SearcherResult search(String query, Integer max, Integer currentPage) throws ViewerException {
    OwlDetails owd = detailsManager.getDetailsByIri(query);

    return ResultFactory.createDetailsResult(owd);
  }
}
