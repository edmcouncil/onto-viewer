package org.edmcouncil.spec.fibo.weasel.model.onto;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OntologyResources {

  private final Map<String, List<String>> resources = new HashMap<>();

  public void addElement(String type, String element) {
    List<String> resList = resources.getOrDefault(type, new LinkedList<>());
    resList.add(element);
    resources.put(type, resList);
  }

  public Map<String, List<String>> getResources() {
    return resources;
  }

  public boolean contains(String iri) {
    return resources.entrySet()
        .stream()
        .filter(i -> i.getValue().contains(iri))
        .count() > 0;
  }

  @Override
  public String toString() {
    return "OntologyResources{" + "resources=" + resources + '}';
  }
  
}
