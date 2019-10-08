package org.edmcouncil.spec.fibo.weasel.ontology.visitor;

import java.util.stream.Collectors;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphNode;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphRelation;
import org.edmcouncil.spec.fibo.weasel.model.graph.ViewerGraph;
import org.edmcouncil.spec.fibo.weasel.ontology.data.OwlDataExtractor;
import org.edmcouncil.spec.fibo.weasel.utils.StringUtils;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.DataRangeType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectCardinalityRestriction;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLQuantifiedObjectRestriction;
import org.semanticweb.owlapi.model.OWLRestriction;
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
      public OWLClassExpression visit(OWLObjectSomeValuesFrom someValuesFromAxiom) {

        String propertyIri = null;
        propertyIri = OwlDataExtractor.extrackAxiomPropertyIri(someValuesFromAxiom);
        ClassExpressionType objectType = someValuesFromAxiom.getFiller().getClassExpressionType();

        //System.out.println("Object type: " + objectType.getName());
        //move switch to function, this is probably repeats
        switch (objectType) {
          case OWL_CLASS:
            OWLClassExpression expression = someValuesFromAxiom.getFiller().getObjectComplementOf();
            String object = null;
            object = extractStringObject(expression, object);

            GraphNode endNode = new GraphNode(vg.nextId());
            endNode.setIri(object);
            String label = StringUtils.getFragment(object);
            endNode.setLabel(label.substring(0, 1).toLowerCase() + label.substring(1) + "Instance");

            GraphRelation rel = new GraphRelation(vg.nextId());
            rel.setIri(propertyIri);
            rel.setLabel(StringUtils.getFragment(propertyIri));
            rel.setStart(node);
            rel.setEnd(endNode);
            vg.addNode(endNode);
            vg.addRelation(rel);

            return null;

          case OBJECT_SOME_VALUES_FROM:
          case OBJECT_EXACT_CARDINALITY:
            GraphNode blankNode = new GraphNode(vg.nextId());
            GraphRelation relSomeVal = new GraphRelation(vg.nextId());
            relSomeVal.setIri(propertyIri);
            relSomeVal.setLabel(StringUtils.getFragment(propertyIri));
            relSomeVal.setStart(node);
            relSomeVal.setEnd(blankNode);
            vg.addNode(blankNode);
            vg.addRelation(relSomeVal);
            vg.setRoot(blankNode);
            vg.setRoot(blankNode);
            //someValuesFromAxiom.accept(superClassAxiom(vg, blankNode));
            return someValuesFromAxiom.getFiller();

        }

        //System.out.println("Object complement: " + someValuesFromAxiom.getFiller().getObjectComplementOf().toString());

        /* for (OWLEntity oWLEntity : someValuesFromAxiom.getFiller().) {
              System.out.println("IRI entity lvl: " + oWLEntity.getIRI().getIRIString());
              }*/
        //loadGraph(root, someValuesFromAxiom, vg);
        return null;
      }

      @Override
      public OWLRestriction doDefault(Object object) {
        System.out.println("Unsupported axiom: " + object);
        return null;
      }

      @Override
      public OWLClassExpression visit(OWLObjectExactCardinality axiom) {
        int cardinality = axiom.getCardinality();

        String propertyIri = null;
        propertyIri = OwlDataExtractor.extrackAxiomPropertyIri(axiom);
        ClassExpressionType objectType = axiom.getFiller().getClassExpressionType();
        OWLClassExpression result = null;
        //System.out.println("Object type: " + objectType.getName());

        for (int i = 0; i < cardinality; i++) {
          switch (objectType) {
            case OWL_CLASS:
              OWLClassExpression expression = axiom.getFiller().getObjectComplementOf();
              String object = null;
              object = extractStringObject(expression, object);

              GraphNode endNode = new GraphNode(vg.nextId());
              endNode.setIri(object);
              String label = StringUtils.getFragment(object);
              String labelPostfix = cardinality>1?"Instance"+i:"Instance";
              endNode.setLabel(label.substring(0, 1).toLowerCase() + label.substring(1) + labelPostfix);

              GraphRelation rel = new GraphRelation(vg.nextId());
              rel.setIri(propertyIri);
              rel.setLabel(StringUtils.getFragment(propertyIri));
              rel.setStart(node);
              rel.setEnd(endNode);
              vg.addNode(endNode);
              vg.addRelation(rel);

              result = null;
              break;

            case OBJECT_SOME_VALUES_FROM:
            case OBJECT_EXACT_CARDINALITY:
              GraphNode blankNode = new GraphNode(vg.nextId());
              GraphRelation relSomeVal = new GraphRelation(vg.nextId());
              relSomeVal.setIri(propertyIri);
              relSomeVal.setLabel(StringUtils.getFragment(propertyIri));
              relSomeVal.setStart(node);
              relSomeVal.setEnd(blankNode);
              vg.addNode(blankNode);
              vg.addRelation(relSomeVal);
              vg.setRoot(blankNode);
              vg.setRoot(blankNode);
              result = axiom.getFiller();
              break;
              
            default:
              System.out.println("Unsupported switch case (ObjectType): " + objectType);

          }
        }
        return result;
      }

      @Override
      public OWLClassExpression visit(OWLDataSomeValuesFrom axiom) {

        String propertyIri = null;
        propertyIri = OwlDataExtractor.extrackAxiomPropertyIri(axiom);
        DataRangeType objectType = axiom.getFiller().getDataRangeType();

        System.out.println("Data range type: " + objectType.getName());

        switch (objectType) {
          case DATATYPE:
            //OWLClassExpression expression = axiom.getFiller().;
            String object = axiom.getFiller().toString();
            //object = extractStringObject(expression, object);

            GraphNode endNode = new GraphNode(vg.nextId());
            endNode.setIri(object);
            String label = StringUtils.getFragment(object);
            endNode.setLabel(label.substring(0, 1).toLowerCase() + label.substring(1));

            GraphRelation rel = new GraphRelation(vg.nextId());
            rel.setIri(propertyIri);
            rel.setLabel(StringUtils.getFragment(propertyIri));
            rel.setStart(node);
            rel.setEnd(endNode);
            vg.addNode(endNode);
            vg.addRelation(rel);

            return null;

          default:
            System.out.println("Unsupported switch case (DataRangeType): " + objectType);
        }

        //System.out.println("Object complement: " + someValuesFromAxiom.getFiller().getObjectComplementOf().toString());

        /* for (OWLEntity oWLEntity : someValuesFromAxiom.getFiller().) {
              System.out.println("IRI entity lvl: " + oWLEntity.getIRI().getIRIString());
              }*/
        //loadGraph(root, someValuesFromAxiom, vg);
        return null;
      }

      @Override
      public OWLClassExpression visit(OWLObjectMinCardinality axiom) {
        int cardinality = axiom.getCardinality();
        
        if(cardinality==0){
          return null;
        }
        
         String propertyIri = null;
        propertyIri = OwlDataExtractor.extrackAxiomPropertyIri(axiom);
        ClassExpressionType objectType = axiom.getFiller().getClassExpressionType();
        OWLClassExpression result = null;
        //System.out.println("Object type: " + objectType.getName());

        for (int i = 0; i < cardinality; i++) {
          switch (objectType) {
            case OWL_CLASS:
              OWLClassExpression expression = axiom.getFiller().getObjectComplementOf();
              String object = null;
              object = extractStringObject(expression, object);

              GraphNode endNode = new GraphNode(vg.nextId());
              endNode.setIri(object);
              String label = StringUtils.getFragment(object);
              String labelPostfix = cardinality>1?"Instance-"+(i+1):"Instance";
              endNode.setLabel(label.substring(0, 1).toLowerCase() + label.substring(1) + labelPostfix);

              GraphRelation rel = new GraphRelation(vg.nextId());
              rel.setIri(propertyIri);
              rel.setLabel(StringUtils.getFragment(propertyIri));
              rel.setStart(node);
              rel.setEnd(endNode);
              vg.addNode(endNode);
              vg.addRelation(rel);

              result = null;
              break;

            case OBJECT_SOME_VALUES_FROM:
            case OBJECT_EXACT_CARDINALITY:
              GraphNode blankNode = new GraphNode(vg.nextId());
              GraphRelation relSomeVal = new GraphRelation(vg.nextId());
              relSomeVal.setIri(propertyIri);
              relSomeVal.setLabel(StringUtils.getFragment(propertyIri));
              relSomeVal.setStart(node);
              relSomeVal.setEnd(blankNode);
              vg.addNode(blankNode);
              vg.addRelation(relSomeVal);
              vg.setRoot(blankNode);
              vg.setRoot(blankNode);
              result = axiom.getFiller();
              break;
              
            default:
              System.out.println("Unsupported switch case (ObjectType): " + objectType);

          }
        }
        return result;
      }
      /*
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

  private static String extractStringObject(OWLClassExpression expression, String object) {
    for (OWLEntity oWLEntity : expression.signature().collect(Collectors.toList())) {
      object = oWLEntity.toStringID();
    }
    return object;
  }

  //public static Visitor
}
