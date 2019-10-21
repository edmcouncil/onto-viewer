package org.edmcouncil.spec.fibo.config.configuration.loader.saxparser;

import org.edmcouncil.spec.fibo.config.configuration.model.ConfigElement;
import org.edmcouncil.spec.fibo.config.configuration.model.ConfigElementType;
import org.edmcouncil.spec.fibo.config.configuration.model.Configuration;
import org.edmcouncil.spec.fibo.config.configuration.model.GroupType;
import org.edmcouncil.spec.fibo.config.configuration.model.WeaselConfigKeys;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ConfigBooleanElement;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ConfigGroupsElement;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ConfigMissingLanguageElement;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ConfigGroupLabelPriorityElement;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ConfigStringElement;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ConfigRenameElement;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.WeaselConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Configuration reader from xml file
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class WeaselConfigurationHandler extends DefaultHandler {

  private static Logger logger = LoggerFactory.getLogger(WeaselConfigurationHandler.class);

  private WeaselConfiguration config = new WeaselConfiguration();
  String key = null;
  String val = null;

  ConfigRenameElement cre = null;
  ConfigGroupsElement cge = null;

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    switch (qName) {
      case WeaselConfigKeys.PRIORITY_LIST:
      case WeaselConfigKeys.IGNORE_TO_DISPLAYING:
      case WeaselConfigKeys.IGNORE_TO_LINKING:
      case WeaselConfigKeys.SCOPE_IRI:
      case WeaselConfigKeys.GROUPS:
      case WeaselConfigKeys.RENAME_GROUPS:
      case WeaselConfigKeys.ONTOLOGY_URL:
      case WeaselConfigKeys.ONTOLOGY_PATH:
      case WeaselConfigKeys.DISPLAYED_LABELS:
      case WeaselConfigKeys.FORCE_LABEL_LANG:
      case WeaselConfigKeys.LABEL_LANG:
      case WeaselConfigKeys.GROUP_LABEL:
      case WeaselConfigKeys.MISSING_LANGUAGE_ACTION:
        this.key = qName;
        break;
      case WeaselConfigKeys.GROUP:
        cge = new ConfigGroupsElement();
        cge.setGroupType(GroupType.DEFAULT);
        break;
      case WeaselConfigKeys.RENAME:
        cre = new ConfigRenameElement();
        break;
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {

    switch (qName) {
      case WeaselConfigKeys.PRIORITY_LIST_ELEMENT:
      case WeaselConfigKeys.IGNORED_ELEMENT:
      case WeaselConfigKeys.URI_NAMESPACE:
      case WeaselConfigKeys.LABEL_LANG:
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
      case WeaselConfigKeys.OLD_NAME:
        cre.setOldName(val);
        break;
      case WeaselConfigKeys.NEW_NAME:
        cre.setNewName(val);
        break;
      case WeaselConfigKeys.RENAME:
        config.addCongigElement(key, cre);
        break;
      case WeaselConfigKeys.ONTOLOGY_URL:
        ConfigStringElement ontologyURL = new ConfigStringElement(val);
        config.addCongigElement(key, ontologyURL);
        break;
      case WeaselConfigKeys.ONTOLOGY_PATH:
        ConfigStringElement ontologyPath = new ConfigStringElement(val);
        config.addCongigElement(key, ontologyPath);
        break;
      case WeaselConfigKeys.DISPLAYED_LABELS:
      case WeaselConfigKeys.FORCE_LABEL_LANG:
        ConfigBooleanElement cbe = new ConfigBooleanElement();
        cbe.setType(ConfigElementType.BOOLEAN);
        cbe.setValue(Boolean.valueOf(val));
        config.addCongigElement(key, cbe);
        break;
      case WeaselConfigKeys.GROUP_LABEL:
        ConfigGroupLabelPriorityElement cpe = new ConfigGroupLabelPriorityElement();
        cpe.setType(ConfigElementType.PRIORITY);
        cpe.setValue(ConfigGroupLabelPriorityElement.GroupLabelPriority.valueOf(val));
        config.addCongigElement(key, cpe);
        break;

      case WeaselConfigKeys.MISSING_LANGUAGE_ACTION:
        ConfigMissingLanguageElement cmle = new ConfigMissingLanguageElement();
        cmle.setType(ConfigElementType.MISSING_LANGUAGE_ACTION);
        cmle.setValue(ConfigMissingLanguageElement.MissingLanguageAction.valueOf(val));
        config.addCongigElement(key, cmle);
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
