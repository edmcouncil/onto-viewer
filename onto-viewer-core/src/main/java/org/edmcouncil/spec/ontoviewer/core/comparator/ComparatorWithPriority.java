package org.edmcouncil.spec.ontoviewer.core.comparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class ComparatorWithPriority {

  // TODO: Improve quality of this code
  public static Comparator<String> get(List<String> prioritySortList) {
    return (String obj1, String obj2) -> {

      if (Objects.equals(obj1, obj2)) {
        return 0;
      }
      if (obj1 == null) {
        return -1;
      }
      if (obj2 == null) {
        return 1;
      }

      Optional<String> var1 = prioritySortList
          .stream()
          .map(Objects::toString)
          .filter(obj1::equals)
          .findFirst();
      Optional<String> var2 = prioritySortList
          .stream()
          .map(Objects::toString)
          .filter(obj2::equals)
          .findFirst();

      boolean containsObj1 = var1.isPresent();
      boolean containsObj2 = var2.isPresent();
      if (containsObj1 && !containsObj2) {
        return -1;
      }
      if (!containsObj1 && containsObj2) {
        return 1;
      }
      if (containsObj1 && containsObj2) {
        List<String> conv = new ArrayList<>(prioritySortList);
        int idxObj1 = conv.indexOf(obj1);
        int idxObj2 = conv.indexOf(obj2);

        return idxObj1 < idxObj2 ? -1 : 1;
      }

      return obj1.toLowerCase().compareTo(obj2.toLowerCase());
    };
  }
}
