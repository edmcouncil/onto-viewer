package org.edmcouncil.spec.ontoviewer.configloader.configuration.service;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData;

public class YamlMemoryBasedConfigurationService extends AbstractYamlConfigurationService {

  private final ConfigurationData configurationData;

  public YamlMemoryBasedConfigurationService() {
    this.configurationData = populateConfiguration();
  }

  @Override
  public ConfigurationData getConfigurationData() {
    return configurationData;
  }

  @Override
  public boolean hasConfiguredGroups() {
    return configurationData.getGroupsConfig().getGroups() != null
        && !configurationData.getGroupsConfig().getGroups().isEmpty();
  }

  private ConfigurationData populateConfiguration() {
    return readDefaultConfiguration();
  }
}