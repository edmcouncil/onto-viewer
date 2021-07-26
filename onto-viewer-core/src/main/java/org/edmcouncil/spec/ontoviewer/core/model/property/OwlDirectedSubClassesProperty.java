package org.edmcouncil.spec.ontoviewer.core.model.property;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.PairImpl;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class OwlDirectedSubClassesProperty extends PropertyValueAbstract<PairImpl> {

  @Override
  public String toString() {
    return super.getValue().getLabel().toString();
  }
}
