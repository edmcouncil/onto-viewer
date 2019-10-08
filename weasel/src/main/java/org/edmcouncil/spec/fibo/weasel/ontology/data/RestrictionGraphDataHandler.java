package org.edmcouncil.spec.fibo.weasel.ontology.data;

import java.util.Iterator;
import java.util.stream.Collectors;
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
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLQuantifiedObjectRestriction;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InferenceDepth;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
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

    ViewerGraph vg = handleGraph(axiomsIterator, obj.getIRI());
    vg = handleInheritedAxiomsGraph(obj, vg, ontology);
    return vg;
  }

  private <T extends OWLAxiom> ViewerGraph handleGraph(
      Iterator<T> axiomsIterator,
      IRI elementIri) {
    return handleGraph(axiomsIterator, elementIri, null, null);
  }

  private <T extends OWLAxiom> ViewerGraph handleGraph(
      Iterator<T> axiomsIterator,
      IRI elementIri,
      GraphNode root,
      ViewerGraph vg) {

    if (vg == null) {
      vg = new ViewerGraph();
    }

    if (root == null) {
      root = new GraphNode(vg.nextId());
      root.setIri(elementIri.toString());
      String label = StringUtils.getFragment(elementIri);
      root.setLabel(label.substring(0, 1).toLowerCase() + label.substring(1) + "Instance");
      vg.addNode(root);
    }

    vg.setRoot(root);

    while (axiomsIterator.hasNext()) {
      T axiom = axiomsIterator.next();

      Boolean isRestriction = OwlUtils.isRestriction(axiom);

      if (isRestriction && axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF)) {
        OWLSubClassOfAxiom axiomEl = axiom.accept(WeaselOntologyVisitors.getAxiomElement(elementIri));

        OWLClassExpression qrestriction = axiomEl.getSuperClass()
            .accept(WeaselOntologyVisitors.superClassAxiom(vg, root));

        while (qrestriction != null) {
          //axiomEl = qrestriction.accept(WeaselOntologyVisitors.getAxiomElement(elementIri));
          qrestriction = qrestriction
              .accept(WeaselOntologyVisitors.superClassAxiom(vg, vg.getRoot()));
        }

        //extractor.extrackAxiomPropertyIri(axiomEl);
      }
    }
    vg.setRoot(root);

    //System.out.println(vg.toJsVars());
    return vg;
  }

  private ViewerGraph handleInheritedAxiomsGraph(OWLClass clazz, ViewerGraph vg, OWLOntology ontology) {

    OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
    OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);

    NodeSet<OWLClass> rset = reasoner.getSuperClasses(clazz, InferenceDepth.ALL);

    for (Node<OWLClass> node : rset) {
      for (OWLClass owlClass : node.entities().collect(Collectors.toSet())) {
        Iterator<OWLClassAxiom> axiomsIterator = ontology.axioms(owlClass).iterator();
        handleGraph(axiomsIterator, owlClass.getIRI(), vg.getRoot(), vg);
      }
    }
    return vg;
  }

}
