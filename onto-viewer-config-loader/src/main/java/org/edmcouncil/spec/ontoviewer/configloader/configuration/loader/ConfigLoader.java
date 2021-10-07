package org.edmcouncil.spec.ontoviewer.configloader.configuration.loader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.loader.saxparser.ViewerCoreConfigurationHandler;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.CoreConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class ConfigLoader {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);

  CoreConfiguration configuration;

  public ConfigLoader() {
    this.configuration = new CoreConfiguration();
  }

  public void loadWeaselConfiguration(Path weaselConfigFile) {

    SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    try {
      File configFile = weaselConfigFile.toFile();
      if (!configFile.exists()) {

        LOG.debug("Configuration file not exist, use default empty configuration.");
        return;
      }
      SAXParser saxParser = saxParserFactory.newSAXParser();
      ViewerCoreConfigurationHandler handler = new ViewerCoreConfigurationHandler(configuration);
      saxParser.parse(configFile, handler);

      configuration = handler.getConfiguration();

    } catch (ParserConfigurationException | SAXException | IOException e) {
      LOG.error("Exception while loading configuration: {}", e.getMessage());
    }

    return;
  }

  public CoreConfiguration getConfiguration() {
    return configuration;
  }
  
  
}
