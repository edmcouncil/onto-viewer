package org.edmcouncil.spec.ontoviewer.configloader.configuration.model;

import java.util.Map;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @param <T> is a type using to storage element in map.
 */
public interface Configuration<T> {

  Map<String, T> getConfiguration();

  T getConfigVal(String cfName);
  
  boolean isEmpty();
}
