package org.edmcouncil.spec.fibo.weasel.model.property;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OwlAxiomPropertyValue extends PropertyValueAbstract<String> {

  private final Map<String, OwlAxiomPropertyEntity> entityMaping;

  private int lastId;

  private String fullRenderedString;

  public OwlAxiomPropertyValue() {
    super();
    entityMaping = new HashMap<>();
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

  @Override
  public String toString() {
    return fullRenderedString == null ? entityMaping.toString() : fullRenderedString;
  }

//  @Override
//  public int hashCode() {
//    int hash = 7;
//   // hash = 73 * hash + Objects.hashCode(this.entityMaping);
//    hash = 73 * hash + Objects.hashCode(this.fullRenderedString);
//    return hash;
//  }
//
//  @Override
//  public boolean equals(Object obj) {
//    if (this == obj) {
//      return true;
//    }
//    if (obj == null) {
//      return false;
//    }
//    if (getClass() != obj.getClass()) {
//      return false;
//    }
//    final OwlAxiomPropertyValue other = (OwlAxiomPropertyValue) obj;
////    if (this.lastId != other.lastId) {
////      return false;
////    }
//    if (!Objects.equals(this.fullRenderedString, other.fullRenderedString)) {
//      return false;
//    }
////    if (!Objects.equals(this.entityMaping, other.entityMaping)) {
////      return false;
////    }
//    return true;
//  }

  
  
}
