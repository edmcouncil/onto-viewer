package org.edmcouncil.spec.ontoviewer.core.ontology.searcher.text;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData.SearchConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.searcher.SearcherField;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.searcher.TextSearcherConfig;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.OwlDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.ExtendedResult;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.SearchItem;
import org.edmcouncil.spec.ontoviewer.core.ontology.searcher.model.hint.HintItem;
import org.edmcouncil.spec.ontoviewer.core.utils.StringUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
@Component
public class TextSearcherDb {

  private static final Logger LOG = LoggerFactory.getLogger(TextSearcherDb.class);

  private Map<String, TextDbItem> db;

  private static final String LABEL_IRI = "http://www.w3.org/2000/01/rdf-schema#label";
  private static final String DEFINITION_IRI = "http://www.w3.org/2004/02/skos/core#definition";
  private static final String IRI_FRAGMENT = "@viewer.iri.fragment";

  private final Double HINT_THRESHOLD = 0.0d;
  private final Double RESULT_THRESHOLD = 0.0d;

  private final OntologyManager ontologyManager;
  private final LabelProvider labelProvider;
  private final OwlDataHandler owlDataHandler;
  private final ApplicationConfigurationService applicationConfigurationService;
 
  private TextSearcherConfig textSearcherConfig;

  @Inject
  public TextSearcherDb(OntologyManager ontologyManager,
      ApplicationConfigurationService applicationConfigurationService,
      LabelProvider labelProvider,
      OwlDataHandler owlDataHandler) {
    this.ontologyManager = ontologyManager;

    this.applicationConfigurationService = applicationConfigurationService;
    this.labelProvider = labelProvider;
    this.owlDataHandler = owlDataHandler;
  }

  @PostConstruct
  public void init() {
    var searchConfig = applicationConfigurationService.getConfigurationData().getSearchConfig();
    this.textSearcherConfig = checkAndLoadConfig(searchConfig);
  }

  public TextSearcherConfig checkAndLoadConfig(SearchConfig config) {
    if (config == null) {
      return loadDefaultConfiguration();
    } else {
      return loadConfiguration(config);
    }
  }

  public Map<String, TextDbItem> loadDefaultData(OWLOntology onto) {
    Map<String, TextDbItem> tmp = new HashMap<>();

    onto.signature(Imports.INCLUDED)
        .forEach(owlEntity -> collectEntityData(owlEntity, onto, tmp));

    onto.importsClosure().forEach(ontology -> collectOntologyData(ontology, tmp));

    return tmp;
  }

  private void collectEntityData(OWLEntity owlEntity, OWLOntology onto,
      Map<String, TextDbItem> newDb) {
    String entityIri = owlEntity.getIRI().toString();

    onto.importsClosure().forEach(ontology -> {
      var annotations = EntitySearcher
          .getAnnotations(owlEntity, ontology)
          .collect(Collectors.toSet());

      if (!annotations.isEmpty()) {
        var textDbItem = collectValues(annotations);
        textDbItem.addValue(IRI_FRAGMENT, StringUtils.getFragment(entityIri), null);
        newDb.put(entityIri, textDbItem);
      }
    });

    if (!newDb.containsKey(entityIri)) {
      var textDbItem = new TextDbItem();
      textDbItem.addValue(IRI_FRAGMENT, StringUtils.getFragment(entityIri), null);
      newDb.put(entityIri, textDbItem);
    }
  }

  private void collectOntologyData(OWLOntology owlOntology, Map<String, TextDbItem> tmp) {
    var annotations = owlOntology.annotations().collect(Collectors.toSet());
    TextDbItem tdi = collectValues(annotations);

    String entityIri = owlOntology.getOntologyID().getOntologyIRI().orElse(IRI.create(""))
        .toString();
    LOG.trace("Entity IRI: {}", entityIri);
    tdi.addValue(IRI_FRAGMENT, StringUtils.getFragment(entityIri), null);

    if (!tdi.isEmpty()) {
      tmp.put(entityIri, tdi);
    }
  }

