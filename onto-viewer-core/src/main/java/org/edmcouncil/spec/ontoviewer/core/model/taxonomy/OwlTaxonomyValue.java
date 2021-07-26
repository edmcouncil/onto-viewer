package org.edmcouncil.spec.ontoviewer.core.model.taxonomy;

import org.edmcouncil.spec.ontoviewer.core.model.WeaselOwlType;
import org.edmcouncil.spec.ontoviewer.core.model.property.PropertyValueAbstract;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OwlTaxonomyValue extends PropertyValueAbstract<String> {

  public OwlTaxonomyValue() {
  }

  public OwlTaxonomyValue(WeaselOwlType type, String value) {
    setType(type);
    setValue(value);
  }

}
