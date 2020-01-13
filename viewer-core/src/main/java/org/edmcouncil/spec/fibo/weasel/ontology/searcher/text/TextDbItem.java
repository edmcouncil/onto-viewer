package org.edmcouncil.spec.fibo.weasel.ontology.searcher.text;

import java.util.HashSet;
import java.util.Set;
import org.edmcouncil.spec.fibo.config.configuration.model.searcher.SearcherField;

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

  public Set<Item> getValue() {
    return value;
  }

  Double computeHintRelevancy(String text, Set<SearcherField> fields) {
    Double result = 0.0d;
    String sText = text.toLowerCase();
    for (SearcherField field : fields) {
      if (!field.getBoost().equals(0.0d)) {
        for (Item item : value) {
          Double tmpVal = 0.0d;
          if (item.type.equals(field.getIri()) && item.value.toLowerCase().contains(sText)) {
            tmpVal = ((double) text.length() / item.value.length()) * BASE_BOST * field.getBoost();
          }
          result = tmpVal > result ? tmpVal : result;
        }
      }
    }

    return result;
  }

  Double computeSearchRelevancy(String text, Set<SearcherField> fields) {
    Double result = 0.0d;
    String sText = text.toLowerCase();
    for (SearcherField field : fields) {
      if (!field.getBoost().equals(0.0d)) {
        for (Item item : value) {
          Double tmpVal = 0.0d;
          if (item.type.equals(field.getIri()) && item.value.toLowerCase().contains(sText)) {
            tmpVal = ((double) text.length() / item.value.length()) * BASE_BOST * field.getBoost();
          }
          result = tmpVal > result ? tmpVal : result;
        }
      }
    }

    return result;
  }
  
  public boolean isEmpty(){
    return value.isEmpty();
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
