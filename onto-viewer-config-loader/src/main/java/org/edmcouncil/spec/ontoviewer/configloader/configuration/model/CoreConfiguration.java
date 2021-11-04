package org.edmcouncil.spec.ontoviewer.configloader.configuration.model;

import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys.INDIVIDUALS_ENABLED;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys.LOCATION_IN_MODULES_ENABLED;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys.ONTOLOGY_GRAPH_ENABLED;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys.ONTOLOGY_HANDLING;
import static org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys.USAGE_ENABLED;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.BooleanItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.DefaultLabelItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.LabelPriority;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.MissingLanguageItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.StringItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.searcher.TextSearcherConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class CoreConfiguration implements Configuration<Set<ConfigItem>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CoreConfiguration.class);

  private Map<String, Set<ConfigItem>> configuration;
  private static final String DEFAULT_LANG = "en";

  public CoreConfiguration() {
    this.configuration = new HashMap<>();
  }

  @Override
  public Map<String, Set<ConfigItem>> getConfiguration() {
    return this.configuration;
  }

  @Override
  public Set<ConfigItem> getValue(String key) {
    return this.configuration != null ? configuration.get(key) : null;
  }

  public void addConfigElement(String key, ConfigItem value) {
    if (configuration == null) {
      configuration = new HashMap<>();
    }

    // MAYBE: merge
    Set<ConfigItem> valueSet = configuration.getOrDefault(key, new HashSet<>());
    valueSet.add(value);
    configuration.put(key, valueSet);
  }

  @Override
  public boolean isEmpty() {
    return configuration == null || configuration.isEmpty();
  }

  @Override
  public boolean isNotEmpty() {
    return !isEmpty();
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
      configuration.get(ConfigKeys.ONTOLOGY_PATH)
          .forEach(configItem -> item.add(configItem.toString()));
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
    Set<ConfigItem> values = configuration.getOrDefault(ConfigKeys.FORCE_LABEL_LANG,
        new HashSet<>());

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
    Set<ConfigItem> values = configuration.getOrDefault(ConfigKeys.MISSING_LANGUAGE_ACTION,
        new HashSet<>());

    for (ConfigItem value : values) {
      MissingLanguageItem cpe = (MissingLanguageItem) value;
      return cpe.getValue();
    }

    return MissingLanguageItem.Action.FIRST;
  }

  public Set<String> getIgnoredElements() {

    Set<ConfigItem> values = configuration.getOrDefault(ConfigKeys.IGNORE_TO_DISPLAYING,
        new HashSet<>());
    Set<String> result = new HashSet<>();
    values.stream()
        .map((value) -> (StringItem) value)
        .forEachOrdered((cse) -> {
          result.add(cse.toString());
        });

    return result;
  }

  public Set<DefaultLabelItem> getDefaultLabels() {
    Set<ConfigItem> values = configuration.getOrDefault(
        ConfigKeys.USER_DEFAULT_NAME_LIST,
        new HashSet<>());
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
    Set<ConfigItem> values = configuration.getOrDefault(ConfigKeys.TEXT_SEARCH_CONFIG,
        Collections.emptySet());

    for (ConfigItem value : values) {
      return (TextSearcherConfig) value;
    }
    return null;
  }

  public Set<String> getScope() {
    Set<ConfigItem> scopeIri = configuration.getOrDefault(ConfigKeys.SCOPE_IRI, new HashSet<>());
    Set<String> result = new HashSet<>();
    for (ConfigItem configElement : scopeIri) {
      StringItem element = (StringItem) configElement;
      result.add(element.toString());
    }
    return result;
  }

  public Optional<String> getSingleStringValue(String key) {
    var value = getValue(key);
    if (value.isEmpty()) {
      return Optional.empty();
    }

    return value.stream().map(Object::toString).findFirst();
  }

  public void logConfigurationDebugInfo() {
    if (configuration.isEmpty()) {
      LOGGER.debug("Configuration is empty.");
    } else {
      LOGGER.debug("Configuration debug info:");
      getConfiguration().forEach((key, value) -> {
        LOGGER.debug("\tEntry '{}':", key);
        LOGGER.debug("\t\t- {}", value);
      });
    }
  }

  public KeyValueMapConfigItem getOntologyHandling() {
    if (!configuration.containsKey(ONTOLOGY_HANDLING)) {
      var properties = new HashMap<String, Object>();
      var defaultOntologyHandling = new KeyValueMapConfigItem(properties);
      addConfigElement(ONTOLOGY_HANDLING, defaultOntologyHandling);
    }

    var ontologyHandlingConfig = (KeyValueMapConfigItem) configuration.get(ONTOLOGY_HANDLING)
        .stream().findFirst()
        .orElse(new KeyValueMapConfigItem(new HashMap<>()));

    ontologyHandlingConfig.putIfAbsent(LOCATION_IN_MODULES_ENABLED, true);
    ontologyHandlingConfig.putIfAbsent(USAGE_ENABLED, true);
    ontologyHandlingConfig.putIfAbsent(ONTOLOGY_GRAPH_ENABLED, true);
    ontologyHandlingConfig.putIfAbsent(INDIVIDUALS_ENABLED, true);

    return ontologyHandlingConfig;
  }
}