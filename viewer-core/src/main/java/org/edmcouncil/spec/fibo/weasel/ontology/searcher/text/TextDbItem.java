package org.edmcouncil.spec.fibo.weasel.ontology.searcher.text;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class TextDbItem {

  private final Set<Item> value;
  private static final Double BASE_BOST = 10.0d;

  public TextDbItem() {
    value = new HashSet<>();
  }
  //TODO: BOST fields map

  public void addValue(String type, String val) {
    //TODO: null checking
    value.add(new Item(type, val));
  }

  private boolean containsText(String text) {
    for (Item item : value) {
      if (item.getValue().contains(text)) {
        return Boolean.TRUE;
      }
    }

    return Boolean.FALSE;
  }

  Double computeRelevancy(String text, Set<String> fields) {
    Double result = 0.0d;

    for (String field : fields) {
      for (Item item : value) {
        Double tmpVal = 0.0d;
        if (item.type.equals(field) && item.value.contains(text)) {
          tmpVal = ((double) text.length() / item.value.length()) * BASE_BOST;
        }
        result = tmpVal > result ? tmpVal : result;
      }
    }

    return result;
  }

  Double computeRelevance(String text) {
    Double result = 0.0d;

    for (Item item : value) {
      Double tmpVal = 0.0d;
      if (item.value.contains(text)) {
        tmpVal = ((double) text.length() / item.value.length())*BASE_BOST;
      }
      result = tmpVal > result ? tmpVal : result;
    }

    return result;
  }

  static class Item {

    private final String type;
    private final String value;

    public Item(String type, String val) {
      this.type = type;
      this.value = val;
    }

    public String getType() {
      return type;
    }

    public String getValue() {
      return value;
    }

  }
}
