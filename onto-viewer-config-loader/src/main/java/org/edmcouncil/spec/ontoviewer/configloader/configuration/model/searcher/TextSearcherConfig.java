package org.edmcouncil.spec.ontoviewer.configloader.configuration.model.searcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigItemType;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.FindProperty;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class TextSearcherConfig implements ConfigItem {

  private final Set<SearcherField> hintFields;
  private Double hintThreshold;
  private final Set<SearcherField> searchFields;
  private Double searchThreshold;
  private final List<String> searchDescription;
  private Double hintMaxLevensteinDistance;
  private Double searchMaxLevensteinDistance;
  private int fuzzyDistance = -1;
  private List<FindProperty> findProperties;
  private Boolean reindexOnStart = null;

  public TextSearcherConfig() {
    hintFields = new HashSet<>();
    searchFields = new HashSet<>();
    searchDescription = new LinkedList<>();
    findProperties = new ArrayList<>();
  }

  public void addHintField(SearcherField hintField) {
    hintFields.add(hintField);
  }

  public void addSearchField(SearcherField searchField) {
    searchFields.add(searchField);
  }

  public void addSearchDescription(String elementIri) {
    searchDescription.add(elementIri);
  }

  public Double getHintThreshold() {
    return hintThreshold;
  }

  public void setHintThreshold(Double hintThreshold) {
    this.hintThreshold = hintThreshold;
  }

  public Double getSearchThreshold() {
    return searchThreshold;
  }

  public void setSearchThreshold(Double searchThreshold) {
    this.searchThreshold = searchThreshold;
  }

  public List<String> getSearchDescriptions() {
    return searchDescription;
  }

  public Set<SearcherField> getHintFields() {
    return hintFields;
  }

  public Set<SearcherField> getSearchFields() {
    return searchFields;
  }

  public Double getHintMaxLevensteinDistance() {
    return hintMaxLevensteinDistance;
  }

  public void setHintMaxLevensteinDistance(Double hintMaxLevensteinDistance) {
    this.hintMaxLevensteinDistance = hintMaxLevensteinDistance;
  }

  public Double getSearchMaxLevensteinDistance() {
    return searchMaxLevensteinDistance;
  }

  public void setSearchMaxLevensteinDistance(Double searchMaxLevensteinDistance) {
    this.searchMaxLevensteinDistance = searchMaxLevensteinDistance;
  }

  public List<FindProperty> getFindProperties() {
    return findProperties;
  }

  public void setFindProperties(List<FindProperty> findProperties) {
    this.findProperties = findProperties;
  }

  public void addFindProperty(FindProperty findProperty) {
    this.findProperties.add(findProperty);
  }

  public Boolean hasHintFieldWithIri(String iri) {
    for (SearcherField hintField : hintFields) {
      if (hintField.getIri().equals(iri)) {
        return Boolean.TRUE;
      }
    }
    return Boolean.FALSE;
  }

  public Boolean hasSearchFieldWithIri(String iri) {
    for (SearcherField searcherField : searchFields) {
      if (searcherField.getIri().equals(iri)) {
        return Boolean.TRUE;
      }
    }
    return Boolean.FALSE;
  }

  public int getFuzzyDistance() {
    return fuzzyDistance;
  }

  public void setFuzzyDistance(int fuzzyDistance) {
    this.fuzzyDistance = fuzzyDistance;
  }

  public Boolean isReindexOnStart() {
    return reindexOnStart;
  }

  public void setReindexOnStart(boolean reindexOnStart) {
    this.reindexOnStart = reindexOnStart;
  }

  @Override
  public ConfigItemType getType() {
    return ConfigItemType.SEARCH_CONFIG;
  }

  public boolean isCompleted() {
    return !hintFields.isEmpty() && !searchFields.isEmpty()
        && hintThreshold != null
        && searchThreshold != null
        && searchDescription != null
        && hintMaxLevensteinDistance != null
        && searchMaxLevensteinDistance != null;
  }

  @Override
  public String toString() {
    return "TextSearcherConfig{" + "hintFields=" + hintFields
        + ", hintThreshold=" + hintThreshold
        + ", searchFields=" + searchFields
        + ", searchThreshold=" + searchThreshold
        + ", searchDescription=" + searchDescription
        + ", hintMaxLevensteinDistance=" + hintMaxLevensteinDistance
        + ", searchMaxLevensteinDistance=" + searchMaxLevensteinDistance
        + ", findProperties=" + findProperties + '}';
  }
}
