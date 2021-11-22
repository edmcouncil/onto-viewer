package org.edmcouncil.spec.ontoviewer.core.ontology.visitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNode;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNodeType;
import org.edmcouncil.spec.ontoviewer.core.model.graph.OntologyGraph;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph.VisitClassAssertionAxiom;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph.VisitDataExactCardinality;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph.VisitDataHasValue;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph.VisitDataMaxCardinality;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph.VisitDataMinCardinality;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph.VisitDataPropertyAssertionAxiom;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph.VisitDataSomeValuesFrom;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph.VisitEquivalentClasses;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph.VisitIntersectionOf;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph.VisitObjectAllValuesFrom;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph.VisitObjectComplementOf;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph.VisitObjectExaclyCardinality;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph.VisitObjectMaxCardinality;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph.VisitObjectMinCardinality;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph.VisitObjectPropertyAssertionAxiom;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph.VisitObjectUnionOf;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph.VisitSomeValuesFrom;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
@Component
public class OntologyVisitors {

  private static final Logger LOG = LoggerFactory.getLogger(OntologyVisitors.class);
  @Autowired
    private LabelProvider labelProvider;

  public final OWLObjectVisitorEx<Boolean> isRestrictionVisitor
      = new OWLObjectVisitorEx<Boolean>() {
    @Override
    public Boolean visit(OWLSubClassOfAxiom subClassAxiom) {
      OWLClassExpression superClass = subClassAxiom.getSuperClass();
      ClassExpressionType classExpressionType = superClass.getClassExpressionType();
      return !classExpressionType.equals(ClassExpressionType.OWL_CLASS);
    }
  };

  public final OWLObjectVisitorEx<OWLSubClassOfAxiom> getAxiomElement(IRI rootIri) {

    return new OWLObjectVisitorEx() {
      @Override
      public OWLSubClassOfAxiom visit(OWLSubClassOfAxiom subClassAxiom) {
        {
          return subClassAxiom;
        }
      }
    };
  }

  public final OWLObjectVisitorEx<Map<GraphNode, Set<ExpressionReturnedClass>>> superClassAxiom(OntologyGraph vg, GraphNode node, GraphNodeType type, Boolean not) {
    return superClassAxiom(vg, node, type, false, not);
  }

  public final OWLObjectVisitorEx<Map<GraphNode, Set<ExpressionReturnedClass>>> superClassAxiom(OntologyGraph vg, GraphNode node, GraphNodeType type, Boolean equivalentTo, Boolean not) {
    LOG.debug("SuperClass Axiom Visitor {}");
    Map<GraphNode, Set<ExpressionReturnedClass>> returnedVal = new HashMap<>();
    return new OWLObjectVisitorEx() {

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectSomeValuesFrom someValuesFromAxiom) {
          return VisitSomeValuesFrom.visit(someValuesFromAxiom, returnedVal, vg, node, type, equivalentTo, not, labelProvider);
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectComplementOf axiom) {
        return VisitObjectComplementOf.visit(axiom, returnedVal, vg, node, type, equivalentTo, not, labelProvider);
      }

      @Override
      public OWLRestriction doDefault(Object object) {
        LOG.debug("Unsupported axiom: " + object);
        LOG.debug("Unsupported axiom type: " + object.getClass().getName());
        return null;
      }
      
       @Override
     public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLDataHasValue axiom) {
        return VisitDataHasValue.visit(axiom, returnedVal, vg, node, type, equivalentTo, not, labelProvider);
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectIntersectionOf axiom) {
        return VisitIntersectionOf.visit(axiom, returnedVal, vg, node, type, equivalentTo, not, labelProvider);
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLEquivalentClassesAxiom axiom) {
       return VisitEquivalentClasses.visit(axiom, returnedVal, vg, node, type, equivalentTo, not, labelProvider);

      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectExactCardinality axiom) {
          return VisitObjectExaclyCardinality.visit(axiom, returnedVal, vg, node, type, equivalentTo, not, labelProvider);
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectAllValuesFrom axiom) {
        return VisitObjectAllValuesFrom.visit(axiom, returnedVal, vg, node, type, equivalentTo, not, labelProvider);
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLDataSomeValuesFrom axiom) {
       return VisitDataSomeValuesFrom.visit(axiom, returnedVal, vg, node, type, equivalentTo, not, labelProvider);
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLDataExactCardinality axiom) {
       return VisitDataExactCardinality.visit(axiom, returnedVal, vg, node, type, equivalentTo, not, labelProvider);
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectMinCardinality axiom) {
        return VisitObjectMinCardinality.visit(axiom, returnedVal, vg, node, type, equivalentTo, not, labelProvider);
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectUnionOf axiom) {
        return VisitObjectUnionOf.visit(axiom, returnedVal, vg, node, type, equivalentTo, not, labelProvider);
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectMaxCardinality axiom) {
        return VisitObjectMaxCardinality.visit(axiom, returnedVal, vg, node, type, equivalentTo, not, labelProvider);
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLDataMaxCardinality axiom) {
        return VisitDataMaxCardinality.visit(axiom, returnedVal, vg, node, type, equivalentTo, not, labelProvider);
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLDataMinCardinality axiom) {
       return VisitDataMinCardinality.visit(axiom, returnedVal, vg, node, type, equivalentTo, not, labelProvider);
      }

    };
  }



  public final OWLAxiomVisitorEx<GraphNode> individualCompleteGraphNode(OntologyGraph vg, GraphNode node, GraphNodeType type) {

    return new OWLAxiomVisitorEx<GraphNode>() {

      @Override
      public GraphNode visit(OWLObjectPropertyAssertionAxiom ax) {
          return VisitObjectPropertyAssertionAxiom.visit(ax, vg, node, type, labelProvider);

      }

      @Override
      public GraphNode visit(OWLDataPropertyAssertionAxiom ax) {
          return VisitDataPropertyAssertionAxiom.visit(ax, vg, node, type, labelProvider);
      
      }

      @Override
      public GraphNode visit(OWLClassAssertionAxiom ax) {
           return VisitClassAssertionAxiom.visit(ax, vg, node, type, labelProvider);
      }

    };
  }

}
