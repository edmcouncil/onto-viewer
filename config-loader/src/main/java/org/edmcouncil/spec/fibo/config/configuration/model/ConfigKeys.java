package org.edmcouncil.spec.fibo.config.configuration.model;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class ConfigKeys {

  public static final String PRIORITY_LIST = "priorityList";
  public static final String PRIORITY_LIST_ELEMENT = "priority";
  public static final String IGNORE_TO_DISPLAYING = "ignoreToDisplaying";
  public static final String IGNORE_TO_LINKING = "ignoreToLinking";
  public static final String IGNORED_ELEMENT = "ignore";

  //groups
  public static final String GROUPS = "groups";
  public static final String GROUP = "group";
  public static final String GROUP_NAME = "groupName";
  public static final String GROUP_ITEM = "groupItem";

  //ontology
  public static final String ONTOLOGY_URL = "ontologyURL";
  public static final String ONTOLOGY_PATH = "ontologyPath";
  public static final String ONTOLOGY_DIR = "ontologyDir";
  public static final String ONTOLOGY_MAPPER = "ontologyMapper";

  //uri / iri
  public static final String SCOPE_IRI = "scopeIri";
  public static final String URI_NAMESPACE = "uriNamespace";

  //labels configuration
  public static final String DISPLAY_LABEL = "displayLabel";
  public static final String LABEL_PRIORITY = "labelPriority";
  public static final String LABEL_LANG = "labelLang";
  public static final String FORCE_LABEL_LANG = "forceLabelLang";
  public static final String MISSING_LANGUAGE_ACTION = "missingLanguageAction";
  public static final String USER_DEFAULT_NAME_LIST = "userDefaultNameList";
  public static final String USER_DEFINED_NAME = "userDefinedName";
  public static final String RESOURCE_IRI_TO_NAME = "resourceIriToName";
  public static final String RESOURCE_IRI_NAME = "resourceIriName";

  // text search configuration
  public static final String TEXT_SEARCH_CONFIG = "searchConfig";
  public static final String HINT_FIELDS = "hintFields";
  public static final String HINT_FIELD = "hintField";
  public static final String HINT_THRESHOLD = "hintThreshold";
  public static final String SEARCH_FIELDS = "searchFields";
  public static final String SEARCH_FIELD = "searchField";
  public static final String SEARCH_THRESHOLD = "searchThreshold";
  public static final String SEARCH_DESCRIPTION = "searchDescription";
  public static final String SEARCH_DESCRIPTIONS = "searchDescriptions";
  public static final String FIELD_IRI = "fieldIri";
  public static final String FIELD_BOOST = "fieldBoost";
  public static final String HINT_LEVENSTEIN_DISTANCE = "hintLevensteinDistance";
  public static final String SEARCH_LEVENSTEIN_DISTANCE = "searchLevensteinDistance";
}
