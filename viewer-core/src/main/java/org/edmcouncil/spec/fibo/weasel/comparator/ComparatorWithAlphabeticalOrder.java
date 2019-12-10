package org.edmcouncil.spec.fibo.weasel.comparator;

import java.util.Comparator;

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
      return obj1.toString().toLowerCase()
              .compareTo(obj2.toString().toLowerCase());

    };
  }
}
