package org.edmcouncil.spec.fibo.config.configuration.loader.saxparser;

import java.util.LinkedHashSet;
import java.util.Set;
import org.edmcouncil.spec.fibo.config.configuration.model.ConfigElement;
import org.edmcouncil.spec.fibo.config.configuration.model.ConfigElementType;
import org.edmcouncil.spec.fibo.config.configuration.model.Configuration;
import org.edmcouncil.spec.fibo.config.configuration.model.GroupType;
import org.edmcouncil.spec.fibo.config.configuration.model.WeaselConfigKeys;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ConfigGroupsElement;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ConfigStringElement;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.WeaselConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Configuration reader from xml file
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class WeaselConfigurationHandler extends DefaultHandler {

  private static Logger logger = LoggerFactory.getLogger(WeaselConfigurationHandler.class);

  private WeaselConfiguration config = new WeaselConfiguration();
  String key = null;
  String val = null;

  ConfigGroupsElement cge = null;
  Set<ConfigGroupsElement> groups = new LinkedHashSet<>();

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

    switch (qName) {
      case WeaselConfigKeys.PRIORITY_LIST:
      case WeaselConfigKeys.IGNORED_TO_DISPLAY:
      case WeaselConfigKeys.GROUPS:
        this.key = qName;
        break;
      case WeaselConfigKeys.GROUP:
        cge = new ConfigGroupsElement();
        cge.setGroupType(GroupType.DEFAULT);
        break;
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {

    switch (qName) {
      case WeaselConfigKeys.PRIORITY_LIST_ELEMENT:
      case WeaselConfigKeys.IGNORED_TO_DISPLAY_ELEMENT:
        if (!val.trim().isEmpty()) {
          ConfigElement configEl = new ConfigStringElement(val);

          config.addCongigElement(key, configEl);
        }
        break;
      case WeaselConfigKeys.GROUP:
        config.addCongigElement(key, cge);
        break;
      case WeaselConfigKeys.GROUP_NAME:
        cge.setName(val);
        break;
      case WeaselConfigKeys.GROUP_ELEMENT:
        cge.addElement(new ConfigStringElement(val));
        break;
      case WeaselConfigKeys.GROUP_TYPE:
       cge.setGroupType(GroupType.valueOf(val));
        break;
    }

  }

  @Override
  public void characters(char ch[], int start, int length) throws SAXException {
    val = new String(ch, start, length);
  }

  public Configuration getConfiguration() {
    return this.config;
  }
}
