package org.edmcouncil.spec.fibo.config.configuration.model.impl;

import org.edmcouncil.spec.fibo.config.configuration.model.impl.element.BooleanItem;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.element.MissingLanguageItem;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.element.StringItem;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.element.LabelPriority;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.edmcouncil.spec.fibo.config.configuration.model.Configuration;
import org.edmcouncil.spec.fibo.config.configuration.model.ConfigKeys;
import org.edmcouncil.spec.fibo.config.configuration.model.ConfigItem;
import org.edmcouncil.spec.fibo.config.configuration.model.searcher.TextSearcherConfig;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.element.DefaultLabelItem;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class ViewerCoreConfiguration implements Configuration<Set<ConfigItem>> {

  private Map<String, Set<ConfigItem>> configuration;
  private static final String DEFAULT_LANG = "en";

  public ViewerCoreConfiguration() {
    configuration = new HashMap<>();
  }

  @Override
  public Map<String, Set<ConfigItem>> getConfiguration() {
    return this.configuration;
  }

  @Override
  public Set<ConfigItem> getConfigVal(String cfName) {
    return configuration != null ? configuration.get(cfName) : null;
  }

  public void addCongigElement(String key, ConfigItem val) {
    if (configuration == null) {
      configuration = new HashMap<>();
    }

    Set valList = configuration.get(key);
    valList = valList == null ? new LinkedHashSet() : valList;
    valList.add(val);
    configuration.put(key, valList);
  }

  @Override
  public boolean isEmpty() {
    return configuration == null || configuration.isEmpty();
  }

  public boolean isGrouped() {
    return !isEmpty() && configuration.get(ConfigKeys.GROUPS) != null;
  }

  public boolean isOntologyLocationSet() {
    return configuration.containsKey(ConfigKeys.ONTOLOGY_URL)
            || configuration.containsKey(ConfigKeys.ONTOLOGY_PATH)
            || configuration.containsKey(ConfigKeys.ONTOLOGY_DIR);
  }

  public Map<String, Set<String>> getOntologyLocation() {
    Map<String, Set<String>> result = new LinkedHashMap<>();
    getOntologyPath(result);
    getOntologyUrl(result);
    getOntologyDir(result);

    return result;
  }

  private void getOntologyPath(Map<String, Set<String>> result) {
    if (configuration.containsKey(ConfigKeys.ONTOLOGY_PATH)) {
      Set<String> item = result.getOrDefault(ConfigKeys.ONTOLOGY_PATH, new HashSet<>());
      configuration.get(ConfigKeys.ONTOLOGY_PATH).forEach((configItem) -> {
        item.add(configItem.toString());
      });
      result.put(ConfigKeys.ONTOLOGY_PATH, item);
    }
  }

  private void getOntologyDir(Map<String, Set<String>> result) {
    if (configuration.containsKey(ConfigKeys.ONTOLOGY_DIR)) {
      Set<String> item = result.getOrDefault(ConfigKeys.ONTOLOGY_DIR, new HashSet<>());
      configuration.get(ConfigKeys.ONTOLOGY_DIR).forEach((configItem) -> {
        item.add(configItem.toString());
      });
      result.put(ConfigKeys.ONTOLOGY_DIR, item);
    }
  }

  private void getOntologyUrl(Map<String, Set<String>> result) {
    if (configuration.containsKey(ConfigKeys.ONTOLOGY_URL)) {
      Set<String> item = result.getOrDefault(ConfigKeys.ONTOLOGY_URL, new HashSet<>());
      configuration.get(ConfigKeys.ONTOLOGY_URL).forEach((configItem) -> {
        item.add(configItem.toString());
      });
      result.put(ConfigKeys.ONTOLOGY_URL, item);
    }
  }

  public Set<String> getOntologyMapper() {
    Set<String> result = new LinkedHashSet<>();
    if (configuration.containsKey(ConfigKeys.ONTOLOGY_MAPPER)) {
      configuration.get(ConfigKeys.ONTOLOGY_MAPPER).forEach((configItem) -> {
        result.add(configItem.toString());
      });
    }

    return result;
  }

  //TODO: Change this method name..
  /**
   *
   * @param uri - String representation of URI
   * @return True if it finds representation in the configuration, otherwise false.
   */
  public Boolean isUriIri(String uri) {
    Set<ConfigItem> scopeIri = configuration.getOrDefault(ConfigKeys.SCOPE_IRI, new HashSet<>());
    for (ConfigItem configElement : scopeIri) {
      StringItem element = (StringItem) configElement;
      if (uri.contains(element.toString())) {
        return Boolean.TRUE;
      }
    }
    return Boolean.FALSE;
  }

  public Boolean useLabels() {
    Set<ConfigItem> values = configuration.getOrDefault(ConfigKeys.DISPLAY_LABEL, new HashSet<>());

    for (ConfigItem value : values) {
      BooleanItem cbe = (BooleanItem) value;
      return cbe.getValue();
    }

    return Boolean.TRUE;
  }

  public Boolean isForceLabelLang() {
    Set<ConfigItem> values = configuration.getOrDefault(ConfigKeys.FORCE_LABEL_LANG, new HashSet<>());

    for (ConfigItem value : values) {
      BooleanItem cbe = (BooleanItem) value;
      return cbe.getValue();
    }

    return Boolean.FALSE;
  }

  public LabelPriority.Priority getGroupLabelPriority() {
    Set<ConfigItem> values = configuration.getOrDefault(ConfigKeys.LABEL_PRIORITY, new HashSet<>());

    for (ConfigItem value : values) {
      LabelPriority cpe = (LabelPriority) value;
      return cpe.getValue();
    }

    return LabelPriority.Priority.USER_DEFINED;
  }

  public String getLabelLang() {
    Set<ConfigItem> values = configuration.getOrDefault(ConfigKeys.LABEL_LANG, new HashSet<>());

    for (ConfigItem value : values) {
      StringItem cpe = (StringItem) value;
      return cpe.toString();
    }

    return DEFAULT_LANG;
  }

  public MissingLanguageItem.Action getMissingLanguageAction() {
    Set<ConfigItem> values = configuration.getOrDefault(ConfigKeys.MISSING_LANGUAGE_ACTION, new HashSet<>());

    for (ConfigItem value : values) {
      MissingLanguageItem cpe = (MissingLanguageItem) value;
      return cpe.getValue();
    }

    return MissingLanguageItem.Action.FIRST;
  }

  public Set<String> getIgnoredElements() {

    Set<ConfigItem> values = configuration.getOrDefault(ConfigKeys.IGNORE_TO_DISPLAYING, new HashSet<>());
    Set<String> result = new HashSet<>();
    values.stream()
            .map((value) -> (StringItem) value)
            .forEachOrdered((cse) -> {
              result.add(cse.toString());
            });

    return result;
  }

  public Set<DefaultLabelItem> getDefaultLabels() {

    Set<ConfigItem> values = configuration.getOrDefault(ConfigKeys.USER_DEFAULT_NAME_LIST, new HashSet<>());
    Set<DefaultLabelItem> result = new HashSet<>();
    values.stream()
            .map((value) -> (DefaultLabelItem) value)
            .forEachOrdered((cse) -> {
              result.add(cse);
            });

    return result;
  }

  public LabelPriority.Priority getLabelPriority() {
    Set<ConfigItem> values = configuration.getOrDefault(ConfigKeys.LABEL_PRIORITY, new HashSet<>());

    for (ConfigItem value : values) {
      LabelPriority item = (LabelPriority) value;
      return item.getValue();
    }
    return LabelPriority.Priority.USER_DEFINED;
  }

  public TextSearcherConfig getTextSearcherConfig() {
    Set<ConfigItem> values = configuration.get(ConfigKeys.TEXT_SEARCH_CONFIG);

    for (ConfigItem value : values) {
      TextSearcherConfig tsc = (TextSearcherConfig) value;
      return tsc;
    }
    return null;
  }

}
