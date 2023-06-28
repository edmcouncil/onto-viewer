package org.edmcouncil.spec.ontoviewer.core.ontology;

import java.util.List;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlDetails;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlGroupedDetails;
import org.springframework.stereotype.Service;

@Service
public class OwlRestrictionsHandler {

  private static final String ONTOLOGICAL_CHARACTERISTICS = "Ontological characteristic";

  public OwlDetails detectInheritableRestrictions(OwlGroupedDetails owlDetails) {
    var restrictions = owlDetails.getProperties().get(ONTOLOGICAL_CHARACTERISTICS); // TODO Handle all other groups?
    collectProperties(restrictions);


    return owlDetails;
  }

  private void collectProperties(Map<String, List<PropertyValue>> restrictions) {
    for (List<PropertyValue> restrictionGroups : restrictions.values()) {
      for (PropertyValue restrictionGroup : restrictionGroups) {
//        restrictionGroup.
      }
    }
  }

}
