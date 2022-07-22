package org.edmcouncil.spec.ontoviewer.core.comparator;

import java.util.Comparator;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlListElementIndividualProperty;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class ComparatorWithAlphabeticalOrder {

  public static Comparator<Object> get() {
    return (Object obj1, Object obj2) -> {
      if (obj1 == obj2) {
        return 0;
      }
      if (obj1 == null) {
        return -1;
      }
      if (obj2 == null) {
        return 1;
      }
      String txt1 = obj1.toString().toLowerCase();
      String txt2 = obj2.toString().toLowerCase();
      // TODO: This is never true; fix or remove it
      if (obj1.getClass().getName().equals(obj2.getClass().getName().equals("OwlAxiomPropertyValue"))) {
        txt1 = ((OwlAxiomPropertyValue)obj1).getFullRenderedString().toLowerCase();
        txt2 = ((OwlAxiomPropertyValue)obj2).getFullRenderedString().toLowerCase();
      }
      if (obj1.getClass().getName().equals(obj2.getClass().getName().equals("OwlListElementIndividualProperty"))) {
        txt1 = ((OwlListElementIndividualProperty)obj1).getValue().getLabel().toLowerCase();
        txt2 = ((OwlListElementIndividualProperty)obj2).getValue().getLabel().toLowerCase();
      }
      return txt1.compareTo(txt2);

    };
  }
}
