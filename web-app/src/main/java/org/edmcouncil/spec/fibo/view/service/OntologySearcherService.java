package org.edmcouncil.spec.fibo.view.service;

import org.edmcouncil.spec.fibo.config.configuration.model.AppConfiguration;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ViewerCoreConfiguration;
import org.edmcouncil.spec.fibo.view.util.ModelBuilder;
import org.edmcouncil.spec.fibo.weasel.exception.NotFoundElementInOntologyException;
import org.edmcouncil.spec.fibo.weasel.exception.ViewerException;
import org.edmcouncil.spec.fibo.weasel.model.details.OwlDetails;
import org.edmcouncil.spec.fibo.weasel.ontology.DetailsManager;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.details.OntologySearcher;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.SearcherResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Service
public class OntologySearcherService {

  private static final Logger LOG = LoggerFactory.getLogger(OntologySearcherService.class);

  @Autowired
  private DetailsManager ontologyManager;

  @Autowired
  private OntologySearcher ontologySearcher;

  public SearcherResult search(String query, int maxValues) throws ViewerException {

    return ontologySearcher.search(query, maxValues);

  }

}
