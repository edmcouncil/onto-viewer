package org.edmcouncil.spec.ontoviewer.core.model.details;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlGroupedDetailsProperties;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OwlGroupedDetails extends OwlDetails {

  private OwlGroupedDetailsProperties<PropertyValue> properties;

  public OwlGroupedDetails() {
    properties = new OwlGroupedDetailsProperties<>();
  }

  public Map<String, Map<String, List<PropertyValue>>> getProperties() {
    return properties.getProperties();
  }

  public void setProperties(OwlGroupedDetailsProperties<PropertyValue> prop) {
    this.properties = prop;
  }

  public void addProperty(String groupKey, String propertyKey, PropertyValue property) {
    properties.addProperty(groupKey, propertyKey, property);
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 71 * hash * super.hashCode();
    hash = 71 * hash + Objects.hashCode(this.properties);
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
    final OwlGroupedDetails other = (OwlGroupedDetails) obj;
    if (!Objects.equals(this.properties, other.properties)) {
      return false;
    }

    return true;
  }

  public void sortProperties(Map<String, List<String>> groups) {
    properties.sort(groups);
  }
}