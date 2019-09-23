package org.edmcouncil.spec.fibo.config.configuration.model.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.edmcouncil.spec.fibo.config.configuration.model.ConfigElement;
import org.edmcouncil.spec.fibo.config.configuration.model.Configuration;
import org.edmcouncil.spec.fibo.config.configuration.model.WeaselConfigKeys;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class WeaselConfiguration implements Configuration<Set<ConfigElement>> {

  private Map<String, Set<ConfigElement>> configuration;

  public WeaselConfiguration() {
    configuration = new HashMap<>();
  }

  @Override
  public Map<String, Set<ConfigElement>> getConfiguration() {
    return this.configuration;
  }

  @Override
  public Set<ConfigElement> getConfigVal(String cfName) {
    return configuration != null ? configuration.get(cfName) : null;
  }

  public void addCongigElement(String key, ConfigElement val) {
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
    return !isEmpty() && configuration.get(WeaselConfigKeys.GROUPS) != null;
  }

  public String getNewName(String oldName) {
    Set<ConfigElement> renamedGroups = configuration.getOrDefault(WeaselConfigKeys.RENAME_GROUPS, new HashSet<>());
    for (ConfigElement renamedG : renamedGroups) {
      ConfigRenameElement rename = (ConfigRenameElement) renamedG;
      if (rename.getOldName().equals(oldName)) {
        return rename.getNewName();
      }
    }
    return null;

  }

  public boolean hasRenamedGroups() {
    return configuration.get(WeaselConfigKeys.RENAME_GROUPS) != null;
  }

  public String getOldName(String newName) {
    Set<ConfigElement> renamedGroups = configuration.getOrDefault(WeaselConfigKeys.RENAME_GROUPS, new HashSet<>());
    for (ConfigElement renamedG : renamedGroups) {
      ConfigRenameElement rename = (ConfigRenameElement) renamedG;
      if (rename.getNewName().equals(newName)) {
        return rename.getOldName();
      }
    }
    return null;
  }

  public boolean isOntologyLocationSet() {
    return configuration.containsKey(WeaselConfigKeys.ONTOLOGY_URL)
        || configuration.containsKey(WeaselConfigKeys.ONTOLOGY_PATH);
  }

  public boolean isOntologyLocationURL() {
    return configuration.containsKey(WeaselConfigKeys.ONTOLOGY_URL);
  }

  
  public String getURLOntology() {
    Set<ConfigElement> elements = configuration.get(WeaselConfigKeys.ONTOLOGY_URL);
    for (ConfigElement element : elements) {
      return element.toString();
    }
    return null;
  }

  public String getPathOntology() {
    Set<ConfigElement> elements = configuration.get(WeaselConfigKeys.ONTOLOGY_PATH);
    for (ConfigElement element : elements) {
      return element.toString();
    }
    return null;
  }

  //TODO: Change this method name..
  /**
   *
   * @param uri - String representation of URI
   * @return True if it finds representation in the configuration, otherwise false.
   */
  public Boolean isUriIri(String uri) {
    Set<ConfigElement> scopeIri = configuration.getOrDefault(WeaselConfigKeys.SCOPE_IRI, new HashSet<>());
    for (ConfigElement configElement : scopeIri) {
      ConfigStringElement element = (ConfigStringElement) configElement;
      if (uri.contains(element.toString())) {
        return Boolean.TRUE;
      }
    }
    return Boolean.FALSE;
  }

}
