package org.edmcouncil.spec.ontoviewer.core.ontology.data;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNode;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNodeType;
import org.edmcouncil.spec.ontoviewer.core.model.graph.OntologyGraph;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.ExpressionReturnedClass;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.OntologyVisitors;
import org.edmcouncil.spec.ontoviewer.core.utils.OwlUtils;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
@Component
public class RestrictionGraphDataHandler {

  private static final Logger LOG = LoggerFactory.getLogger(RestrictionGraphDataHandler.class);

  @Autowired
  private LabelProvider labelExtractor;
  @Autowired
  private OwlUtils owlUtils;
  @Autowired
  private OntologyVisitors ontologyVisitors;

  public OntologyGraph handleGraph(
      OWLNamedIndividual obj,
      OWLOntology ontology) {
    Iterator<OWLIndividualAxiom> axiomsIterator = ontology.axioms(obj, Imports.INCLUDED).iterator();

    IRI elementIri = obj.getIRI();
    GraphNode root = null;
    OntologyGraph vg = null;
    GraphNodeType type = GraphNodeType.INTERNAL;

    if (vg == null) {
      vg = new OntologyGraph();
    }

    if (root == null) {
      root = new GraphNode(vg.nextId());
      root.setIri(elementIri.toString());
      root.setType(GraphNodeType.MAIN);
      String label = labelExtractor.getLabelOrDefaultFragment(elementIri);
      root.setLabel(label);
      vg.addNode(root);
    }

    vg.setRoot(root);

    while (axiomsIterator.hasNext()) {
      OWLIndividualAxiom axiom = axiomsIterator.next();
      axiom.accept(ontologyVisitors.individualCompleteGraphNode(vg, root, type));
      LOG.debug(axiom.getAxiomType().getName());
      for (OWLEntity oWLEntity : axiom.signature().collect(Collectors.toList())) {
        LOG.debug(oWLEntity.toString());
      }

    }

    return vg;
  }

  public OntologyGraph handleGraph(
      OWLObjectProperty obj,
      OWLOntology ontology) {

    Iterator<OWLObjectPropertyAxiom> axiomsIterator = ontology.axioms(obj).iterator();
    return handleGraph(axiomsIterator, obj.getIRI());
  }

  public OntologyGraph handleGraph(
      OWLDataProperty obj,
      OWLOntology ontology) {

    Iterator<OWLDataPropertyAxiom> axiomsIterator = ontology.axioms(obj).iterator();
    return handleGraph(axiomsIterator, obj.getIRI());
  }

  public OntologyGraph handleGraph(OWLClass obj, OWLOntology ontology) {
    Iterator<OWLClassAxiom> axiomsIterator = ontology.axioms(obj, Imports.INCLUDED).iterator();

    OntologyGraph vg = handleGraph(axiomsIterator, obj.getIRI());
    vg = handleInheritedAxiomsGraph(obj, vg, ontology);
    vg = handleEquivalentClassesAxiomGraph(obj, vg, ontology);

    return vg;
  }

  private <T extends OWLAxiom> OntologyGraph handleGraph(
      Iterator<T> axiomsIterator,
      IRI elementIri) {
    return handleGraph(axiomsIterator, elementIri, null, null, GraphNodeType.INTERNAL);
  }

