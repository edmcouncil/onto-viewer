package org.edmcouncil.spec.ontoviewer.core.model.property;

import java.util.HashMap;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OwlAxiomPropertyValue extends PropertyValueAbstract<String> {

  private int lastId;
  private String fullRenderedString;
  private boolean inferable = false;
  private Map<String, OwlAxiomPropertyEntity> entityMaping = new HashMap<>();

  public OwlAxiomPropertyValue() {
    super();
  }

  public OwlAxiomPropertyValue(String value, OwlType type, int lastId, String fullRenderedString,
      Map<String, OwlAxiomPropertyEntity> entityMaping) {
    this.value = value;
    this.type = type;
    this.lastId = lastId;
    this.fullRenderedString = fullRenderedString;
    this.entityMaping = entityMaping;
    this.inferable = false;
  }

  public OwlAxiomPropertyValue(String value, OwlType type, int lastId, String fullRenderedString,
      Map<String, OwlAxiomPropertyEntity> entityMaping,
      boolean inferable) {
    this.value = value;
    this.type = type;
    this.lastId = lastId;
    this.fullRenderedString = fullRenderedString;
    this.inferable = inferable;
    this.entityMaping = entityMaping;
  }

  public void addEntityValues(String key, OwlAxiomPropertyEntity valIri) {
    entityMaping.put(key, valIri);
  }

  public Map<String, OwlAxiomPropertyEntity> getEntityMaping() {
    return this.entityMaping;
  }

  public int getLastId() {
    return this.lastId;
  }

  public void setLastId(int lastId) {
    this.lastId = lastId;
  }

  public void setFullRenderedString(String fullRenderedString) {
    this.fullRenderedString = fullRenderedString;
  }

  public String getFullRenderedString() {
    return fullRenderedString;
  }

  public boolean isInferable() {
    return inferable;
  }

  public void setInferable(boolean inferable) {
    this.inferable = inferable;
  }

  @Override
  public String toString() {
    return fullRenderedString == null ? entityMaping.toString() : fullRenderedString;
  }
}
