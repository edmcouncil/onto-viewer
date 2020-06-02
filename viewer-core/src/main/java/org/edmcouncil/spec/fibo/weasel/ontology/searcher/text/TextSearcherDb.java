package org.edmcouncil.spec.fibo.weasel.ontology.searcher.text;

import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.ExtendedResult;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.edmcouncil.spec.fibo.config.configuration.model.AppConfiguration;
import org.edmcouncil.spec.fibo.config.configuration.model.searcher.SearcherField;
import org.edmcouncil.spec.fibo.config.configuration.model.searcher.TextSearcherConfig;
import org.edmcouncil.spec.fibo.weasel.ontology.OntologyManager;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.SearchItem;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.hint.HintItem;
import org.edmcouncil.spec.fibo.weasel.utils.StringUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class TextSearcherDb {

  private static final Logger LOG = LoggerFactory.getLogger(TextSearcherDb.class);

  private Map<String, TextDbItem> db;

  private static final String LABEL_IRI = "http://www.w3.org/2000/01/rdf-schema#label";
  private static final String DEFINITION_IRI = "http://www.w3.org/2004/02/skos/core#definition";

  private final Double HINT_THRESHOLD = 0.0d;
  private final Double RESULT_THRESHOLD = 0.0d;
  @Autowired
  private OntologyManager om;
  private TextSearcherConfig conf;
  @Autowired
  private AppConfiguration appConfig;

  @Inject
  public TextSearcherDb(OntologyManager om) {
    this.om = om;

  }

  /**
   * Initialize database.
   */
  @PostConstruct
  public void init() {

    this.conf = appConfig.getViewerCoreConfig().getTextSearcherConfig();

    if (conf == null) {
      loadDefaultConfiguration();
    } else {
      loadConfiguration(conf);
    }

    //TODO: move this into configuration, default is only label to search
    LOG.info("Start initialize TextSearcherDB");
    db = new HashMap<>();
    OWLOntology onto = om.getOntology();

    Stream<OWLEntity> entities = onto.signature();

    entities.collect(Collectors.toSet()).forEach((owlEntity) -> {
      collectEntityData(owlEntity, onto);
    });
    
    for (OWLOntology owlOntology : om.getOntology().getOWLOntologyManager().ontologies().collect(Collectors.toSet())) {
      collectOntologyData(owlOntology, onto);
    }

    LOG.info("End of initialize TextSearcherDB");
  }

  private void collectEntityData(OWLEntity owlEntity, OWLOntology onto) {
    Stream<OWLAnnotation> annotations = EntitySearcher.getAnnotations(owlEntity, onto);
    String entityIri = owlEntity.getIRI().toString();
    LOG.trace("Entity IRI: {}", entityIri);
    TextDbItem tdi = collectValues(annotations);

    if (!tdi.isEmpty()) {
      db.put(entityIri, tdi);
    }
  }
  private void collectOntologyData(OWLOntology owlOntology, OWLOntology onto) {
    Stream<OWLAnnotation> annotations = owlOntology.annotations();
    String entityIri = owlOntology.getOntologyID().getOntologyIRI().orElse(IRI.create("")).toString();
    LOG.trace("Entity IRI: {}", entityIri);
    TextDbItem tdi = collectValues(annotations);

    if (!tdi.isEmpty()) {
      db.put(entityIri, tdi);
    }
  }

  private TextDbItem collectValues(Stream<OWLAnnotation> annotations) {
    TextDbItem tdi = new TextDbItem();
    annotations.collect(Collectors.toSet()).forEach((annotation) -> {
      String propertyIri = annotation.getProperty().getIRI().toString();
      if (conf.hasHintFieldWithIri(propertyIri)
          || conf.hasSearchFieldWithIri(propertyIri)) {
        LOG.trace("Find property: {}", propertyIri);
        Optional<OWLLiteral> opt = annotation.annotationValue().literalValue();
        if (opt.isPresent()) {
          tdi.addValue(propertyIri, opt.get().getLiteral());
          LOG.trace("Literal value: {}", opt.get().getLiteral());
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
      Double relevancy = record.getValue().computeHintRelevancy(text, conf.getHintFields());
      if (relevancy > conf.getHintThreshold()) {
        HintItem hi = new HintItem();
        hi.setIri(record.getKey());
        hi.setRelevancy(relevancy);
        hi.setLabel(getValue(record.getKey(), LABEL_IRI));
        result.add(hi);
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
      Double relevancy = record.getValue().computeSearchRelevancy(text, conf.getSearchFields());
      if (relevancy > conf.getSearchThreshold()) {
        SearchItem si = getPreparedSearchResultItem(record, relevancy);
        listResult.add(si);
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

    return result;
  }

  private void sortSearchResults(List<SearchItem> listResult) {
    Collections.sort(listResult, Comparator.comparing(SearchItem::getRelevancy).reversed()
        .thenComparing(SearchItem::getDescription).reversed());
    Collections.reverse(listResult);
  }

  private SearchItem getPreparedSearchResultItem(Map.Entry<String, TextDbItem> record, Double relevancy) {
    SearchItem si = new SearchItem();
    si.setIri(record.getKey());
    si.setRelevancy(relevancy);
    String label = getValue(record.getKey(), LABEL_IRI);
    String description = getDescription(record);
    si.setLabel(label);
    si.setDescription(StringUtils.cutString(description, 150, true));
    return si;
  }

  private String getDescription(Map.Entry<String, TextDbItem> record) {
    String description = null;
    LOG.debug("Look at record description: {}", record.getKey());
    for (String key : conf.getSearchDescriptions()) {
      description = getValue(record.getKey(), key);
      LOG.debug("Description for property {} : {}", key, description);
      if (description != null) {
        return description;
      }
    }
    return description;
  }

  private String getValue(String recordID, String recordProperty) {
    if (!conf.hasSearchFieldWithIri(recordProperty) && !db.containsKey(recordID)) {
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

  private void loadConfiguration(TextSearcherConfig tsc) {

    if (!tsc.isCompleted()) {
      if (tsc.getHintFields().isEmpty()) {
        SearcherField sf = new SearcherField();
        sf.setIri(LABEL_IRI);
        tsc.addHintField(sf);
      }
      if (tsc.getSearchFields().isEmpty()) {
        SearcherField sf = new SearcherField();
        sf.setIri(LABEL_IRI);
        tsc.addSearchField(sf);
        sf = new SearcherField();
        sf.setIri(DEFINITION_IRI);
        tsc.addSearchField(sf);
      }
      if (tsc.getHintThreshold() == null) {
        tsc.setHintThreshold(HINT_THRESHOLD);
      }
      if (tsc.getSearchThreshold() == null) {
        tsc.setHintThreshold(RESULT_THRESHOLD);
      }
      if (tsc.getSearchDescriptions().isEmpty()) {
        tsc.addSearchDescription(DEFINITION_IRI);
      }
    }
    this.conf = tsc;
  }

  private void loadDefaultConfiguration() {

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

    tsc.addSearchDescription(DEFINITION_IRI);

    this.conf = tsc;
  }
}
