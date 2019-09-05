package org.edmcouncil.spec.fibo.weasel.model;

import org.edmcouncil.spec.fibo.weasel.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlAnnotationPropertyValue;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.edmcouncil.spec.fibo.config.configuration.model.ConfigElement;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ConfigGroupsElement;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ConfigStringElement;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.WeaselConfiguration;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlGroupedDetailsProperties;
import org.edmcouncil.spec.fibo.weasel.model.taxonomy.OwlTaxonomyImpl;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OwlGroupedDetails {

  private String label;
  private OwlGroupedDetailsProperties<PropertyValue> properties;
  private String type;
  private OwlTaxonomy taxonomy;
 

  public OwlGroupedDetails() {
    if (properties == null) {
      properties = new OwlGroupedDetailsProperties<>();
    }
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Map<String, Map<String, List<PropertyValue>>> getProperties() {
    return properties.getProperties();
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void addProperty(String groupKey, String propertyKey, PropertyValue property) {
    properties.addProperty(groupKey, propertyKey, property);
  }

  public void setTaxonomy(OwlTaxonomy tax) {
    this.taxonomy = tax;
  }

  public OwlTaxonomy getTaxonomy() {
    return this.taxonomy;
  }

  public void sortProperties(List<ConfigStringElement> priorityList) {
    properties.sort(priorityList);
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
    final OwlGroupedDetails other = (OwlGroupedDetails) obj;
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

  public void sortProperties(Set<ConfigElement> groups) {
   properties.sort(groups);

  }

  public void sortProperties(Set<ConfigElement> groups, WeaselConfiguration cfg) {
   properties.sort(groups,cfg);
  }

}
