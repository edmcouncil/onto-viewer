package org.edmcouncil.spec.fibo.view.service;

import org.edmcouncil.spec.fibo.weasel.exception.ViewerException;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.details.OntologySearcher;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.SearcherResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Service
public class OntologySearcherService {

  private static final Logger LOG = LoggerFactory.getLogger(OntologySearcherService.class);

  private final OntologySearcher ontologySearcher;

  public OntologySearcherService(OntologySearcher ontologySearcher) {
    this.ontologySearcher = ontologySearcher;
  }

  public SearcherResult search(String query, int maxValues) throws ViewerException {
    return ontologySearcher.search(query, maxValues);
  }
}
