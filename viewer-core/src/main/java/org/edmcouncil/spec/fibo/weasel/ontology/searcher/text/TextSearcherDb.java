package org.edmcouncil.spec.fibo.weasel.ontology.searcher.text;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.edmcouncil.spec.fibo.weasel.ontology.OntologyManager;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.SearchItem;
import org.edmcouncil.spec.fibo.weasel.ontology.searcher.model.hint.HintItem;
import org.edmcouncil.spec.fibo.weasel.utils.StringUtils;
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
  private static Set<String> labels;

  private static final String LABEL_STRING = "http://www.w3.org/2000/01/rdf-schema#label";
  private static final String DEFINITION_STRING = "http://www.w3.org/2004/02/skos/core#definition";

  private final Set<String> searchFields = new HashSet<>();
  private final Double RESULT_THRESHOLD;
  @Autowired
  private OntologyManager om;

  public TextSearcherDb() {
    RESULT_THRESHOLD = 0.0d;
    //labels field is used in hints 
    labels = new HashSet<>();
    labels.add(LABEL_STRING);

    searchFields.add(LABEL_STRING);
    searchFields.add(DEFINITION_STRING);

  }

  @PostConstruct
  public void init() {
    //TODO: move this into configuration, default is only label to search

    LOG.info("Start initialize TextSearcherDB");
    db = new HashMap<>();
    OWLOntology onto = om.getOntology();

    Stream<OWLEntity> entities = onto.signature();

    entities.collect(Collectors.toSet()).forEach((owlEntity) -> {
      Stream<OWLAnnotation> annotations = EntitySearcher.getAnnotations(owlEntity, onto);
      String entityIri = owlEntity.getIRI().toString();
      LOG.trace("Entity IRI: {}", entityIri);
      TextDbItem tdi = new TextDbItem();
      boolean emptyTdi = true;
      for (OWLAnnotation annotation : annotations.collect(Collectors.toSet())) {
        String propertyIri = annotation.getProperty().getIRI().toString();
        if (searchFields.contains(propertyIri)) {
          LOG.trace("Find property: {}", propertyIri);
          Optional<OWLLiteral> opt = annotation.annotationValue().literalValue();
          if (opt.isPresent()) {
            tdi.addValue(propertyIri, opt.get().getLiteral());
            emptyTdi = false;
            LOG.trace("Literal value: {}", opt.get().getLiteral());
          }
        }
      }

      if (!emptyTdi) {
        db.put(entityIri, tdi);
      }
    });

    LOG.info("End of initialize TextSearcherDB");
    //debug only
    List<HintItem> hitems = getHints("cred", 20);
    hitems.forEach((hi) -> {
      LOG.debug("Iri: {}, relevancy: {}", hi.getIri(), hi.getRelevancy());
    });
    //end of debug
  }

  public List<HintItem> getHints(String text, Integer maxHintCount) {
    List<HintItem> result = new LinkedList<>();

    for (Map.Entry<String, TextDbItem> record : db.entrySet()) {
      Double relevancy = record.getValue().computeRelevancy(text, labels);
      if (relevancy > RESULT_THRESHOLD) {
        HintItem hi = new HintItem();
        hi.setIri(record.getKey());
        hi.setRelevancy(relevancy);
        hi.setLabel(getValue(record.getKey(), LABEL_STRING));
        result.add(hi);
      }
    }
    Integer endIndex = result.size() > maxHintCount ? maxHintCount : result.size();
    Collections.sort(result, Comparator.comparing(HintItem::getRelevancy).reversed()
        .thenComparing(HintItem::getLabel).reversed());
    Collections.reverse(result);

    result = result.subList(0, endIndex);

    return result;
  }

  public List<SearchItem> getSearchResult(String text, Integer maxResults) {
    List<SearchItem> result = new LinkedList<>();

    for (Map.Entry<String, TextDbItem> record : db.entrySet()) {
      Double relevancy = record.getValue().computeRelevancy(text);
      if (relevancy > RESULT_THRESHOLD) {
        SearchItem si = new SearchItem();
        si.setIri(record.getKey());
        si.setRelevancy(relevancy);
        String label = getValue(record.getKey(), LABEL_STRING);
        String description = getValue(record.getKey(), DEFINITION_STRING);
        si.setLabel(label);
        si.setDescription(StringUtils.cutString(description, 150, true));
        result.add(si);
      }
    }

    Integer endIndex = result.size() > maxResults ? maxResults : result.size();
    Collections.sort(result, Comparator.comparing(SearchItem::getRelevancy).reversed()
        .thenComparing(SearchItem::getDescription).reversed());
    Collections.reverse(result);
    
    result = result.subList(0, endIndex);

    return result;
  }

  private String getValue(String recordID, String recordProperty) {
    if (!searchFields.contains(recordProperty) && !db.containsKey(recordID)) {
      return null;
    }

    for (TextDbItem.Item val : db.get(recordID).getValue()) {
      if (val.getType().equals(recordProperty)) {
        return val.getValue();
      }
    }
    return null;

  }

}
