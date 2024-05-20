package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.edmcouncil.spec.ontoviewer.core.mapping.OntoViewerEntityType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyEntity;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlLabeledMultiAxiom;
import org.edmcouncil.spec.ontoviewer.core.model.property.RestrictionType;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.springframework.stereotype.Service;

/**
 * This service is intended to detect restrictions that are inferrable. An inferrable restriction is a restriction that
 * can be inferred from other restriction. Here is a description how it works: Suppose that the following statements are
 * true:
 * <ul>
 *   <li> r1 is a restriction: R someValuesFrom X</li>
 *   <li> r2 is a restriction: S someValuesFrom Y</li>
 *   <li> A is subClassOf r1</li>
 *   <li> A is subClassOf r2</li>
 *   <li> R is subPropertyOf+ S</li>
 *   <li> X is subClassOf+ Y</li>
 * </ul>
 * then r2 can be marked as inferrable.
 */
@Service
public class InferableRestrictionHandler {

  private static final String CARDINALITY_PATTERN_STRING = "/arg\\d+/ (?:min|max) (?<n>\\d+) /arg\\d+/";
  private static final Pattern CARDINALITY_PATTERN = Pattern.compile(CARDINALITY_PATTERN_STRING);

  private final OntologyManager ontologyManager;
  private final OWLDataFactory owlDataFactory;

  public InferableRestrictionHandler(OntologyManager ontologyManager) {
    this.ontologyManager = ontologyManager;
    this.owlDataFactory = OWLManager.getOWLDataFactory();
  }

  public void markInferableRestrictions(Map<String, List<PropertyValue>> properties) {
    List<OwlAxiomPropertyValue> propertiesWithRestrictions = filterPropertiesWithRestrictions(properties);

    for (OwlAxiomPropertyValue axiom1 : propertiesWithRestrictions) {
      RestrictionType restrictionType1 = axiom1.getRestrictionType();
      if (restrictionType1 == RestrictionType.OTHER) {
        continue;
      }

      for (OwlAxiomPropertyValue axiom2 : propertiesWithRestrictions) {
        if (axiom1.equals(axiom2) || !restrictionType1.equals(axiom2.getRestrictionType())) {
          continue;
        }

        Optional<OwlAxiomPropertyEntity> objectProperty1 = getObjectProperty(axiom1);
        Optional<OwlAxiomPropertyEntity> objectProperty2 = getObjectProperty(axiom2);
        objectProperty1.ifPresent(op1 ->
            objectProperty2.ifPresent(op2 -> {
              if (checkIfIsSuperProperty(op1, op2)) {
                Optional<OwlAxiomPropertyEntity> class1 = getClass(axiom1);
                Optional<OwlAxiomPropertyEntity> class2 = getClass(axiom2);
                class1.ifPresent(c1 -> class2.ifPresent(c2 -> handleAxioms(axiom1, axiom2, c1, c2)));
              }
            }));
      }
    }
  }

  private void handleAxioms(OwlAxiomPropertyValue axiom1,
      OwlAxiomPropertyValue axiom2,
      OwlAxiomPropertyEntity c1,
      OwlAxiomPropertyEntity c2) {
    switch (axiom1.getRestrictionType()) {
      case EXISTENTIAL_QUANTIFICATION:
        if (checkIfIsSuperClass(c1, c2)) {
          axiom1.setInferable(true);
        }
        break;
      case UNIVERSAL_QUANTIFICATION:
        if (checkIfIsSuperClass(c2, c1)) {
          axiom1.setInferable(true);
        }
        break;
      case OBJECT_MINIMUM_CARDINALITY:
        if (checkIfIsSuperClass(c1, c2)) {
          int cardinalityOfAxiom1 = getCardinality(axiom1.getValue());
          int cardinalityOfAxiom2 = getCardinality(axiom2.getValue());
          if (cardinalityOfAxiom1 != -1 &&
              cardinalityOfAxiom2 != -1 &&
              cardinalityOfAxiom1 <= cardinalityOfAxiom2) {
            axiom1.setInferable(true);
          }
        }
        break;
      case OBJECT_MAXIMUM_CARDINALITY:
        if (checkIfIsSuperClass(c1, c2)) {
          int cardinalityOfAxiom1 = getCardinality(axiom1.getValue());
          int cardinalityOfAxiom2 = getCardinality(axiom2.getValue());
          if (cardinalityOfAxiom1 != -1 &&
              cardinalityOfAxiom2 != -1 &&
              cardinalityOfAxiom2 <= cardinalityOfAxiom1) {
            axiom1.setInferable(true);
          }
        }
        break;
      default:
        // skip
    }
  }

