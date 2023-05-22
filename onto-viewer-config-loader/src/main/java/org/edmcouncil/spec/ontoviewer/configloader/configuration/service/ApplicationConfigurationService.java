package org.edmcouncil.spec.ontoviewer.configloader.configuration.service;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.exception.UnableToLoadRemoteConfigurationException;

public interface ApplicationConfigurationService {

  void init();

  ConfigurationData getConfigurationData();

  boolean hasConfiguredGroups();

  void reloadConfiguration() throws UnableToLoadRemoteConfigurationException;
}