  private TextDbItem collectValues(Set<OWLAnnotation> annotations) {
    TextDbItem tdi = new TextDbItem();
    annotations.forEach(annotation -> {
      String propertyIri = annotation.getProperty().getIRI().toString();
      if (textSearcherConfig.hasHintFieldWithIri(propertyIri) || textSearcherConfig.hasSearchFieldWithIri(propertyIri)) {
        LOG.trace("Find property: {}", propertyIri);
        Optional<OWLLiteral> literalOptional = annotation.annotationValue().literalValue();
        if (literalOptional.isPresent()) {
          String lang = literalOptional.get().getLang();
          if (applicationConfigurationService.getConfigurationData().getLabelConfig().isForceLabelLang()) {
            if (lang == null ||
                !lang.equals(applicationConfigurationService.getConfigurationData().getLabelConfig().getLabelLang())) {
              return;
            }
          }
          tdi.addValue(propertyIri, literalOptional.get().getLiteral(), lang);
          LOG.trace("Literal value: {}", literalOptional.get().getLiteral());
        }
      }
    });
    return tdi;
  }

  /**
   * Hint search. Return list of hints, based on text search on own prepared database.
   *
   * @param text
   * @param maxHintCount
   * @return
   */
  public List<HintItem> getHints(String text, Integer maxHintCount) {
    List<HintItem> result = new LinkedList<>();

    for (Map.Entry<String, TextDbItem> record : db.entrySet()) {
      Double relevancy = record.getValue().computeHintRelevancy(text, textSearcherConfig.getHintFields());
      if (relevancy > textSearcherConfig.getHintThreshold()) {
        HintItem hi = new HintItem();
        hi.setIri(record.getKey());
        hi.setRelevancy(relevancy);
        String hintLabel = getValue(record.getKey(), LABEL_IRI);
        if (hintLabel == null) {
          hintLabel = getValue(record.getKey(), IRI_FRAGMENT);
        }
        hi.setLabel(hintLabel);
        result.add(hi);
      }
    }
    if (result.isEmpty()) {
      for (Map.Entry<String, TextDbItem> record : db.entrySet()) {

        double distance = record.getValue().computeLevensteinDistance(text, textSearcherConfig.getHintFields());
        LOG.debug("TextSearcherDb -> Distance {} between {} and {}", distance, text,
            record.getKey());

        if (distance <= textSearcherConfig.getHintMaxLevensteinDistance() && distance > 0.0d) {

          HintItem hi = new HintItem();
          hi.setIri(record.getKey());
          hi.setRelevancy(distance);

          String hintLabel = getValue(record.getKey(), LABEL_IRI);
          if (hintLabel == null) {

            hintLabel = getValue(record.getKey(), IRI_FRAGMENT);
          }
          hi.setLabel(hintLabel);
          result.add(hi);
        }

      }

    }

    sortHints(result);

    result = cutHintResults(result, maxHintCount);

    return result;
  }

  private List<HintItem> cutHintResults(List<HintItem> result, Integer maxHintCount) {
    Integer endIndex = result.size() > maxHintCount ? maxHintCount : result.size();
    result = result.subList(0, endIndex);
    return result;
  }

  private void sortHints(List<HintItem> result) {
    Collections.sort(result, Comparator.comparing(HintItem::getRelevancy).reversed()
        .thenComparing(HintItem::getLabel).reversed());
    Collections.reverse(result);
  }

  /**
   * Advanced search. You can use different properties than the hint search (other fields,
   * threshholds, bost). Support simply paggination.
   *
   * @param text
   * @param maxResults
   * @param currentPage
   * @return Search result of extended search.
   */
  public ExtendedResult getSearchResult(String text, Integer maxResults, Integer currentPage) {
    ExtendedResult result = new ExtendedResult();

    List<SearchItem> listResult = new LinkedList<>();

    for (Map.Entry<String, TextDbItem> record : db.entrySet()) {
      Double relevancy = record.getValue().computeSearchRelevancy(text, textSearcherConfig.getSearchFields());
      if (relevancy > textSearcherConfig.getSearchThreshold()) {
        SearchItem si = getPreparedSearchResultItem(record, relevancy);        
        listResult.add(si);
      }
    }

    if (listResult.isEmpty()) {
      for (Map.Entry<String, TextDbItem> record : db.entrySet()) {
        Double relevancy = record.getValue()
            .computeLevensteinDistance(text, textSearcherConfig.getSearchFields());
        if (relevancy <= textSearcherConfig.getSearchMaxLevensteinDistance()) {
          SearchItem si = getPreparedSearchResultItem(record, relevancy);
          listResult.add(si);
        }
      }

    }

    int countOfResults = listResult.size();

    sortSearchResults(listResult);

    Integer startIndex = (currentPage - 1) * maxResults;
    if (startIndex > countOfResults) {
      return result;
    }

    Integer endIndex = (currentPage - 1) * maxResults + maxResults;
    endIndex = endIndex > countOfResults ? countOfResults : endIndex;

    //Cut results for approproate page
    listResult = listResult.subList(startIndex, endIndex);

    result.setResult(listResult);
    result.setPage(currentPage);
    result.setQuery(text);
    result.setHasMorePage(countOfResults > endIndex);
    result.setMaxPage(countOfResults / maxResults + (countOfResults % maxResults != 0 ? 1 : 0));
    result.setTotalResult(countOfResults);
    return result;
  }