  private Optional<OwlAxiomPropertyEntity> getClass(OwlAxiomPropertyValue axiomPropertyValue) {
    return getObjectProperty(axiomPropertyValue, OntoViewerEntityType.CLASS);
  }

  private Optional<OwlAxiomPropertyEntity> getObjectProperty(OwlAxiomPropertyValue axiomPropertyValue) {
    return getObjectProperty(axiomPropertyValue, OntoViewerEntityType.OBJECT_PROPERTY);
  }

  private boolean checkIfIsSuperProperty(OwlAxiomPropertyEntity op1, OwlAxiomPropertyEntity op2) {
    if (op1.getIri().equals(op2.getIri())) {
      return true;
    }

    OWLObjectProperty owlObjectProperty1 = owlDataFactory.getOWLObjectProperty(IRI.create(op1.getIri()));
    OWLObjectProperty owlObjectProperty2 = owlDataFactory.getOWLObjectProperty(IRI.create(op2.getIri()));
    return EntitySearcher
        .getSuperProperties(owlObjectProperty2, ontologyManager.getOntologyWithImports())
        .anyMatch(owlObjectProperty -> owlObjectProperty.equals(owlObjectProperty1));
  }

  private boolean checkIfIsSuperClass(OwlAxiomPropertyEntity axiomProperty1, OwlAxiomPropertyEntity axiomProperty2) {
    if (axiomProperty1.getIri().equals(axiomProperty2.getIri())) {
      return true;
    }

    OWLClass class1 = owlDataFactory.getOWLClass(IRI.create(axiomProperty1.getIri()));
    OWLClass class2 = owlDataFactory.getOWLClass(IRI.create(axiomProperty2.getIri()));
    return EntitySearcher
        .getSuperClasses(class2, ontologyManager.getOntologyWithImports())
        .anyMatch(clazz -> clazz.equals(class1));
  }

  private Optional<OwlAxiomPropertyEntity> getObjectProperty(OwlAxiomPropertyValue axiomPropertyValue,
      OntoViewerEntityType entityType) {
    return axiomPropertyValue.getEntityMaping()
        .values()
        .stream()
        .filter(entity -> entity.getEntityType().equals(entityType))
        .findFirst();
  }

  private int getCardinality(String axiomValue) {
    Matcher matcher = CARDINALITY_PATTERN.matcher(axiomValue);
    if (matcher.matches()) {
      String cardinalityString = matcher.group("n");
      if (cardinalityString != null) {
        try {
          return Integer.parseInt(cardinalityString);
        } catch (NumberFormatException ex) {
          return -1;
        }
      }
    }
    return -1;
  }

  private List<OwlAxiomPropertyValue> filterPropertiesWithRestrictions(Map<String, List<PropertyValue>> properties) {
    Set<PropertyValue> mergedProperties = mergeProperties(properties);
    return mergedProperties.stream()
        .flatMap(propertyValue -> {
          if (propertyValue instanceof OwlLabeledMultiAxiom) {
            OwlLabeledMultiAxiom owlLabeledMultiAxiom = (OwlLabeledMultiAxiom) propertyValue;
            return owlLabeledMultiAxiom.getValue().stream();
          } else {
            return Stream.of(propertyValue);
          }
        })
        .filter(OwlAxiomPropertyValue.class::isInstance)
        .map(OwlAxiomPropertyValue.class::cast)
        .filter(axiomPropertyValue -> axiomPropertyValue.getRestrictionType() != RestrictionType.OTHER)
        .collect(Collectors.toList());
  }

  private Set<PropertyValue> mergeProperties(Map<String, List<PropertyValue>> properties) {
    Set<PropertyValue> mergedProperties = new HashSet<>();
    for (List<PropertyValue> values : properties.values()) {
      mergedProperties.addAll(values);
    }
    return mergedProperties;
  }
}
