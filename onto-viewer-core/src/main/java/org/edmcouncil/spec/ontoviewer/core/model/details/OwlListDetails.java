package org.edmcouncil.spec.ontoviewer.core.model.details;

import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationPropertyValue;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OwlListDetails extends OwlDetails {

  private OwlDetailsProperties<PropertyValue> properties;

  public OwlListDetails() {
    this.properties = new OwlDetailsProperties<>();
  }

  public Map<String, List<PropertyValue>> getProperties() {
    return properties.getProperties();
  }

  public void setProperties(OwlDetailsProperties<PropertyValue> properties) {
    this.properties = properties;
  }

  public void addProperty(String key, OwlAnnotationPropertyValue property) {
    properties.addProperty(key, property);
  }

  public void sortProperties(List<String> priorityList) {
    properties.sort(priorityList);
  }

  public void addAllProperties(OwlDetailsProperties<PropertyValue> axioms) {
    axioms.getProperties().entrySet().forEach((entry) -> {
      entry.getValue().forEach((propertyValue) -> {
        properties.addProperty(entry.getKey(), propertyValue);
      });
    });
  }

  public void release() {
    properties.release();
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 51 * hash + super.hashCode();
    hash = 51 * hash + Objects.hashCode(this.properties);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final OwlListDetails other = (OwlListDetails) obj;
    if (!Objects.equals(this.properties, other.properties)) {
      return false;
    }
    return true;
  }
}
