package org.edmcouncil.spec.ontoviewer.core.model.property;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import org.edmcouncil.spec.ontoviewer.core.comparator.ComparatorWithPriority;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 * @param <T>
 */
public class OwlGroupedDetailsProperties<T> {

  private List taxonomy;
  private Map<String, Map<String, List<T>>> properties;

  public OwlGroupedDetailsProperties() {
    properties = new LinkedHashMap<>();
  }

  public void addProperty(String groupKey, String propertyKey, T property) {
    if (this.properties == null) {
      this.properties = new LinkedHashMap<>();
    }

    Map<String, List<T>> group = properties.getOrDefault(groupKey, new LinkedHashMap<>());
    List<T> objects = group.getOrDefault(propertyKey, new LinkedList<>());

    objects.add(property);
    group.put(propertyKey, objects);
    properties.put(groupKey, group);
  }

  public void addTaxonomy(String tax) {
    if (this.taxonomy == null) {
      this.taxonomy = new LinkedList();
    }

    this.taxonomy.add(tax);
  }

  public List getTaxonomy() {
    return taxonomy == null ? new ArrayList(0) : taxonomy;
  }

  public Map<String, Map<String, List<T>>> getProperties() {
    return properties;
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 23 * hash + Objects.hashCode(this.taxonomy);
    hash = 23 * hash + Objects.hashCode(this.properties);
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
    final OwlGroupedDetailsProperties<?> other = (OwlGroupedDetailsProperties<?>) obj;
    if (!Objects.equals(this.taxonomy, other.taxonomy)) {
      return false;
    }
    if (!Objects.equals(this.properties, other.properties)) {
      return false;
    }
    return true;
  }

  public void sort(Map<String, List<String>> groups) {
    Map<String, Map<String, List<T>>> sortedResults = new LinkedHashMap<>();

    Map<String, List<T>> others = properties.get("other");

    for (Entry<String, List<String>> groupEntry : groups.entrySet()) {
      Map<String, List<T>> prop = properties.get(groupEntry.getKey());
      if (prop == null) {
        continue;
      }

      Map<String, List<T>> newprop = new LinkedHashMap<>();

      List<String> priotityList = new LinkedList<>(groupEntry.getValue());
      Comparator<String> comparator = ComparatorWithPriority.get(priotityList);
      SortedSet<String> keys = new TreeSet<>(comparator);
      keys.addAll(prop.keySet());

      keys.forEach((key) -> {
        newprop.put(key, prop.get(key));
      });

      sortedResults.put(groupEntry.getKey(), newprop);
    }

    if (others != null) {
      Map<String, List<T>> newothers = new LinkedHashMap<>();
      Comparator<String> comparator = ComparatorWithPriority.get(new ArrayList<>(0));
      SortedSet<String> keys = new TreeSet<>(comparator);
      keys.addAll(others.keySet());

      keys.forEach((key) -> {
        newothers.put(key, others.get(key));
      });

      sortedResults.put("other", newothers);
    }

    properties = sortedResults;
  }

    @Override
    public String toString() {
        return "{" + "taxonomy=" + taxonomy + ", properties=" + properties + '}';
    }
}
