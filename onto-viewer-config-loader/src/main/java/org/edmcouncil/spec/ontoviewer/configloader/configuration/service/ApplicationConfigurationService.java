package org.edmcouncil.spec.ontoviewer.configloader.configuration.service;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData;

public interface ApplicationConfigurationService {

  void init();

  ConfigurationData getConfigurationData();

  boolean hasConfiguredGroups();

  void reloadConfiguration();
}
