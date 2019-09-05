package org.edmcouncil.spec.fibo.weasel.model;

import org.edmcouncil.spec.fibo.weasel.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlAnnotationPropertyValue;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ConfigStringElement;
import org.edmcouncil.spec.fibo.weasel.model.taxonomy.OwlTaxonomyElementImpl;
import org.edmcouncil.spec.fibo.weasel.model.taxonomy.OwlTaxonomyImpl;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OwlDetails {

  private String label;
  private OwlDetailsProperties<PropertyValue> properties;
  private String type;
  private OwlTaxonomyImpl taxonomy;

  public OwlDetails() {
    if (properties == null) {
      properties = new OwlDetailsProperties<>();
    }
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Map<String, List<PropertyValue>> getProperties() {
    return properties.getProperties();
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setProperties(OwlDetailsProperties<PropertyValue> properties) {
    this.properties = properties;
  }
  
  public void addProperty(String key, OwlAnnotationPropertyValue property) {
    properties.addProperty(key, property);
  }

  public void setTaxonomy(OwlTaxonomyImpl tax) {
    this.taxonomy = tax;
  }

  public OwlTaxonomy getTaxonomy() {
    return this.taxonomy;
  }

  public void sortProperties(List<ConfigStringElement> priorityList) {
    properties.sort(priorityList);
  }

  public void addAllProperties(OwlDetailsProperties<PropertyValue> axioms) {
    axioms.getProperties().entrySet().forEach((entry) -> {
      entry.getValue().forEach((propertyValue) -> {
        properties.addProperty(entry.getKey(), propertyValue);
      });
    });
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 71 * hash + Objects.hashCode(this.label);
    hash = 71 * hash + Objects.hashCode(this.properties);
    hash = 71 * hash + Objects.hashCode(this.type);
    hash = 71 * hash + Objects.hashCode(this.taxonomy);
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
    final OwlDetails other = (OwlDetails) obj;
    if (!Objects.equals(this.label, other.label)) {
      return false;
    }
    if (!Objects.equals(this.type, other.type)) {
      return false;
    }
    if (!Objects.equals(this.properties, other.properties)) {
      return false;
    }
    if (!Objects.equals(this.taxonomy, other.taxonomy)) {
      return false;
    }
    return true;
  }


  
}
