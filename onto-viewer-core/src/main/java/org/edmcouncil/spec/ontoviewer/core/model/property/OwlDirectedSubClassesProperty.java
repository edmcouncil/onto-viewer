package org.edmcouncil.spec.ontoviewer.core.model.property;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.Pair;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class OwlDirectedSubClassesProperty extends PropertyValueAbstract<Pair> {

  @Override
  public String toString() {
    return super.getValue().getLabel().toString();
  }
}
