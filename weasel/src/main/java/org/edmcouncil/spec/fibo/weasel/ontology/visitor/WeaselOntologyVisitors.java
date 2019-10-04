package org.edmcouncil.spec.fibo.weasel.ontology.visitor;

import java.util.stream.Collectors;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphNode;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphRelation;
import org.edmcouncil.spec.fibo.weasel.model.graph.ViewerGraph;
import org.edmcouncil.spec.fibo.weasel.ontology.data.OwlDataExtractor;
import org.edmcouncil.spec.fibo.weasel.utils.StringUtils;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLQuantifiedObjectRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class WeaselOntologyVisitors {

  public static OWLObjectVisitorEx<Boolean> isRestrictionVisitor
      = new OWLObjectVisitorEx<Boolean>() {
    @Override
    public Boolean visit(OWLSubClassOfAxiom subClassAxiom) {
      OWLClassExpression superClass = subClassAxiom.getSuperClass();
      ClassExpressionType classExpressionType = superClass.getClassExpressionType();
      return !classExpressionType.equals(ClassExpressionType.OWL_CLASS);
    }
  };

  public static OWLObjectVisitorEx<OWLSubClassOfAxiom> getAxiomElement(IRI rootIri) {

    return new OWLObjectVisitorEx() {
      @Override
      public OWLSubClassOfAxiom visit(OWLSubClassOfAxiom subClassAxiom) {
        {
          return subClassAxiom;
        }
      }
    };
  }
  //https://stackoverflow.com/questions/47980787/getting-object-properties-and-classes
  
  public static OWLObjectVisitorEx<OWLQuantifiedObjectRestriction> superClassAxiom(ViewerGraph vg, GraphNode node) {

    return new OWLObjectVisitorEx() {

      @Override
      public OWLQuantifiedObjectRestriction visit(OWLObjectSomeValuesFrom someValuesFromAxiom) {
        //getDeepGraph
        /*for (OWLEntity object : someValuesFromAxiom.signature().collect(Collectors.toList())) {
              System.out.println("IRI entity lvl 1: " + object.getIRI().getIRIString());
            }*/

        //someValuesFromAxiom.getFiller();
        /*for (OWLClassExpression owlExp : someValuesFromAxiom.nestedClassExpressions().collect(Collectors.toList())) {
              System.out.println("Expression:  " + owlExp.toString());
        }*/
        String propertyIri = null;
        propertyIri = OwlDataExtractor.extrackAxiomPropertyIri(someValuesFromAxiom);
        ClassExpressionType objectType = someValuesFromAxiom.getFiller().getClassExpressionType();

        System.out.println("Object type: " + objectType.getName());

        switch (objectType) {
          case OWL_CLASS:
            OWLClassExpression expression = someValuesFromAxiom.getFiller().getObjectComplementOf();
            String object = null;
            object = extractStringObject(expression, object);

            GraphNode endNode = new GraphNode();
            endNode.setIri(object);
            endNode.setLabel(StringUtils.getFragment(object));

            GraphRelation rel = new GraphRelation();
            rel.setIri(propertyIri);
            rel.setLabel(StringUtils.getFragment(propertyIri));
            rel.setStart(node);
            rel.setEnd(endNode);
            vg.addNode(endNode);
            vg.addRelation(rel);
            
            return null;

          case OBJECT_SOME_VALUES_FROM:
            GraphNode blankNode = new GraphNode();
            GraphRelation relSomeVal = new GraphRelation();
            relSomeVal.setIri(propertyIri);
            relSomeVal.setLabel(StringUtils.getFragment(propertyIri));
            relSomeVal.setStart(node);
            relSomeVal.setEnd(blankNode);
            vg.addNode(blankNode);
            vg.addRelation(relSomeVal);
            vg.setRoot(blankNode);
            //someValuesFromAxiom.accept(superClassAxiom(vg, blankNode));
            return someValuesFromAxiom;

        }

        //System.out.println("Object complement: " + someValuesFromAxiom.getFiller().getObjectComplementOf().toString());

        /* for (OWLEntity oWLEntity : someValuesFromAxiom.getFiller().) {
              System.out.println("IRI entity lvl: " + oWLEntity.getIRI().getIRIString());
              }*/
        //loadGraph(root, someValuesFromAxiom, vg);
        return null;
      }

      private String extractStringObject(OWLClassExpression expression, String object) {
        for (OWLEntity oWLEntity : expression.signature().collect(Collectors.toList())) {
          object = oWLEntity.toStringID();
        }
        return object;
      }

      /*@Override
      public ViewerGraph visit(OWLObjectExactCardinality exactCardinalityAxiom) {
        for (OWLEntity object : exactCardinalityAxiom.signature().collect(Collectors.toList())) {
          System.out.println("IRI: " + object.getIRI().getIRIString());
        }
        printCardinalityRestriction(exactCardinalityAxiom);
        return vg;
      }

      @Override
      public ViewerGraph visit(OWLObjectMinCardinality minCardinalityAxiom) {
        printCardinalityRestriction(minCardinalityAxiom);
        return vg;
      }

      @Override
      public ViewerGraph visit(OWLObjectMaxCardinality maxCardinalityAxiom) {
        printCardinalityRestriction(maxCardinalityAxiom);
        return vg;
      }*/

      // TODO: same for AllValuesFrom etc.
    };
  }

  public static void printQuantifiedRestriction(OWLQuantifiedObjectRestriction restriction) {
    System.out.println("\t\tClassExpressionType: " + restriction.getClassExpressionType().toString());
    System.out.println("\t\tProperty: " + restriction.getProperty().toString());
    System.out.println("\t\tObject: " + restriction.getFiller().toString());
    System.out.println();
  }

  public static void printCardinalityRestriction(OWLObjectCardinalityRestriction restriction) {
    System.out.println("\t\tClassExpressionType: " + restriction.getClassExpressionType().toString());
    System.out.println("\t\tCardinality: " + restriction.getCardinality());
    System.out.println("\t\tProperty: " + restriction.getProperty().toString());
    System.out.println("\t\tObject: " + restriction.getFiller().toString());
    System.out.println();
  }
}