  private void sortSearchResults(List<SearchItem> listResult) {
    Collections.sort(listResult, Comparator.comparing(SearchItem::getRelevancy).reversed()
        .thenComparing(SearchItem::getDescription).reversed());
    Collections.reverse(listResult);
  }

  private SearchItem getPreparedSearchResultItem(Map.Entry<String, TextDbItem> record,
      Double relevancy) {
    SearchItem si = new SearchItem();
    si.setIri(record.getKey());
    si.setRelevancy(relevancy);

    String label = getValue(record.getKey(), LABEL_IRI);
    if (label == null) {
      label = getValue(record.getKey(), IRI_FRAGMENT);
    }
    
    si.setMaturityLevel(owlDataHandler.getMaturityLevel(si.getIri()));
    String description = getDescription(record);
    si.setLabel(label);
    si.setDescription(StringUtils.cutString(description, 150, true));
    return si;
  }

  private String getDescription(Map.Entry<String, TextDbItem> record) {
    String description = null;
    LOG.debug("Look at record description: {}", record.getKey());
    for (String key : textSearcherConfig.getSearchDescriptions()) {
      description = getValue(record.getKey(), key);
      LOG.debug("Description for property {} : {}", key, description);
      if (description != null) {
        return description;
      }
    }
    return description;
  }

  private String getValue(String recordID, String recordProperty) {
    if (!textSearcherConfig.hasSearchFieldWithIri(recordProperty) && !db.containsKey(recordID)) {
      LOG.debug("Data doesn't present in structures.");
      return null;
    }

    for (TextDbItem.Item val : db.get(recordID).getValue()) {
      if (val.getType().equals(recordProperty)) {
        return val.getValue();
      }
    }
    LOG.debug("Data doesn't find in database.");
    return null;
  }

  private TextSearcherConfig loadConfiguration(SearchConfig searchConfig) {
    TextSearcherConfig textSearcherConfig = new TextSearcherConfig();
//    if (!textSearcherConfig.isCompleted()) {
//      if (textSearcherConfig.getHintFields().isEmpty()) {
//        SearcherField sf = new SearcherField();
//        sf.setIri(LABEL_IRI);
//        searchConfig.addHintField(sf);
//      }
//      if (searchConfig.getSearchFields().isEmpty()) {
//        SearcherField sf = new SearcherField();
//        sf.setIri(LABEL_IRI);
//        searchConfig.addSearchField(sf);
//        sf = new SearcherField();
//        sf.setIri(DEFINITION_IRI);
//        searchConfig.addSearchField(sf);
//
//        sf = new SearcherField();
//        sf.setIri(IRI_FRAGMENT);
//        searchConfig.addSearchField(sf);
//
//      }
//
//      if (searchConfig.getHintThreshold() == null) {
//        searchConfig.setHintThreshold(HINT_THRESHOLD);
//      }
//      if (searchConfig.getSearchThreshold() == null) {
//        searchConfig.setHintThreshold(RESULT_THRESHOLD);
//      }
//      if (searchConfig.getSearchDescriptions().isEmpty()) {
//        searchConfig.addSearchDescription(DEFINITION_IRI);
//      }
//    }
    return textSearcherConfig;
  }

  private TextSearcherConfig loadDefaultConfiguration() {

    TextSearcherConfig tsc = new TextSearcherConfig();
    tsc.setHintThreshold(HINT_THRESHOLD);
    tsc.setSearchThreshold(RESULT_THRESHOLD);
    SearcherField sf = new SearcherField();
    sf.setIri(LABEL_IRI);
    tsc.addHintField(sf);

    tsc.addSearchField(sf);

    sf = new SearcherField();
    sf.setIri(DEFINITION_IRI);
    tsc.addSearchField(sf);

    sf = new SearcherField();
    sf.setIri(IRI_FRAGMENT);
    tsc.addSearchField(sf);

    tsc.addSearchDescription(DEFINITION_IRI);

    return tsc;
  }

  public void clearAndSetDb(Map<String, TextDbItem> newDb) {
    if (db != null) {
      db.clear();
    }
    db = newDb;
  }
}
