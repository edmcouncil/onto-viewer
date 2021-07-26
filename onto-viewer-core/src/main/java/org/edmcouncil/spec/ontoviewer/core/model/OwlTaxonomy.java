package org.edmcouncil.spec.ontoviewer.core.model;

import java.util.List;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @param <T> Inner element type.
 */
public interface OwlTaxonomy<T> {
  List<List<T>> getValue();
}
