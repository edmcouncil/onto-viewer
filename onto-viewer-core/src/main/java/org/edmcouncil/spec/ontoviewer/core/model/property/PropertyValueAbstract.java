package org.edmcouncil.spec.ontoviewer.core.model.property;

import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import java.util.Objects;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public abstract class PropertyValueAbstract<T> implements PropertyValue<T> {

  private OwlType type;
  private T value;
  
  @Override
  public OwlType getType() {
    return this.type;
  }

  @Override
  public void setType(OwlType type) {
    this.type = type;
  }

  @Override
  public T getValue() {
    return this.value;
  }

  @Override
  public void setValue(T value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "PropertyValue<" + "value:" + value + "type:" + type + '>';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PropertyValueAbstract<?> that = (PropertyValueAbstract<?>) o;
    return type == that.type && value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, value);
  }
}
