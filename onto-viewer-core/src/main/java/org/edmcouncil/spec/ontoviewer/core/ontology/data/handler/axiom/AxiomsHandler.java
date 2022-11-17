package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom;

import static org.edmcouncil.spec.ontoviewer.core.model.OwlType.TAXONOMY;
import static org.semanticweb.owlapi.model.parameters.Imports.INCLUDED;

import java.util.Iterator;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.StringIdentifier;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.edmcouncil.spec.ontoviewer.core.utils.StringUtils;
import org.semanticweb.owlapi.model.IRI;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AxiomsHandler {

  private static final Logger LOG = LoggerFactory.getLogger(AxiomsHandler.class);
  private final AxiomsHelper axiomsHelper;

  public AxiomsHandler(AxiomsHelper axiomsHelper) {
    this.axiomsHelper = axiomsHelper;
  }

  public OwlDetailsProperties<PropertyValue> handle(
      OWLNamedIndividual obj,
      OWLOntology ontology) {

    Iterator<OWLIndividualAxiom> axiomsIterator = ontology.axioms(obj, INCLUDED).iterator();
    return handle(axiomsIterator, obj.getIRI());
  }

  public OwlDetailsProperties<PropertyValue> handle(
      OWLObjectProperty obj,
      OWLOntology ontology) {

    Iterator<OWLObjectPropertyAxiom> axiomsIterator = ontology.axioms(obj, INCLUDED).iterator();
    return handle(axiomsIterator, obj.getIRI());
  }

  public OwlDetailsProperties<PropertyValue> handle(
      OWLDataProperty obj,
      OWLOntology ontology) {
    Iterator<OWLDataPropertyAxiom> axiomsIterator = ontology.axioms(obj, INCLUDED).iterator();
    return handle(axiomsIterator, obj.getIRI());
  }

  public OwlDetailsProperties<PropertyValue> handle(OWLClass obj, OWLOntology ontology) {
    Iterator<OWLClassAxiom> axiomsIterator = ontology.axioms(obj, INCLUDED).iterator();
    return handle(axiomsIterator, obj.getIRI());
  }

  public OwlDetailsProperties<PropertyValue> handle(
      OWLAnnotationProperty obj,
      OWLOntology ontology) {
    Iterator<OWLAnnotationAxiom> axiomsIterator = ontology.axioms(obj, INCLUDED).iterator();
    return handle(axiomsIterator, obj.getIRI());
  }

  private <T extends OWLAxiom> OwlDetailsProperties<PropertyValue> handle(
      Iterator<T> axiomsIterator,
      IRI elementIri) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();
    String iriFragment = elementIri.getFragment();
    String splitFragment = StringUtils.getIdentifier(elementIri);
    Boolean fixRenderedIri = !iriFragment.equals(splitFragment);

    int start = 0;

    while (axiomsIterator.hasNext()) {
      T axiom = axiomsIterator.next();

      String key = axiom.getAxiomType().getName();
      key = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.axiom, key);

      OwlAxiomPropertyValue opv = axiomsHelper.prepareAxiomPropertyValue(axiom, iriFragment,
          splitFragment,
          fixRenderedIri, key, start, true);

      if (opv == null) {
        continue;
      }
      start = opv.getLastId();
      if (!key.equals(StringIdentifier.subClassOfIriString) || !opv.getType().equals(TAXONOMY)) {
        result.addProperty(key, opv);
      }
    }
    result.sortPropertiesInAlphabeticalOrder();
    return result;
  }
}
