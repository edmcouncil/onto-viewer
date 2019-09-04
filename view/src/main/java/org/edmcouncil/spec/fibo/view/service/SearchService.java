package org.edmcouncil.spec.fibo.view.service;

import java.util.Collection;
import org.edmcouncil.spec.fibo.config.configuration.model.AppConfiguration;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.WeaselConfiguration;
import org.edmcouncil.spec.fibo.view.util.ModelBuilder;
import org.edmcouncil.spec.fibo.weasel.ontology.WeaselOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Service
public class SearchService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SearchService.class);

  @Autowired
  private WeaselOntologyManager ontologyManager;
  @Autowired
  private AppConfiguration config;

  public void search(String query, ModelBuilder mb) {
    Collection weaselTerms = ontologyManager.getDetailsByIri(query);
    boolean isGrouped = ((WeaselConfiguration) config.getWeaselConfig()).isGrouped();
    mb.setQuery(query)
        .ontoDetails(weaselTerms)
        .isGrouped(isGrouped);
    
  }

}
