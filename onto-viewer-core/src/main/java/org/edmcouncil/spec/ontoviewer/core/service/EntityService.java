package org.edmcouncil.spec.ontoviewer.core.service;

import org.edmcouncil.spec.ontoviewer.core.exception.NotFoundElementInOntologyException;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlDetails;
import org.edmcouncil.spec.ontoviewer.core.ontology.DetailsManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.SearcherResult;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.result.ResultFactory;
import org.springframework.stereotype.Service;

@Service
public class EntityService {

  private final DetailsManager detailsManager;

  public EntityService(DetailsManager detailsManager) {
    this.detailsManager = detailsManager;
  }

  public SearcherResult<OwlDetails> getEntityDetailsByIri(String iri) throws NotFoundElementInOntologyException {
    OwlDetails ontologyDetails = detailsManager.getEntityDetailsByIri(iri);
    return ResultFactory.createDetailsResult(ontologyDetails);
  }
}
