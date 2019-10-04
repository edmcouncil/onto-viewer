package org.edmcouncil.spec.fibo.weasel.ontology.data;

import java.util.Iterator;
import org.edmcouncil.spec.fibo.weasel.model.PropertyValue;
import org.edmcouncil.spec.fibo.weasel.model.WeaselOwlType;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphNode;
import org.edmcouncil.spec.fibo.weasel.model.graph.ViewerGraph;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.WeaselOntologyVisitors;
import org.edmcouncil.spec.fibo.weasel.utils.OwlUtils;
import org.edmcouncil.spec.fibo.weasel.utils.StringUtils;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class RestrictionGraphDataHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(RestrictionGraphDataHandler.class);
  
  @Autowired
  private OwlDataExtractor extractor;

  public ViewerGraph handleGraph(
      OWLNamedIndividual obj,
      OWLOntology ontology) {

    Iterator<OWLIndividualAxiom> axiomsIterator = ontology.axioms(obj).iterator();
    return handleGraph(axiomsIterator, obj.getIRI());
  }

  public ViewerGraph handleGraph(
      OWLObjectProperty obj,
      OWLOntology ontology) {

    Iterator<OWLObjectPropertyAxiom> axiomsIterator = ontology.axioms(obj).iterator();
    return handleGraph(axiomsIterator, obj.getIRI());
  }

  public ViewerGraph handleGraph(
      OWLDataProperty obj,
      OWLOntology ontology) {

    Iterator<OWLDataPropertyAxiom> axiomsIterator = ontology.axioms(obj).iterator();
    return handleGraph(axiomsIterator, obj.getIRI());
  }

  public ViewerGraph handleGraph(
      OWLClass obj,
      OWLOntology ontology) {

    Iterator<OWLClassAxiom> axiomsIterator = ontology.axioms(obj).iterator();
    return handleGraph(axiomsIterator, obj.getIRI());
  }

  private <T extends OWLAxiom> ViewerGraph handleGraph(
      Iterator<T> axiomsIterator, IRI elementIri) {
    ViewerGraph vg = new ViewerGraph();

    GraphNode root = new GraphNode();
    root.setId(0);
    root.setIri(elementIri.toString());
    root.setLabel(StringUtils.getFragment(elementIri));
    vg.addNode(root);

    while (axiomsIterator.hasNext()) {
      T axiom = axiomsIterator.next();

      Boolean isRestriction = OwlUtils.isRestriction(axiom);

      if (isRestriction && axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF)) {
        OWLSubClassOfAxiom axiomEl = axiom.accept(WeaselOntologyVisitors.getAxiomElement(elementIri));
        
        
        
        
        //extractor.extrackAxiomPropertyIri(axiomEl);
      }
    }
    System.out.println(vg.toString());
    return vg;
  }
}
