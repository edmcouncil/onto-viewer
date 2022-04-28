package org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model;

import org.edmcouncil.spec.ontoviewer.core.model.details.OwlDetails;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class DetailsResult<T extends OwlDetails> extends SearcherResult<T> {
  
  public DetailsResult(Type type, T result) {
    super(type, result);
  }
}
