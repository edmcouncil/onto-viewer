package org.edmcouncil.spec.fibo.weasel.ontology.searcher.model;

import org.edmcouncil.spec.fibo.weasel.model.details.OwlDetails;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class DetailsResult extends SearcherResult<OwlDetails>{
  
  public DetailsResult(Type type, OwlDetails result) {
    super(type, result);
  }
  
}