  private <T extends OWLAxiom> OntologyGraph handleGraph(
      Iterator<T> axiomsIterator,
      IRI elementIri,
      GraphNode root,
      OntologyGraph vg,
      GraphNodeType type) {

    if (vg == null) {
      vg = new OntologyGraph();
    }

    if (root == null) {
      root = new GraphNode(vg.nextId());
      root.setIri(elementIri.toString());
      root.setType(GraphNodeType.MAIN);
      String label = labelExtractor.getLabelOrDefaultFragment(elementIri);
      root.setLabel(label);
      vg.addNode(root);
    }

    vg.setRoot(root);

    while (axiomsIterator.hasNext()) {
      T axiom = axiomsIterator.next();

      Boolean isRestriction = owlUtils.isRestriction(axiom);

      if (isRestriction && axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF)) {
        OWLSubClassOfAxiom axiomEl = axiom.accept(ontologyVisitors.getAxiomElement(elementIri));

        Map<GraphNode, Set<ExpressionReturnedClass>> qrestrictions = axiomEl.getSuperClass()
            .accept(ontologyVisitors.superClassAxiom(vg, root, type, Boolean.FALSE));

        if (qrestrictions != null && !qrestrictions.isEmpty()) {
          for (Map.Entry<GraphNode, Set<ExpressionReturnedClass>> entry : qrestrictions.entrySet()) {
            for (ExpressionReturnedClass classExpression : entry.getValue()) {
              handleRecursivelyRestrictions(classExpression.getOwlClassExpression(), vg, entry.getKey(), type, false, classExpression.getNot());
            }
          }
        }
      }
    }
    vg.setRoot(root);
    return vg;
  }

  public void handleRecursivelyRestrictions(
      OWLClassExpression expression,
      OntologyGraph vg,
      GraphNode root,
      GraphNodeType type,
      Boolean eqivalentTo,
      Boolean not
  ) {

    LOG.debug("[Expression] Process expression: {}", expression.toString());

    if (expression == null) {
      return;
    }

    Map<GraphNode, Set<ExpressionReturnedClass>> expressionsMap = expression
        .accept(ontologyVisitors.superClassAxiom(vg, root, type, eqivalentTo, not));

    if (expressionsMap != null && !expressionsMap.isEmpty()) {
      expressionsMap.entrySet().forEach((entry) -> {
        for (ExpressionReturnedClass classExpression : entry.getValue()) {
          handleRecursivelyRestrictions(classExpression.getOwlClassExpression(), vg, entry.getKey(), type, eqivalentTo, classExpression.getNot());
        }

      });
    }
  }

  private OntologyGraph handleInheritedAxiomsGraph(OWLClass clazz, OntologyGraph vg,
      OWLOntology ontology) {
    Set<OWLClassExpression> alreadySeen = new HashSet<>();
    owlUtils.getSuperClasses(clazz, ontology, alreadySeen).forEach((owlClass) -> {
      Iterator<OWLClassAxiom> axiomsIterator =
          ontology.axioms(owlClass, Imports.INCLUDED).iterator();
      handleGraph(axiomsIterator, owlClass.getIRI(), vg.getRoot(), vg, GraphNodeType.EXTERNAL);
    });
    return vg;
  }

  public Set<OWLEquivalentClassesAxiom> getClassesAxioms(OWLClass clazz, OWLOntology ontology) {
    Set<OWLEquivalentClassesAxiom> result = new HashSet<>();
    ontology.importsClosure().forEach(currentOntology -> {
      result.addAll(ontology.equivalentClassesAxioms(clazz).collect(Collectors.toSet()));
    });
    return result;
  }

  public OntologyGraph handleEquivalentClassesAxiomGraph(OWLClass clazz, OntologyGraph vg,
      OWLOntology ontology) {
    Set<OWLEquivalentClassesAxiom> result = getClassesAxioms(clazz, ontology);

    GraphNode root = vg.getRoot();
    for (OWLEquivalentClassesAxiom owlEquivalentClassesAxiom : result) {
      LOG.debug("Equivalent Classes Axiom " + owlEquivalentClassesAxiom.toString());

      OWLEquivalentClassesAxiom eca = owlEquivalentClassesAxiom.getAxiomWithoutAnnotations();
      LOG.debug("EquivalentClassesAxiom: {}", eca.toString());
      Map<GraphNode, Set<ExpressionReturnedClass>> qrestrictions = eca
          .accept(ontologyVisitors.superClassAxiom(vg, vg.getRoot(), GraphNodeType.INTERNAL, true, false));
      //   Boolean isFirstEquivalentMarked = false;

      if (qrestrictions != null && !qrestrictions.isEmpty()) {
        for (Map.Entry<GraphNode, Set<ExpressionReturnedClass>> entry : qrestrictions.entrySet()) {
          for (ExpressionReturnedClass classExpression : entry.getValue()) {
            LOG.debug("Class expression {} ", classExpression.toString());
            LOG.debug("handleEquivalentClassesAxiomGraph -> entryGetKey {}", entry.getKey());

            handleRecursivelyRestrictions(classExpression.getOwlClassExpression(), vg, entry.getKey(), GraphNodeType.INTERNAL, classExpression.getEquivalent(), false);

//            if (isFirstEquivalentMarked == false) {
//              isFirstEquivalentMarked = true;
//
//            }
          }
        }
      }
    }
    vg.setRoot(root);
    return vg;
  }

}
