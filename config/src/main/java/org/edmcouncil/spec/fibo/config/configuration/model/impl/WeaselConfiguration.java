package org.edmcouncil.spec.fibo.config.configuration.model.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.edmcouncil.spec.fibo.config.configuration.model.ConfigElement;
import org.edmcouncil.spec.fibo.config.configuration.model.Configuration;
import org.edmcouncil.spec.fibo.config.configuration.model.WeaselConfigKeys;

/**
 * Create by Michał Daniel (michal.daniel@makolab.com)
 */
public class WeaselConfiguration implements Configuration<Set<ConfigElement>> {

  private Map<String, Set<ConfigElement>> configuration;

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

//
//    for (ConfigElement ConfigElement : l){
//      ConfigRenameElement element = (ConfigRenameElement) l;
//      if (element.getOLD_NAME() != null && !element.getNEW_NAME().isEmpty()) {
//        if (element.getNEW_NAME().equals(element)) {
//          return element.NEW_NAME;
//        }
//      }
//    }
    return null;

  }
  
  public boolean hasRenamedGroups(){
    return configuration.get(WeaselConfigKeys.RENAME_GROUPS)!=null;
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

}
