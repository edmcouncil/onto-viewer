package org.edmcouncil.spec.ontoviewer.configloader.configuration.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.CoreConfiguration;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.FindProperty;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.GroupType;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.GroupsPropertyKey;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.KeyValueMapConfigItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.BooleanItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.GroupsItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.StringItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.searcher.TextSearcherConfig;

public class MemoryBasedConfigurationService implements ConfigurationService {

  private static final Set<String> THIS_ONTOLOGY_CONTAINS_PROPERTIES = new HashSet<>();
  private static final Set<String> ONTOLOGICAL_CHARACTERISTIC_PROPERTIES = new HashSet<>();
  private static final List<FindProperty> FIND_PROPERTIES = new ArrayList<>();

  static {
    THIS_ONTOLOGY_CONTAINS_PROPERTIES.addAll(
        Arrays.asList(
            "@viewer.internal.clazz",
            "@viewer.external.clazz",
            "@viewer.internal.objectProperty",
            "@viewer.external.objectProperty",
            "@viewer.internal.dataProperty",
            "@viewer.external.dataProperty",
            "@viewer.internal.instance",
            "@viewer.external.instance",
            "@viewer.internal.annotationProperty",
            "@viewer.external.annotationProperty"));

    ONTOLOGICAL_CHARACTERISTIC_PROPERTIES.addAll(
        Arrays.asList(
            "@viewer.function.usage_classes",
            "@viewer.function.direct_sub_data_property",
            "@viewer.function.direct_sub_object_property",
            "@viewer.function.direct_sub_annotation_property",
            "@viewer.function.direct_subclasses",
            "@viewer.function.instances",
            "@viewer.axiom.EquivalentClasses",
            "@viewer.axiom.SubClassOf",
            "@viewer.function.anonymous_ancestor",
            "@viewer.axiom.DisjointClasses",
            "@viewer.axiom.ClassAssertion",
            "@viewer.axiom.SameIndividual",
            "@viewer.axiom.ObjectPropertyAssertion",
            "@viewer.axiom.DataPropertyAssertion",
            "@viewer.axiom.SubObjectPropertyOf",
            "@viewer.axiom.SubDataPropertyOf",
            "@viewer.axiom.SubAnnotationPropertyOf",
            "@viewer.axiom.EquivalentObjectProperties",
            "@viewer.axiom.EquivalentDataProperties",
            "@viewer.axiom.ObjectPropertyDomain",
            "@viewer.axiom.DataPropertyDomain",
            "@viewer.axiom.ObjectPropertyRange",
            "@viewer.axiom.FunctionalObjectProperty",
            "@viewer.axiom.DataPropertyRange",
            "@viewer.axiom.AnnotationPropertyDomain",
            "@viewer.axiom.AnnotationPropertyRangeOf",
            "@viewer.axiom.InverseObjectProperties",
            "http://purl.org/dc/terms/hasPart",
            "http://www.omg.org/techprocess/ab/SpecificationMetadata/dependsOn"));

    FIND_PROPERTIES.addAll(
        Arrays.asList(
            new FindProperty("RDFS Label", "rdfs_label", "http://www.w3.org/2000/01/rdf-schema#label"),
            new FindProperty("SKOS Definition", "skos_definition", "http://www.w3.org/2004/02/skos/core#definition"),
            new FindProperty("FIBO Explanatory Note", "fibo_explanatoryNote", "https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/explanatoryNote"),
            new FindProperty("SKOS Note", "skos_note", "http://www.w3.org/2004/02/skos/core#note"),
            new FindProperty("FIBO Abbreviation", "fibo_abbreviation", "https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/abbreviation"),
            new FindProperty("FIBO Common Designation", "fibo_commonDesignation", "https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/commonDesignation"),
            new FindProperty("PURL Description", "purl_description", "http://purl.org/dc/terms/description"),
            new FindProperty("FIBO Synonym", "fibo_synonym", "https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/synonym"),
            new FindProperty("FIBO Preferred Designation", "fibo_preferredDesignation", "https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/preferredDesignation"),
            new FindProperty("SKOS Example", "skos_example", "http://www.w3.org/2004/02/skos/core#example"),
            new FindProperty("FIBO Usage Note", "fibo_usageNote", "https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/usageNote"),
            new FindProperty("SKOS Scope Note", "skos_scopeNote", "http://www.w3.org/2004/02/skos/core#scopeNote"),
            new FindProperty("FIBO Symbol", "fibo_symbol", "https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/symbol"),
            new FindProperty("SKOS Alt Label", "skos_altLabel", "https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/definitionOrigin")));
  }

