package org.edmcouncil.spec.ontoviewer.core.model.property;

import java.util.Map;
import java.util.Objects;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OwlAnnotationPropertyValueWithSubAnnotations extends OwlAnnotationPropertyValue {

  private Map<String, PropertyValue> subAnnotations;

  public Map<String, PropertyValue> getSubAnnotations() {
    return subAnnotations;
  }

  public void setSubAnnotations(
      Map<String, PropertyValue> subAnnotations) {
    this.subAnnotations = subAnnotations;
  }

  @Override
  public String toString() {
    return super.getValue();
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.getType(), this.getValue(), this.getSubAnnotations());
  }
}
