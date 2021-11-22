package org.edmcouncil.spec.ontoviewer.configloader.configuration.loader.saxparser;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import javax.xml.parsers.SAXParserFactory;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.CoreConfiguration;
import org.junit.jupiter.api.Test;

class ViewerCoreConfigurationHandlerTest {

  @Test
  void shouldReturnDefaultTrueValueForLocationInModulesEnabledWhenConfigIsEmpty() {
    var configuration = prepareConfiguration("ontology_config_empty.xml");
    var configItem = configuration.getOntologyHandling();

    var actualResult = (Boolean) configItem.get(ConfigKeys.LOCATION_IN_MODULES_ENABLED);

    assertTrue(actualResult);
  }

  @Test
  void shouldReturnDefaultTrueValueForUsageEnabledWhenConfigIsEmpty() {
    var configuration = prepareConfiguration("ontology_config_empty.xml");
    var configItem = configuration.getOntologyHandling();

    var actualResult = (Boolean) configItem.get(ConfigKeys.USAGE_ENABLED);

    assertTrue(actualResult);
  }

  @Test
  void shouldReturnDefaultTrueValueForOntologyGraphEnabledWhenConfigIsEmpty() {
    var configuration = prepareConfiguration("ontology_config_empty.xml");
    var configItem = configuration.getOntologyHandling();

    var actualResult = (Boolean) configItem.get(ConfigKeys.ONTOLOGY_GRAPH_ENABLED);

    assertTrue(actualResult);
  }

  @Test
  void shouldReturnFalseValueForLocationInModulesEnabledWhenConfigIsSet() {
    var configuration = prepareConfiguration("ontology_config_all_false.xml");
    var configItem = configuration.getOntologyHandling();

    var actualResult = (Boolean) configItem.get(ConfigKeys.LOCATION_IN_MODULES_ENABLED);

    assertFalse(actualResult);
  }

  @Test
  void shouldReturnFalseValueForUsageEnabledWhenConfigIsSet() {
    var configuration = prepareConfiguration("ontology_config_all_false.xml");
    var configItem = configuration.getOntologyHandling();

    var actualResult = (Boolean) configItem.get(ConfigKeys.USAGE_ENABLED);

    assertFalse(actualResult);
  }

  @Test
  void shouldReturnFalseValueForOntologyGraphEnabledWhenConfigIsSet() {
    var configuration = prepareConfiguration("ontology_config_all_false.xml");
    var configItem = configuration.getOntologyHandling();

    var actualResult = (Boolean) configItem.get(ConfigKeys.ONTOLOGY_GRAPH_ENABLED);

    assertFalse(actualResult);
  }

  @Test
  void shouldReturnTrueValueForLocationInModulesEnabledWhenConfigIsSet() {
    var configuration = prepareConfiguration("ontology_config_all_true.xml");
    var configItem = configuration.getOntologyHandling();

    var actualResult = (Boolean) configItem.get(ConfigKeys.LOCATION_IN_MODULES_ENABLED);

    assertTrue(actualResult);
  }

  @Test
  void shouldReturnTrueValueForUsageEnabledWhenConfigIsSet() {
    var configuration = prepareConfiguration("ontology_config_all_true.xml");
    var configItem = configuration.getOntologyHandling();

    var actualResult = (Boolean) configItem.get(ConfigKeys.USAGE_ENABLED);

    assertTrue(actualResult);
  }

  @Test
  void shouldReturnTrueValueForOntologyGraphEnabledWhenConfigIsSet() {
    var configuration = prepareConfiguration("ontology_config_all_true.xml");
    var configItem = configuration.getOntologyHandling();

    var actualResult = (Boolean) configItem.get(ConfigKeys.ONTOLOGY_GRAPH_ENABLED);

    assertTrue(actualResult);
  }

  private CoreConfiguration prepareConfiguration(String configPath) {
    var coreConfiguration = new CoreConfiguration();
    var configurationHandler = new ViewerCoreConfigurationHandler(coreConfiguration);
    var configFile = getClass().getResource("/configuration_tests/" + configPath).getFile();

    try {
      var saxParserFactory = SAXParserFactory.newInstance();
      var saxParser = saxParserFactory.newSAXParser();
      saxParser.parse(configFile, configurationHandler);
    } catch (Exception ex) {
      fail("Exception thrown while reading configuration file. Details: " + ex.getMessage());
    }

    return configurationHandler.getConfiguration();
  }
}