  private final CoreConfiguration configuration;

  @Override
  public CoreConfiguration getCoreConfiguration() {
    return this.configuration;
  }
  public MemoryBasedConfigurationService() {
    configuration = new CoreConfiguration();
    configuration.addConfigElement(ConfigKeys.DISPLAY_LABEL, new BooleanItem(true));
    configuration.addConfigElement(ConfigKeys.FORCE_LABEL_LANG, new BooleanItem(false));
    configuration.addConfigElement(ConfigKeys.LABEL_LANG, new StringItem("en"));
    configuration.addConfigElement(ConfigKeys.ONTOLOGY_HANDLING, prepareOntologyHandlingConfig());
    configuration.addConfigElement(ConfigKeys.TEXT_SEARCH_CONFIG, prepareTextSearchConfig());

    var glossary = new GroupsItem();
    glossary.setName(GroupsPropertyKey.GLOSSARY.getKey());
    glossary.setGroupType(GroupType.DEFAULT);
    glossary.addElement(new StringItem("https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/synonym"));
    glossary.addElement(new StringItem("http://www.w3.org/2004/02/skos/core#definition"));
    glossary.addElement(new StringItem("http://www.w3.org/2004/02/skos/core#example"));
    glossary.addElement(new StringItem("http://www.w3.org/2004/02/skos/core#editorialNote"));
    glossary.addElement(new StringItem("https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/explanatoryNote"));
    configuration.addConfigElement(ConfigKeys.GROUPS, glossary);

    configuration.addConfigElement(ConfigKeys.GROUPS,
        populateConfiguration(
            GroupsPropertyKey.ONTOLOGICAL_CHARACTERISTIC,
            ONTOLOGICAL_CHARACTERISTIC_PROPERTIES));

    configuration.addConfigElement(ConfigKeys.GROUPS,
        populateConfiguration(
            GroupsPropertyKey.THIS_ONTOLOGY_CONTAINS,
            THIS_ONTOLOGY_CONTAINS_PROPERTIES));
  }

  private ConfigItem prepareTextSearchConfig() {
    var textSearcherConfig = new TextSearcherConfig();
    FIND_PROPERTIES.forEach(textSearcherConfig::addFindProperty);
    return textSearcherConfig;
  }

  private ConfigItem prepareOntologyHandlingConfig() {
    var properties = new HashMap<String, Object>();
    properties.put(ConfigKeys.LOCATION_IN_MODULES_ENABLED, false);
    properties.put(ConfigKeys.USAGE_ENABLED, false);
    properties.put(ConfigKeys.ONTOLOGY_GRAPH_ENABLED, false);
    properties.put(ConfigKeys.INDIVIDUALS_ENABLED, false);
    return new KeyValueMapConfigItem(properties);
  }

  private GroupsItem populateConfiguration(GroupsPropertyKey key, Set<String> properties) {
    var groupOfProperties = new GroupsItem();
    groupOfProperties.setName(key.getKey());
    groupOfProperties.setGroupType(GroupType.DEFAULT);
    for (var property : properties) {
      groupOfProperties.addElement(new StringItem(property));
    }
    return groupOfProperties;
  }

}
