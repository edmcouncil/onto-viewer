package org.edmcouncil.spec.ontoviewer.core.model.property;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OwlAxiomPropertyValue extends PropertyValueAbstract<String> {

  private String fullRenderedString;
  private Map<String, OwlAxiomPropertyEntity> entityMaping = new HashMap<>();
  private boolean inferable = false;
  private RestrictionType restrictionType = RestrictionType.OTHER;
  private List <PropertyValue> annotationPropertyValues = new LinkedList();

  public OwlAxiomPropertyValue() {
    super();
  }

  public OwlAxiomPropertyValue(String value, OwlType type, String fullRenderedString,
      Map<String, OwlAxiomPropertyEntity> entityMaping) {
    this.value = value;
    this.type = type;
    this.fullRenderedString = fullRenderedString;
    this.entityMaping = entityMaping;
  }

  public OwlAxiomPropertyValue(String value, OwlType type, String fullRenderedString,
      Map<String, OwlAxiomPropertyEntity> entityMaping, boolean inferable, RestrictionType restrictionType) {
    this.value = value;
    this.type = type;
    this.fullRenderedString = fullRenderedString;
    this.entityMaping = entityMaping;
    this.inferable = inferable;
    this.restrictionType = restrictionType;
  }

  public void addEntityValues(String key, OwlAxiomPropertyEntity valIri) {
    entityMaping.put(key, valIri);
  }

  public Map<String, OwlAxiomPropertyEntity> getEntityMaping() {
    return this.entityMaping;
  }

  public void setFullRenderedString(String fullRenderedString) {
    this.fullRenderedString = fullRenderedString;
  }

  public String getFullRenderedString() {
    return fullRenderedString;
  }

  public void setAnnotationPropertyValues(List <PropertyValue> annotationPropertyValues) { this.annotationPropertyValues = annotationPropertyValues;}

  public List <PropertyValue> getAnnotationPropertyValues() { return annotationPropertyValues;}

  public boolean isInferable() {
    return inferable;
  }

  public void setInferable(boolean inferable) {
    this.inferable = inferable;
  }

  public RestrictionType getRestrictionType() {
    return restrictionType;
  }

  public void setRestrictionType(RestrictionType restrictionType) {
    this.restrictionType = restrictionType;
  }

  @Override
  public String toString() {
    return fullRenderedString == null ? entityMaping.toString() : fullRenderedString;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OwlAxiomPropertyValue)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    OwlAxiomPropertyValue that = (OwlAxiomPropertyValue) o;
    return inferable == that.inferable
        && Objects.equals(fullRenderedString, that.fullRenderedString)
        && restrictionType == that.restrictionType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), fullRenderedString, inferable, restrictionType);
  }
}
