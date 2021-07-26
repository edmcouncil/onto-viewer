package org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model;

import org.edmcouncil.spec.ontoviewer.core.model.details.OwlDetails;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class DetailsResult extends SearcherResult<OwlDetails> {
  
  public DetailsResult(Type type, OwlDetails result) {
    super(type, result);
  }
}
