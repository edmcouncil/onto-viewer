package org.edmcouncil.spec.ontoviewer.core.model.onto;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationIri;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OntologyResources {

  private final Map<String, List<PropertyValue>> resources = new HashMap<>();

  public void addElement(String type, PropertyValue element) {
    List<PropertyValue> listOfResources = resources.getOrDefault(type, new LinkedList<>());
    listOfResources.add(element);
    resources.put(type, listOfResources);
  }

  public Map<String, List<PropertyValue>> getResources() {
    return resources;
  }

  public void sortInAlphabeticalOrder() {
    for (Map.Entry<String, List<PropertyValue>> entry : resources.entrySet()) {
      List<PropertyValue> list = entry.getValue();
      list.sort((o1, o2) -> ((OwlAnnotationIri) o1).getValue().getLabel()
          .compareToIgnoreCase(((OwlAnnotationIri) o2).getValue().getLabel()));
    }
  }

  @Override
  public String toString() {
    return "OntologyResources{" + "resources=" + resources + '}';
  }
}
