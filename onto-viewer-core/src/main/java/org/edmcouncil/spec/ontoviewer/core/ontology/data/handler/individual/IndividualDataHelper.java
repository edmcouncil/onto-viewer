package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.individual;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.Pair;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlListElementIndividualProperty;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class IndividualDataHelper {

  private static final Logger LOG = LoggerFactory.getLogger(IndividualDataHelper.class);
  private static final String instanceKey = ViewerIdentifierFactory
          .createId(ViewerIdentifierFactory.Type.function, OwlType.INSTANCES.name().toLowerCase());

  private final LabelProvider labelExtractor;

  public IndividualDataHelper(LabelProvider labelExtractor) {
    this.labelExtractor = labelExtractor;
  }

  /**
   * Handle all individual for OWLClass given on parameter.
   *
   * @param ontology
   * @param clazz
   * @return
   */
  public OwlDetailsProperties<PropertyValue> handleClassIndividuals(OWLOntology ontology, OWLClass clazz) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();

    Set<OWLIndividual> listOfIndividuals = ontology.importsClosure()
            .flatMap(currentOntology -> getInstancesByClass(currentOntology, clazz).stream())
            .collect(Collectors.toSet());

    for (OWLIndividual individual : listOfIndividuals) {
      OwlListElementIndividualProperty s = new OwlListElementIndividualProperty();
      s.setType(OwlType.INSTANCES);
      String label;
      if (individual.isNamed()) {
        label = labelExtractor.getLabelOrDefaultFragment(individual.asOWLNamedIndividual());
      } else {
        label = labelExtractor.getLabelOrDefaultNodeID(individual.asOWLAnonymousIndividual());
      }
      String iri = individual.isNamed() ? individual.asOWLNamedIndividual().getIRI().toString() : individual.asOWLAnonymousIndividual().toStringID();

      s.setValue(new Pair(label.replaceFirst("^_:",""), iri));
      result.addProperty(instanceKey, s);
    }
    result.sortPropertiesInAlphabeticalOrder();
    return result;
  }

  private Set<OWLIndividual> getInstancesByClass(OWLOntology ontology, OWLClass clazz) {
    Set<OWLIndividual> result = new LinkedHashSet<>();
    ontology.importsClosure().forEach(currentOntology -> {
      currentOntology.classAssertionAxioms(clazz).forEach(axiom -> {
        OWLIndividual individual = axiom.getIndividual();
        if (individual.isNamed() || individual.isAnonymous()) {
          result.add(individual);
        } 
      });
    });


    Set<OWLIndividual> bnodeIndividuals = new LinkedHashSet<>();
    for (var currentOntology : ontology.importsClosure().collect(Collectors.toSet())) {
      currentOntology.referencedAnonymousIndividuals().collect(Collectors.toSet()).forEach((individual) -> {
        currentOntology.classAssertionAxioms(individual).collect(Collectors.toSet())
                .stream()
                .forEach(_item -> bnodeIndividuals.add(individual));
      });
    }
    
    
    int number = 4;
    System.out.println(number);
    
    
    return result;
  }

  /**
   * This method is used to display Particular Individual
   *
   * @param ontology This is a loaded ontology.
   * @param clazz    Clazz are all Instances.
   * @return All instances of a given class;
   */
  public OwlDetailsProperties<PropertyValue> handleInstances(OWLOntology ontology, OWLClass clazz) {
//    OwlDetailsProperties<PropertyValue> propertyValueOwlDetailsProperties = handleClassIndividualsAnonymous(ontology, clazz);
    OwlDetailsProperties<PropertyValue> result = handleClassIndividuals(ontology, clazz);
    return result;
  }
}