package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.Pair;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlListElementIndividualProperty;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class IndividualDataHandler {

  private static final Logger LOG = LoggerFactory.getLogger(IndividualDataHandler.class);
  private static final String instanceKey = ViewerIdentifierFactory
      .createId(ViewerIdentifierFactory.Type.function, OwlType.INSTANCES.name().toLowerCase());

  private final LabelProvider labelExtractor;

  public IndividualDataHandler(LabelProvider labelExtractor) {
    this.labelExtractor = labelExtractor;
  }

  /**
   * Handle all individual for OWLClass given on parameter.
   *
   * @param ontology
   * @param clazz
   * @return
   */
  public OwlDetailsProperties<PropertyValue> handleClassIndividuals(OWLOntology ontology,
      OWLClass clazz) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();

    Set<OWLNamedIndividual> listOfIndividuals = ontology.importsClosure()
        .flatMap(currentOntology -> getInstancesByClass(currentOntology, clazz).stream())
        .collect(Collectors.toSet());

    for (OWLNamedIndividual namedIndividual : listOfIndividuals) {
        OwlListElementIndividualProperty s = new OwlListElementIndividualProperty();
        s.setType(OwlType.INSTANCES);
        String label = labelExtractor.getLabelOrDefaultFragment(namedIndividual);
        s.setValue(new Pair(label, namedIndividual.getIRI().toString()));
        result.addProperty(instanceKey, s);
        namedIndividual.getEntityType();
    }

    return result;
  }

  private Set<OWLNamedIndividual> getInstancesByClass(OWLOntology ontology, OWLClass clazz) {
    Set<OWLNamedIndividual> result = new HashSet<>();

    for (var currentOntology : ontology.importsClosure().collect(Collectors.toSet())) {
      currentOntology.individualsInSignature().collect(Collectors.toSet()).forEach((individual) -> {
        currentOntology.classAssertionAxioms(individual).collect(Collectors.toSet())
            .stream()
            .filter(classAssertion -> (classAssertion.containsEntityInSignature(clazz)))
            .forEach(_item -> result.add(individual));
      });
    }

    return result;
  }
}
