package org.edmcouncil.spec.ontoviewer.configloader.configuration.service;

import java.io.IOException;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData;

public interface ApplicationConfigurationService {

  void init() throws IOException;

  ConfigurationData getConfigurationData();

  boolean hasConfiguredGroups();

  void reloadConfiguration() throws IOException;
}
