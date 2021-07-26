package org.edmcouncil.spec.ontoviewer.core.model;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public interface PropertyValue<T> {

  WeaselOwlType getType();

  void setType(WeaselOwlType type);

  T getValue();

  void setValue(T value);
}
