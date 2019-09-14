package org.edmcouncil.spec.fibo.weasel.model.details;

import java.util.Objects;
import org.edmcouncil.spec.fibo.weasel.model.OwlTaxonomy;
import org.edmcouncil.spec.fibo.weasel.model.PropertyValue;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlGroupedDetailsProperties;

/**
 *
 * Created by Michał Daniel (michal.mateusz.daniel@gmail.com)
 */
public class OwlDetails {

  private String label;
  private String iri;
  private String type;
  private OwlTaxonomy taxonomy;

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getIri() {
    return iri;
  }

  public void setIri(String iri) {
    this.iri = iri;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public void setTaxonomy(OwlTaxonomy tax) {
    this.taxonomy = tax;
  }

  public OwlTaxonomy getTaxonomy() {
    return this.taxonomy;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 59 * hash + Objects.hashCode(this.label);
    hash = 59 * hash + Objects.hashCode(this.iri);
    hash = 59 * hash + Objects.hashCode(this.type);
    hash = 59 * hash + Objects.hashCode(this.taxonomy);
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
    if (!Objects.equals(this.iri, other.iri)) {
      return false;
    }
    if (!Objects.equals(this.type, other.type)) {
      return false;
    }
    if (!Objects.equals(this.taxonomy, other.taxonomy)) {
      return false;
    }
    return true;
  }
  
  
}
