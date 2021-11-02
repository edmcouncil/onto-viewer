package org.edmcouncil.spec.ontoviewer.core.model;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public interface PropertyValue<T> {

  OwlType getType();

  void setType(OwlType type);

  T getValue();

  void setValue(T value);
}
