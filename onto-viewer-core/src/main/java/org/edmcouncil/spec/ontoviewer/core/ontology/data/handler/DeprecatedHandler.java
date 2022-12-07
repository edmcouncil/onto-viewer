package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import static org.semanticweb.owlapi.model.parameters.Imports.INCLUDED;

import java.util.ArrayList;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.edmcouncil.spec.ontoviewer.core.cache.CacheConfig;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.utils.OntologyUtils;
import org.semanticweb.owlapi.model.*;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Created by Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class DeprecatedHandler {

  private final ArrayList<String> deprecatedList ;
  private final CacheManager cacheManager;
  private final OntologyUtils ontologyUtils;
  private final OntologyManager ontologyManager;

  public DeprecatedHandler(CacheManager cacheManager,
      OntologyUtils ontologyUtils,
      OntologyManager ontologyManager) {
    this.cacheManager = cacheManager;
    this.ontologyUtils = ontologyUtils;
    this.ontologyManager = ontologyManager;
    this.deprecatedList = new ArrayList<>();
    this.deprecatedList.add(OWL.DEPRECATED.toString());
    this.deprecatedList.add(OWL.DEPRECATEDCLASS.toString());
    this.deprecatedList.add(OWL.DEPRECATEDPROPERTY.toString());
  }

  private Cache getCache() {
    return cacheManager.getCache(CacheConfig.CacheNames.DEPRECATED.label);
  }

  public Boolean getDeprecatedFromOntology(IRI ontologyIri) {
    Cache cache = getCache();
    Boolean val = cache.get(ontologyIri, Boolean.class);
    if (val != null) {
      return val;
    }
    var ontologyOptional = ontologyUtils.getOntologyByIRI(ontologyIri);
    if (ontologyOptional.isPresent()) {
      var ontology = ontologyOptional.get();
      for (OWLAnnotation annotation : ontology.annotations()
          .collect(Collectors.toList())) {
        var propertyIri = annotation.getProperty().getIRI().getIRIString();
        if(deprecatedList.contains(propertyIri)) {
          return extractDeprecatedFromAnnotation(ontologyIri, cache, annotation);
        }
      }
    }

    return false;
  }

  public Boolean getDeprecatedForEntity(IRI entityIri) {
    //System.out.println("yyyyy");
    Cache cache = getCache();
    Boolean val = cache.get(entityIri.getIRIString(), Boolean.class);
    if (val != null) {
      return val;
    }
    OWLOntology ontology = ontologyManager.getOntology();

    var annotationAssertions = ontology
        .annotationAssertionAxioms(entityIri, INCLUDED)
        .collect(Collectors.toSet());

    for (OWLAnnotationAssertionAxiom annotationAssertion : annotationAssertions) {
      IRI propertyIri = annotationAssertion.getProperty().getIRI();

      if(deprecatedList.contains(propertyIri.getIRIString())){
        return extractDeprecatedFromAnnotation(entityIri, cache, annotationAssertion.getAnnotation());
      }
    }
    return false;
  }

  private Boolean extractDeprecatedFromAnnotation(IRI iri, Cache cache, OWLAnnotation annotation) {
    String deprecatedValue = annotation.getValue().toString();
    deprecatedValue = deprecatedValue.replace("\"^^xsd:boolean", "").replace("\"", "");
    var isDeprecated = Boolean.valueOf(deprecatedValue);
    cache.put(iri.getIRIString(), isDeprecated);
    return isDeprecated;
  }
}
