package org.edmcouncil.spec.fibo.weasel.ontology.searcher;

import org.edmcouncil.spec.fibo.weasel.exception.ViewerException;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.SearcherResult;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public interface ViewerSearcher {
    SearcherResult search(String query, Integer max) throws ViewerException;
}
