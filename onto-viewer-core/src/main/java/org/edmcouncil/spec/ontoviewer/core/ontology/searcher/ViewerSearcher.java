package org.edmcouncil.spec.ontoviewer.core.ontology.searcher;

import org.edmcouncil.spec.ontoviewer.core.exception.ViewerException;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.SearcherResult;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public interface ViewerSearcher {
    SearcherResult search(String query, Integer max, Integer currentPage) throws ViewerException;
}
