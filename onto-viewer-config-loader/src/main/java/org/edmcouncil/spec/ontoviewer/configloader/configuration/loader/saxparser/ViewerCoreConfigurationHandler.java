package org.edmcouncil.spec.ontoviewer.configloader.configuration.loader.saxparser;

import java.util.HashMap;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigItemType;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.FindProperty;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.GroupType;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.KeyValueMapConfigItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.BooleanItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.GroupsItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.MissingLanguageItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.LabelPriority;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.StringItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.RenameItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.CoreConfiguration;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.DefaultLabelItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.searcher.SearcherField;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.searcher.TextSearcherConfig;
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
public class ViewerCoreConfigurationHandler extends DefaultHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ViewerCoreConfigurationHandler.class);

  private CoreConfiguration configuration;
  private final Map<String, Object> ontologyHandling = new HashMap<>();

  String key = null;
  String val = null;

  RenameItem cre = null;
  GroupsItem cge = null;
  DefaultLabelItem dli = null;
  TextSearcherConfig tsc = null;
  SearcherField sf = null;
  FindProperty findProperty = null;

  public ViewerCoreConfigurationHandler(CoreConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) {
    switch (qName) {
      case ConfigKeys.ONTOLOGY_ROOT:
        int attributeLength = attributes.getLength();
        if(attributeLength>0 && attributes.getQName(0).equals(ConfigKeys.ONTOLOGY_DOWNLOAD_DIR)) {
          configuration.addConfigElement(ConfigKeys.ONTOLOGY_DOWNLOAD_DIR, new StringItem(attributes.getValue(0)));
        }
        break;
      case ConfigKeys.PRIORITY_LIST:
      case ConfigKeys.IGNORE_TO_DISPLAYING:
      case ConfigKeys.IGNORE_TO_LINKING:
      case ConfigKeys.SCOPE_IRI:
      case ConfigKeys.GROUPS:
      case ConfigKeys.ONTOLOGY_URL:
      case ConfigKeys.ONTOLOGY_PATH:
      case ConfigKeys.ONTOLOGY_DIR:
      case ConfigKeys.ONTOLOGY_MAPPER:
      case ConfigKeys.ONTOLOGY_CATALOG_PATH:
      case ConfigKeys.ONTOLOGY_ZIP_URL:
      case ConfigKeys.DISPLAY_LABEL:
      case ConfigKeys.FORCE_LABEL_LANG:
      case ConfigKeys.LABEL_LANG:
      case ConfigKeys.LABEL_PRIORITY:
      case ConfigKeys.MISSING_LANGUAGE_ACTION:
      case ConfigKeys.USER_DEFAULT_NAME_LIST:
      case ConfigKeys.ONTOLOGY_MODULE_TO_IGNORE:
      case ConfigKeys.ONTOLOGY_MODULE_IGNORE_PATTERN:
        this.key = qName;
        break;
      case ConfigKeys.GROUP:
        cge = new GroupsItem();
        cge.setGroupType(GroupType.DEFAULT);
        break;
      case ConfigKeys.USER_DEFINED_NAME:
        dli = new DefaultLabelItem();
        break;
      case ConfigKeys.TEXT_SEARCH_CONFIG:
        this.key = qName;
        tsc = new TextSearcherConfig();
        break;
      case ConfigKeys.HINT_FIELD:
      case ConfigKeys.SEARCH_FIELD:
        sf = new SearcherField();
        break;
      case ConfigKeys.LOCATION_IN_MODULES_ENABLED:
      case ConfigKeys.USAGE_ENABLED:
      case ConfigKeys.ONTOLOGY_GRAPH_ENABLED:
      case ConfigKeys.INDIVIDUALS_ENABLED:
        this.key = qName;
        break;
      case ConfigKeys.FIND_PROPERTY:
        this.findProperty = new FindProperty();
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
      case ConfigKeys.ONTOLOGY_MODULE_TO_IGNORE:
        if (!val.trim().isEmpty()) {
          ConfigItem configEl = new StringItem(val);
          configuration.addConfigElement(key, configEl);
          configEl = null;
        }
        break;
      case ConfigKeys.GROUP:
        configuration.addConfigElement(key, cge);
        break;
      case ConfigKeys.GROUP_NAME:
        cge.setName(val);
        break;
      case ConfigKeys.GROUP_ITEM:
        cge.addElement(new StringItem(val));
        break;
      case ConfigKeys.ONTOLOGY_URL:
      case ConfigKeys.ONTOLOGY_DIR:
      case ConfigKeys.ONTOLOGY_PATH:
      case ConfigKeys.ONTOLOGY_MAPPER:
      case ConfigKeys.ONTOLOGY_CATALOG_PATH:
      case ConfigKeys.ONTOLOGY_MODULE_IGNORE_PATTERN:
      case ConfigKeys.ONTOLOGY_ZIP_URL:
        StringItem ontologyPath = new StringItem(val);
        configuration.addConfigElement(key, ontologyPath);
        ontologyPath = null;
        break;
      case ConfigKeys.DISPLAY_LABEL:
      case ConfigKeys.FORCE_LABEL_LANG:
        BooleanItem cbe = new BooleanItem();
        cbe.setType(ConfigItemType.BOOLEAN);
        cbe.setValue(Boolean.valueOf(val));
        configuration.addConfigElement(key, cbe);
        cbe = null;
        break;
      case ConfigKeys.LABEL_PRIORITY:
        LabelPriority cpe = new LabelPriority();
        cpe.setType(ConfigItemType.PRIORITY);
        cpe.setValue(LabelPriority.Priority.valueOf(val));
        configuration.addConfigElement(key, cpe);
        cpe = null;
        break;
      case ConfigKeys.MISSING_LANGUAGE_ACTION:
        MissingLanguageItem cmle = new MissingLanguageItem();
        cmle.setType(ConfigItemType.MISSING_LANGUAGE_ACTION);
        cmle.setValue(MissingLanguageItem.Action.valueOf(val));
        configuration.addConfigElement(key, cmle);
        cmle = null;
        break;
      case ConfigKeys.RESOURCE_IRI_TO_NAME:
        dli.setIri(val);
        break;
      case ConfigKeys.RESOURCE_IRI_NAME:
        dli.setLabel(val);
        break;
      case ConfigKeys.USER_DEFINED_NAME:
        configuration.addConfigElement(key, dli);
        break;
      case ConfigKeys.HINT_FIELD:
        tsc.addHintField(sf);
        break;
      case ConfigKeys.SEARCH_FIELD:
        tsc.addSearchField(sf);
        break;
      case ConfigKeys.FIELD_IRI:
        sf.setIri(val);
        break;
      case ConfigKeys.FIELD_BOOST:
        sf.setBoost(Double.valueOf(val));
        break;
      case ConfigKeys.HINT_THRESHOLD:
        tsc.setHintThreshold(Double.valueOf(val));
        break;
      case ConfigKeys.SEARCH_THRESHOLD:
        tsc.setSearchThreshold(Double.valueOf(val));
        break;
      case ConfigKeys.SEARCH_DESCRIPTION:
        tsc.addSearchDescription(val);
        break;
      case ConfigKeys.TEXT_SEARCH_CONFIG:
        configuration.addConfigElement(key, tsc);
        tsc = null;
        break;
      case ConfigKeys.HINT_LEVENSTEIN_DISTANCE:
        tsc.setHintMaxLevensteinDistance(Double.valueOf(val));
        break;
      case ConfigKeys.SEARCH_LEVENSTEIN_DISTANCE:
        tsc.setSearchMaxLevensteinDistance(Double.valueOf(val));
        break;
      case ConfigKeys.FUZZY_DISTANCE:
        tsc.setFuzzyDistance(Integer.parseInt(val));
        break;
      case ConfigKeys.REINDEX_ON_START:
        tsc.setReindexOnStart(Boolean.parseBoolean(val));
        break;
      case ConfigKeys.ONTOLOGY_HANDLING:
        var configItem = new KeyValueMapConfigItem(ontologyHandling);
        configuration.addConfigElement(ConfigKeys.ONTOLOGY_HANDLING, configItem);
        break;
      case ConfigKeys.LOCATION_IN_MODULES_ENABLED:
      case ConfigKeys.USAGE_ENABLED:
      case ConfigKeys.ONTOLOGY_GRAPH_ENABLED:
      case ConfigKeys.INDIVIDUALS_ENABLED:
        var booleanValue = Boolean.valueOf(val);
        ontologyHandling.put(key, booleanValue);
        break;
      case ConfigKeys.FIND_PROPERTY:
        tsc.addFindProperty(findProperty);
        break;
      case ConfigKeys.FIND_PROPERTY_IRI:
        findProperty.setIri(val);
        break;
      case ConfigKeys.FIND_PROPERTY_LABEL:
        findProperty.setLabel(val);
        break;
      case ConfigKeys.FIND_PROPERTY_IDENTIFIER:
        findProperty.setIdentifier(val);
        break;
    }
  }

  @Override
  public void characters(char ch[], int start, int length) throws SAXException {
    val = new String(ch, start, length);
  }

  public CoreConfiguration getConfiguration() {
    return this.configuration;
  }
}
