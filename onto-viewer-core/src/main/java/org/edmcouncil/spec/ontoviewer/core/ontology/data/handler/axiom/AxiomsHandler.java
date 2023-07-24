package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom;

import static org.edmcouncil.spec.ontoviewer.core.model.OwlType.TAXONOMY;
import static org.semanticweb.owlapi.model.parameters.Imports.INCLUDED;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationPropertyValueWithSubAnnotations;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.extractor.OwlDataExtractor;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.StringIdentifier;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.data.AnnotationsDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.visitor.ContainsVisitors;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.edmcouncil.spec.ontoviewer.core.utils.StringUtils;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AxiomsHandler {
private static  final Logger LOG = LoggerFactory.getLogger(AxiomsHandler.class);
  private final AxiomsHelper axiomsHelper;
  private final ContainsVisitors containsVisitors;
  private OwlDataExtractor dataExtractor;
  private AnnotationsDataHandler annotationsDataHandler;

  public AxiomsHandler(AxiomsHelper axiomsHelper, ContainsVisitors containsVisitors) {
    this.axiomsHelper = axiomsHelper;
    this.containsVisitors = containsVisitors;
  }

  public OwlDetailsProperties<PropertyValue> handle(OWLNamedIndividual obj, OWLOntology ontology) {
    Iterator<OWLIndividualAxiom> axiomsIterator = ontology.axioms(obj, INCLUDED).iterator();
    return handle(axiomsIterator, obj.getIRI());
  }

  public OwlDetailsProperties<PropertyValue> handle(OWLObjectProperty obj, OWLOntology ontology) {
    Iterator<OWLObjectPropertyAxiom> axiomsIterator = ontology.axioms(obj, INCLUDED).iterator();
    return handle(axiomsIterator, obj.getIRI());
  }

  public OwlDetailsProperties<PropertyValue> handle(OWLDataProperty obj, OWLOntology ontology) {
    Iterator<OWLDataPropertyAxiom> axiomsIterator = ontology.axioms(obj, INCLUDED).iterator();
    return handle(axiomsIterator, obj.getIRI());
  }

  public OwlDetailsProperties<PropertyValue> handle(OWLClass obj, OWLOntology ontology) {
    return handle(obj, ontology, false);
  }

  public OwlDetailsProperties<PropertyValue> handle(OWLClass obj, OWLOntology ontology, boolean extractGCI) {
    Set<OWLClassAxiom> axiomsSet = ontology.axioms(obj, INCLUDED).collect(Collectors.toSet());

    if (extractGCI) {
      ontology.importsClosure().forEach(currentOntology ->
          axiomsSet.addAll(
              currentOntology.axioms(AxiomType.SUBCLASS_OF)
                  .filter(OWLSubClassOfAxiom::isGCI)
                  .filter(el -> el.accept(containsVisitors.visitor(obj.getIRI())))
                  .collect(Collectors.toSet())));
    }

    return handle(axiomsSet.iterator(), obj.getIRI());
  }

  public OwlDetailsProperties<PropertyValue> handle(OWLAnnotationProperty obj, OWLOntology ontology) {
    Iterator<OWLAnnotationAxiom> axiomsIterator = ontology.axioms(obj, INCLUDED).iterator();
    return handle(axiomsIterator, obj.getIRI());
  }

  private <T extends OWLAxiom> OwlDetailsProperties<PropertyValue> handle(
      Iterator<T> axiomsIterator,
      IRI elementIri) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();

    String iriFragment = elementIri.getFragment();
    String splitFragment = StringUtils.getIdentifier(elementIri);
    boolean fixRenderedIri = !iriFragment.equals(splitFragment);


    while (axiomsIterator.hasNext()) {
      T axiom = axiomsIterator.next();

      String key = axiom.getAxiomType().getName();
      if (axiom instanceof OWLSubClassOfAxiom) {
        OWLSubClassOfAxiom clazzAxiom = (OWLSubClassOfAxiom) axiom;
        if (clazzAxiom.isGCI()) {
          key = "gci";
        }
      }

      var annotationsAxiom = axiom.annotations().collect(Collectors.toList());
      if (annotationsAxiom.size() > 0) {

//        for (OWLAnnotationAssertionAxiom annotationAssertion : annotationAssertions) {
//        String value = annotationAssertion.annotationValue().toString();
//        PropertyValue annotationPropertyValue = new OwlAnnotationPropertyValueWithSubAnnotations();
//        annotationPropertyValue.setType(dataExtractor.extractAnnotationType(annotationAssertion));
//        annotationPropertyValue = annotationsDataHandler.getAnnotationPropertyValue(annotationAssertion, value,
//            annotationPropertyValue);
//      }
      LOG.info("anotated: {}", axiom.annotations().collect(Collectors.toList()));

      key = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.axiom, key);

      OwlAxiomPropertyValue axiomPropertyValue = axiomsHelper.prepareAxiomPropertyValue(
          axiom, iriFragment, splitFragment, fixRenderedIri, true);

      if (axiomPropertyValue == null) {
        continue;
      }
      if (!key.equals(StringIdentifier.subClassOfIriString) || !axiomPropertyValue.getType().equals(TAXONOMY)) {
        result.addProperty(key, axiomPropertyValue);
      }
    }
    result.sortPropertiesInAlphabeticalOrder();
  }
    return result;
}
}
