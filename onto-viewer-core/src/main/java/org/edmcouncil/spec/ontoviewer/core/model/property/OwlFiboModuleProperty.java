package org.edmcouncil.spec.ontoviewer.core.model.property;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.Pair;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OwlFiboModuleProperty implements PropertyValue<Pair> {

  private OwlType type;
  private Pair value;

  public OwlFiboModuleProperty() {
    value = new Pair();
  }

  @Override
  public OwlType getType() {
    return this.type;
  }

  @Override
  public void setType(OwlType type) {
    this.type = type;
  }

  @Override
  public Pair getValue() {
    return this.value;
  }

  @Override
  public void setValue(Pair value) {
    this.value = value;
  }

  public String getIri() {
    return value.getIri();
  }

  public String getName() {
    return value.getIri();
  }

  public void setIri(String s) {
    value.setIri(s);
  }

  public void setName(String s) {
    value.setLabel(s);
  }
}