package org.edmcouncil.spec.ontoviewer.configloader.configuration.service;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigItemType;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.CoreConfiguration;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.GroupType;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.GroupsPropertyKey;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.BooleanItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.GroupsItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.StringItem;

public class MemoryBasedConfigurationService implements ConfigurationService {

  private final CoreConfiguration configuration;

  public MemoryBasedConfigurationService() {
    configuration = new CoreConfiguration();
    configuration.addConfigElement(ConfigKeys.DISPLAY_LABEL, new BooleanItem(true));
    configuration.addConfigElement(ConfigKeys.FORCE_LABEL_LANG, new BooleanItem(false));
    configuration.addConfigElement(ConfigKeys.LABEL_LANG, new StringItem("en"));

    var glossary = new GroupsItem();
    glossary.setName(GroupsPropertyKey.GLOSSARY.getKey());
    glossary.setGroupType(GroupType.DEFAULT);
    glossary.addElement(new StringItem("https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/synonym"));
    glossary.addElement(new StringItem("http://www.w3.org/2004/02/skos/core#definition"));
    glossary.addElement(new StringItem("http://www.w3.org/2004/02/skos/core#example"));
    glossary.addElement(new StringItem("http://www.w3.org/2004/02/skos/core#editorialNote"));
    configuration.addConfigElement(ConfigKeys.GROUPS, glossary);

    // TODO: Add more default options
  }

  @Override
  public CoreConfiguration getCoreConfiguration() {
    return this.configuration;
  }
}
