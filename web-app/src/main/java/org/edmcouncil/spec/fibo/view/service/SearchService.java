package org.edmcouncil.spec.fibo.view.service;

import org.edmcouncil.spec.fibo.weasel.exception.ViewerException;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.SearcherResult;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public interface SearchService {

  SearcherResult search(String query, int maxValues) throws ViewerException;
}
