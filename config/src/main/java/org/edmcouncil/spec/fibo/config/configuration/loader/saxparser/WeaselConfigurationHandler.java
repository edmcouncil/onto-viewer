package org.edmcouncil.spec.fibo.config.configuration.loader.saxparser;

import org.edmcouncil.spec.fibo.config.configuration.model.ConfigItemType;
import org.edmcouncil.spec.fibo.config.configuration.model.Configuration;
import org.edmcouncil.spec.fibo.config.configuration.model.GroupType;
import org.edmcouncil.spec.fibo.config.configuration.model.ConfigKeys;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.element.BooleanItem;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.element.GroupsItem;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.element.MissingLanguageItem;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.element.GroupLabelPriorityItem;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.element.StringItem;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.element.RenameItem;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.WeaselConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.edmcouncil.spec.fibo.config.configuration.model.ConfigItem;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.element.DefaultLabelItem;

/**
 * Configuration reader from xml file
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class WeaselConfigurationHandler extends DefaultHandler {

  private static final Logger LOG = LoggerFactory.getLogger(WeaselConfigurationHandler.class);

  private WeaselConfiguration config = new WeaselConfiguration();
  String key = null;
  String val = null;

  RenameItem cre = null;
  GroupsItem cge = null;
  DefaultLabelItem dli = null;

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    switch (qName) {
      case ConfigKeys.PRIORITY_LIST:
      case ConfigKeys.IGNORE_TO_DISPLAYING:
      case ConfigKeys.IGNORE_TO_LINKING:
      case ConfigKeys.SCOPE_IRI:
      case ConfigKeys.GROUPS:
      case ConfigKeys.RENAME_GROUPS:
      case ConfigKeys.ONTOLOGY_URL:
      case ConfigKeys.ONTOLOGY_PATH:
      case ConfigKeys.DISPLAYED_LABELS:
      case ConfigKeys.FORCE_LABEL_LANG:
      case ConfigKeys.LABEL_LANG:
      case ConfigKeys.GROUP_LABEL:
      case ConfigKeys.MISSING_LANGUAGE_ACTION:
      case ConfigKeys.DEFAULT_LABEL_LIST:
        this.key = qName;
        break;
      case ConfigKeys.GROUP:
        cge = new GroupsItem();
        cge.setGroupType(GroupType.DEFAULT);
        break;
      case ConfigKeys.RENAME:
        cre = new RenameItem();
        break;
      case ConfigKeys.DEFAULT_LABEL_DEF:
        dli = new DefaultLabelItem();
        break;
      
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {

    switch (qName) {
      case ConfigKeys.PRIORITY_LIST_ELEMENT:
      case ConfigKeys.IGNORED_ELEMENT:
      case ConfigKeys.URI_NAMESPACE:
      case ConfigKeys.LABEL_LANG:
        if (!val.trim().isEmpty()) {
          ConfigItem configEl = new StringItem(val);
          config.addCongigElement(key, configEl);
        }
        break;
      case ConfigKeys.GROUP:
        config.addCongigElement(key, cge);
        break;
      case ConfigKeys.GROUP_NAME:
        cge.setName(val);
        break;
      case ConfigKeys.GROUP_ELEMENT:
        cge.addElement(new StringItem(val));
        break;
      case ConfigKeys.GROUP_TYPE:
        cge.setGroupType(GroupType.valueOf(val));
        break;
      case ConfigKeys.OLD_NAME:
        cre.setOldName(val);
        break;
      case ConfigKeys.NEW_NAME:
        cre.setNewName(val);
        break;
      case ConfigKeys.RENAME:
        config.addCongigElement(key, cre);
        break;
      case ConfigKeys.ONTOLOGY_URL:
        StringItem ontologyURL = new StringItem(val);
        config.addCongigElement(key, ontologyURL);
        break;
      case ConfigKeys.ONTOLOGY_PATH:
        StringItem ontologyPath = new StringItem(val);
        config.addCongigElement(key, ontologyPath);
        break;
      case ConfigKeys.DISPLAYED_LABELS:
      case ConfigKeys.FORCE_LABEL_LANG:
        BooleanItem cbe = new BooleanItem();
        cbe.setType(ConfigItemType.BOOLEAN);
        cbe.setValue(Boolean.valueOf(val));
        config.addCongigElement(key, cbe);
        break;
      case ConfigKeys.GROUP_LABEL:
        GroupLabelPriorityItem cpe = new GroupLabelPriorityItem();
        cpe.setType(ConfigItemType.PRIORITY);
        cpe.setValue(GroupLabelPriorityItem.Priority.valueOf(val));
        config.addCongigElement(key, cpe);
        break;

      case ConfigKeys.MISSING_LANGUAGE_ACTION:
        MissingLanguageItem cmle = new MissingLanguageItem();
        cmle.setType(ConfigItemType.MISSING_LANGUAGE_ACTION);
        cmle.setValue(MissingLanguageItem.Action.valueOf(val));
        config.addCongigElement(key, cmle);
        break;
      case ConfigKeys.DEFAULT_LABEL_IRI:
        dli.setIri(val);
        break;
      case ConfigKeys.DEFAULT_LABEL_VALUE:
        dli.setLabel(val);
        break;
      case ConfigKeys.DEFAULT_LABEL_DEF:
        config.addCongigElement(key, dli);
    }
  }

  @Override
  public void characters(char ch[], int start, int length) throws SAXException {
    val = new String(ch, start, length);
  }

  public WeaselConfiguration getConfiguration() {
    return this.config;
  }
}
