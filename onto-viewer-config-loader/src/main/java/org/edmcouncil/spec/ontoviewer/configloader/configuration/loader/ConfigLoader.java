package org.edmcouncil.spec.ontoviewer.configloader.configuration.loader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.loader.saxparser.ViewerCoreConfigurationHandler;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.CoreConfiguration;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.StringItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.properties.AppProperties;


/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class ConfigLoader {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigLoader.class);

  final AppProperties appProperties;
  CoreConfiguration configuration;
  
  public ConfigLoader(AppProperties appProperties) {
    this.configuration = new CoreConfiguration();
    this.appProperties = appProperties;
  }

  public void loadViewerConfiguration(Path ontoViewerConfigurationPath) {
    SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    try {
      File configFile = ontoViewerConfigurationPath.toFile();
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
    
    loadCommandLineConfigValues(); 
  }

  public CoreConfiguration getConfiguration() {
    return configuration;
  }

  private void loadCommandLineConfigValues() {
    if(appProperties == null) return;
    
    String[] zipURL = appProperties.getZipURL();
    if(zipURL != null && zipURL.length>0){
      for (String url : Arrays.asList(zipURL)) {
        configuration.addConfigElement(ConfigKeys.ONTOLOGY_ZIP_URL, new StringItem(url));
      }
    }
    
    String[] ontologyCatalogPaths = appProperties.getOntologyCatalogPaths();
    if(ontologyCatalogPaths != null && ontologyCatalogPaths.length>0){
      for (String path : Arrays.asList(ontologyCatalogPaths)) {
        configuration.addConfigElement(ConfigKeys.ONTOLOGY_CATALOG_PATH, new StringItem(path));
      }
    }
  }
}