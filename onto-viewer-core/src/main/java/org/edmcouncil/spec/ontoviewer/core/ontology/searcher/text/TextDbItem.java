package org.edmcouncil.spec.ontoviewer.core.ontology.searcher.text;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.searcher.SearcherField;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 *
 */
public class TextDbItem {

  private final Set<Item> value;
  private static final Double BASE_BOST = 10.0d;
  private static final LevenshteinDistance levenshteinDistance = LevenshteinDistance.getDefaultInstance();

  public TextDbItem() {
    value = new HashSet<>();
  }
  //TODO: BOST fields map

  public void addValue(String type, String val, String lang) {
    //TODO: null checking
    value.add(new Item(type, val, lang));
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

  Double computeLevensteinDistance(String text, Set<SearcherField> fields) {
    Double result = Double.MAX_VALUE ;
    // String sText = text.toLowerCase();
    for (SearcherField field : fields) {
      if (!field.getBoost().equals(0.0d)) {
        for (Item item : value) {
          Double tmpVal = Double.MAX_VALUE;
          if (item.type.equals(field.getIri())) {
            tmpVal = ((double) levenshteinDistance.apply(text, item.getValue()));
          }
          result = tmpVal < result ? tmpVal : result;
        }
      }
    }

    return result;
  }

  public boolean isEmpty() {
    return value.isEmpty();
  }

  static class Item {

    private final String type;
    private final String value;
    private final String lang;

    public Item(String type, String value, String lang) {
      this.type = type;
      this.value = value;
      this.lang = lang;
    }

    public String getLang() {
      return lang;
    }

    public String getType() {
      return type;
    }

    public String getValue() {
      return value;
    }

  }
}
