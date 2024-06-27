package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.edmcouncil.spec.ontoviewer.core.mapping.OntoViewerEntityType;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyEntity;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.RestrictionType;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

class InferableRestrictionHandlerTest {

  private static final String IRI_PREFIX = "http://trojczak.pl/ontology/sparseDisplayExample/";
  private static final String GROUP1 = "group1";

  private InferableRestrictionHandler inferableRestrictionHandler;

  @BeforeEach
  void setUp() throws OWLOntologyCreationException {
    var ontologyManager = new OntologyManager();
    ontologyManager.updateOntology(prepareOntology());

    this.inferableRestrictionHandler = new InferableRestrictionHandler(ontologyManager);
  }

  @Test
  void shouldMarkPropertyAsInferableForExistentialQuantificationRestriction() {
    Map<String, List<PropertyValue>> properties = new HashMap<>();

    List<PropertyValue> propertyValueList = new ArrayList<>();
    propertyValueList.add(
        new OwlAxiomPropertyValue("/arg2/ some /arg4/", OwlType.AXIOM, "propA some ClassA",
            Map.of(
                "/arg2/", prepareObjectPropertyAxiomPropertyEntity("propA"),
                "/arg4/", prepareClassAxiomPropertyEntity("ClassA")),
            false,
            RestrictionType.EXISTENTIAL_QUANTIFICATION));
    propertyValueList.add(
        new OwlAxiomPropertyValue("/arg2/ some /arg4/", OwlType.AXIOM, "propA_1 some ClassA_1",
            Map.of(
                "/arg2/", prepareObjectPropertyAxiomPropertyEntity("propA_1"),
                "/arg4/", prepareClassAxiomPropertyEntity("ClassA_1")),
            false,
            RestrictionType.EXISTENTIAL_QUANTIFICATION));
    properties.put(GROUP1, propertyValueList);

    inferableRestrictionHandler.markInferableRestrictions(properties);

    OwlAxiomPropertyValue propertyValue = (OwlAxiomPropertyValue) properties.get(GROUP1).get(0);
    assertTrue(propertyValue.isInferable());
  }

  @Test
  void shouldMarkPropertyAsInferableForUniversalQuantificationRestriction() {
    Map<String, List<PropertyValue>> properties = new HashMap<>();

    List<PropertyValue> propertyValueList = new ArrayList<>();
    propertyValueList.add(
        new OwlAxiomPropertyValue("/arg2/ only /arg4/", OwlType.AXIOM, "propB only ClassB_1",
            Map.of(
                "/arg2/", prepareObjectPropertyAxiomPropertyEntity("propB"),
                "/arg4/", prepareClassAxiomPropertyEntity("ClassB_1")),
            false,
            RestrictionType.UNIVERSAL_QUANTIFICATION));
    propertyValueList.add(
        new OwlAxiomPropertyValue("/arg2/ only /arg4/", OwlType.AXIOM, "propB_1 only ClassB",
            Map.of(
                "/arg2/", prepareObjectPropertyAxiomPropertyEntity("propB_1"),
                "/arg4/", prepareClassAxiomPropertyEntity("ClassB")),
            false,
            RestrictionType.UNIVERSAL_QUANTIFICATION));
    properties.put(GROUP1, propertyValueList);

    inferableRestrictionHandler.markInferableRestrictions(properties);

    OwlAxiomPropertyValue propertyValue = (OwlAxiomPropertyValue) properties.get(GROUP1).get(0);
    assertTrue(propertyValue.isInferable());
  }

  @Test
  void shouldMarkPropertyAsInferableForObjectMinimumCardinalityRestriction() {
    Map<String, List<PropertyValue>> properties = new HashMap<>();

    List<PropertyValue> propertyValueList = new ArrayList<>();
    propertyValueList.add(
        new OwlAxiomPropertyValue("/arg2/ min 1 /arg4/", OwlType.AXIOM, "propB min 1 ClassB",
            Map.of(
                "/arg2/", prepareObjectPropertyAxiomPropertyEntity("propB"),
                "/arg4/", prepareClassAxiomPropertyEntity("ClassB")),
            false,
            RestrictionType.OBJECT_MINIMUM_CARDINALITY));
    propertyValueList.add(
        new OwlAxiomPropertyValue("/arg2/ min 3 /arg4/", OwlType.AXIOM, "propB_1 min 3 ClassB_1",
            Map.of(
                "/arg2/", prepareObjectPropertyAxiomPropertyEntity("propB_1"),
                "/arg4/", prepareClassAxiomPropertyEntity("ClassB_1")),
            false,
            RestrictionType.OBJECT_MINIMUM_CARDINALITY));
    properties.put(GROUP1, propertyValueList);

    inferableRestrictionHandler.markInferableRestrictions(properties);

    OwlAxiomPropertyValue propertyValue = (OwlAxiomPropertyValue) properties.get(GROUP1).get(0);
    assertTrue(propertyValue.isInferable());
  }

  @Test
  void shouldMarkPropertyAsInferableForObjectMaximumCardinalityRestriction() {
    Map<String, List<PropertyValue>> properties = new HashMap<>();

    List<PropertyValue> propertyValueList = new ArrayList<>();
    propertyValueList.add(
        new OwlAxiomPropertyValue("/arg2/ max 3 /arg4/", OwlType.AXIOM, "propB max 3 ClassB",
            Map.of(
                "/arg2/", prepareObjectPropertyAxiomPropertyEntity("propB"),
                "/arg4/", prepareClassAxiomPropertyEntity("ClassB")),
            false,
            RestrictionType.OBJECT_MAXIMUM_CARDINALITY));
    propertyValueList.add(
        new OwlAxiomPropertyValue("/arg2/ max 1 /arg4/", OwlType.AXIOM, "propB_1 max 1 ClassB_1",
            Map.of(
                "/arg2/", prepareObjectPropertyAxiomPropertyEntity("propB_1"),
                "/arg4/", prepareClassAxiomPropertyEntity("ClassB_1")),
            false,
            RestrictionType.OBJECT_MAXIMUM_CARDINALITY));
    properties.put(GROUP1, propertyValueList);

    inferableRestrictionHandler.markInferableRestrictions(properties);

    OwlAxiomPropertyValue propertyValue = (OwlAxiomPropertyValue) properties.get(GROUP1).get(0);
    assertTrue(propertyValue.isInferable());
  }

  private OWLOntology prepareOntology() throws OWLOntologyCreationException {
    var resourceAsStream = getClass().getResourceAsStream("/ontology/sparseDisplayExample1.owl");
    var owlOntologyManager = OWLManager.createOWLOntologyManager();
    return owlOntologyManager.loadOntologyFromOntologyDocument(resourceAsStream);
  }

  private OwlAxiomPropertyEntity prepareClassAxiomPropertyEntity(String classIriFragment) {
    return new OwlAxiomPropertyEntity(IRI_PREFIX + classIriFragment,
        classIriFragment,
        OntoViewerEntityType.CLASS,
        false);
  }

  private OwlAxiomPropertyEntity prepareObjectPropertyAxiomPropertyEntity(String objectPropertyIriFragment) {
    return new OwlAxiomPropertyEntity(IRI_PREFIX + objectPropertyIriFragment,
        objectPropertyIriFragment,
        OntoViewerEntityType.OBJECT_PROPERTY,
        false);
  }
}