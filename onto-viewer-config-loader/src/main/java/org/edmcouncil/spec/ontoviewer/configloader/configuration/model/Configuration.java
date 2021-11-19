package org.edmcouncil.spec.ontoviewer.configloader.configuration.model;

import java.util.Map;

/**
 * @param <T> is a type using to storage element in map.
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public interface Configuration<T> {

  Map<String, T> getConfiguration();

  T getValue(String key);

  boolean isEmpty();

  boolean isNotEmpty();
}
