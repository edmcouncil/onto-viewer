package org.edmcouncil.spec.fibo.view.service;

import org.edmcouncil.spec.fibo.weasel.ontology.DataManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class TextSearchService implements SearchService {

  @Autowired
  private DataManager ontologyManager;

  @Override
  public Object search(String query) {
    
    
    
    return null;
  }

}
