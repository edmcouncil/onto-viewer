package org.edmcouncil.spec.fibo.weasel.ontology.visitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphNode;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphNodeType;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphRelation;
import org.edmcouncil.spec.fibo.weasel.model.graph.ViewerGraph;
import org.edmcouncil.spec.fibo.weasel.ontology.data.extractor.OwlDataExtractor;
import org.edmcouncil.spec.fibo.weasel.ontology.data.extractor.label.LabelExtractor;
import org.edmcouncil.spec.fibo.weasel.utils.StringUtils;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.DataRangeType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class OntologyVisitors {

  private static final Logger Logger = LoggerFactory.getLogger(OntologyVisitors.class);
  private static final String DEFAULT_BLANK_NODE_LABEL = "Thing";

  @Autowired
  private LabelExtractor labelExtractor;

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

  public final OWLObjectVisitorEx<Map<GraphNode, OWLClassExpression>> superClassAxiom(ViewerGraph vg, GraphNode node, GraphNodeType type) {

    return new OWLObjectVisitorEx() {

      @Override
      public Map<GraphNode, OWLClassExpression> visit(OWLObjectSomeValuesFrom someValuesFromAxiom) {

        String propertyIri = null;
        propertyIri = OwlDataExtractor.extrackAxiomPropertyIri(someValuesFromAxiom);
        ClassExpressionType objectType = someValuesFromAxiom.getFiller().getClassExpressionType();
        Map<GraphNode, OWLClassExpression> returnedVal = new HashMap<>();

        switch (objectType) {
          case OWL_CLASS:
            OWLClassExpression expression = someValuesFromAxiom.getFiller().getObjectComplementOf();
            String iri = null;
            iri = extractStringObject(expression, iri);

            GraphNode endNode = new GraphNode(vg.nextId());
            endNode.setIri(iri);
            endNode.setType(type);
            String label = labelExtractor.getLabelOrDefaultFragment(IRI.create(iri));
            //label = label.equals("rdfs:Literal") || label.equals("Rdfs:Literal") ? "Literal" : label;
            endNode.setLabel(label);
            endNode.setType(type);

            GraphRelation rel = new GraphRelation(vg.nextId());
            rel.setIri(propertyIri);
            rel.setLabel(labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri)));
            rel.setStart(node);
            rel.setEnd(endNode);
            rel.setEndNodeType(type);
            vg.addNode(endNode);
            vg.addRelation(rel);

            return null;

          case OBJECT_SOME_VALUES_FROM:
          case OBJECT_EXACT_CARDINALITY:
          case OBJECT_MIN_CARDINALITY:
          case OBJECT_MAX_CARDINALITY:
          case DATA_MIN_CARDINALITY:
          case DATA_MAX_CARDINALITY:
            GraphNode blankNode = new GraphNode(vg.nextId());
            blankNode.setType(type);
            blankNode.setLabel(DEFAULT_BLANK_NODE_LABEL);
            GraphRelation relSomeVal = new GraphRelation(vg.nextId());
            relSomeVal.setIri(propertyIri);
            relSomeVal.setLabel(labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri)));
            relSomeVal.setStart(node);
            relSomeVal.setEnd(blankNode);
            relSomeVal.setEndNodeType(type);
            vg.addNode(blankNode);
            vg.addRelation(relSomeVal);
            vg.setRoot(blankNode);
            //someValuesFromAxiom.accept(superClassAxiom(vg, blankNode));

            returnedVal.put(blankNode, someValuesFromAxiom.getFiller());
            return returnedVal;

        }

        return null;
      }

      @Override
      public OWLRestriction doDefault(Object object) {
        System.out.println("Unsupported axiom: " + object);
        return null;
      }

      @Override
      public Map<GraphNode, OWLClassExpression> visit(OWLObjectExactCardinality axiom) {
        int cardinality = axiom.getCardinality();

        String propertyIri = null;
        propertyIri = OwlDataExtractor.extrackAxiomPropertyIri(axiom);
        ClassExpressionType objectType = axiom.getFiller().getClassExpressionType();
        Map<GraphNode, OWLClassExpression> returnedVal = new HashMap<>();

        for (int i = 0; i < cardinality; i++) {
          switch (objectType) {
            case OWL_CLASS:
              OWLClassExpression expression = axiom.getFiller().getObjectComplementOf();
              String iri = null;
              iri = extractStringObject(expression, iri);

              GraphNode endNode = new GraphNode(vg.nextId());
              endNode.setIri(iri);
              endNode.setType(type);
              String label = labelExtractor.getLabelOrDefaultFragment(IRI.create(iri));
              //label = label.equals("rdfs:Literal") || label.equals("Rdfs:Literal") ? "Literal" : label;
              String labelPostfix = cardinality > 1 ? " (" + (i + 1) + ")" : "";
              endNode.setLabel(label + labelPostfix);

              GraphRelation rel = new GraphRelation(vg.nextId());
              rel.setIri(propertyIri);
              rel.setLabel(labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri)));
              rel.setStart(node);
              rel.setEnd(endNode);
              rel.setEndNodeType(type);
              vg.addNode(endNode);
              vg.addRelation(rel);

              break;

            case OBJECT_SOME_VALUES_FROM:
            case OBJECT_EXACT_CARDINALITY:
            case OBJECT_MIN_CARDINALITY:
            case OBJECT_MAX_CARDINALITY:
            case DATA_MIN_CARDINALITY:
            case DATA_MAX_CARDINALITY:
              GraphNode blankNode = new GraphNode(vg.nextId());
              blankNode.setType(type);
              blankNode.setLabel(DEFAULT_BLANK_NODE_LABEL);
              GraphRelation relSomeVal = new GraphRelation(vg.nextId());
              relSomeVal.setIri(propertyIri);
              relSomeVal.setLabel(labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri)));
              relSomeVal.setStart(node);
              relSomeVal.setEnd(blankNode);
              relSomeVal.setEndNodeType(type);
              vg.addNode(blankNode);
              vg.addRelation(relSomeVal);
              vg.setRoot(blankNode);
              vg.setRoot(blankNode);
              returnedVal.put(blankNode, axiom.getFiller());
              break;

            default:
              System.out.println("Unsupported switch case (ObjectType): " + objectType);

          }
        }
        return returnedVal;
      }

      @Override
      public Map<GraphNode, OWLClassExpression> visit(OWLDataSomeValuesFrom axiom) {

        String propertyIri = null;
        propertyIri = OwlDataExtractor.extrackAxiomPropertyIri(axiom);
        DataRangeType objectType = axiom.getFiller().getDataRangeType();
        Map<GraphNode, OWLClassExpression> returnedVal = new HashMap<>();

        switch (objectType) {
          case DATATYPE:
            //OWLClassExpression expression = axiom.getFiller().;
            String object = axiom.getFiller().toString();
            //object = extractStringObject(expression, object);

            GraphNode endNode = new GraphNode(vg.nextId());
            endNode.setIri(object);
            endNode.setType(type);
            String label = labelExtractor.getLabelOrDefaultFragment(IRI.create(object));

            //TODO: change this to more automatic solution
            //label = label.equals("rdfs:Literal") || label.equals("Rdfs:Literal") ? "Literal" : label;
            //label = label.equals("Literal") ? label : label.substring(0, 1).toLowerCase() + label.substring(1);
            endNode.setLabel(label);

            GraphRelation rel = new GraphRelation(vg.nextId());
            rel.setIri(propertyIri);
            rel.setLabel(StringUtils.getFragment(propertyIri));
            rel.setStart(node);
            rel.setEnd(endNode);
            rel.setEndNodeType(type);
            vg.addNode(endNode);
            vg.addRelation(rel);

            return null;

          default:
            System.out.println("Unsupported switch case (DataRangeType): " + objectType);
        }

        return returnedVal;
      }

      @Override
      public Map<GraphNode, OWLClassExpression> visit(OWLObjectMinCardinality axiom) {
        int cardinality = axiom.getCardinality();
        boolean isOptional = cardinality == 0;
        cardinality = cardinality == 0 ? 1 : cardinality;

        String propertyIri = null;
        propertyIri = OwlDataExtractor.extrackAxiomPropertyIri(axiom);
        ClassExpressionType objectType = axiom.getFiller().getClassExpressionType();
        Map<GraphNode, OWLClassExpression> returnedVal = new HashMap<>();

        for (int i = 0; i < cardinality; i++) {
          switch (objectType) {
            case OWL_CLASS:
              OWLClassExpression expression = axiom.getFiller().getObjectComplementOf();
              String object = null;
              object = extractStringObject(expression, object);

              GraphNode endNode = new GraphNode(vg.nextId());
              endNode.setIri(object);
              endNode.setType(type);
              if (cardinality == 0) {
                endNode.setOptional(true);
              }
              String label = labelExtractor.getLabelOrDefaultFragment(IRI.create(object));
              String labelPostfix = cardinality > 1 ? " (" + (i + 1) + ")": "";
              //label = label.equals("rdfs:Literal") || label.equals("Rdfs:Literal") ? "Literal" : label;
              //label = label.equals("Literal") ? label : label.substring(0, 1).toLowerCase() + label.substring(1);
              endNode.setLabel(label + labelPostfix);

              GraphRelation rel = new GraphRelation(vg.nextId());
              rel.setIri(propertyIri);
              rel.setLabel(labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri)));
              rel.setStart(node);
              rel.setEnd(endNode);
              rel.setOptional(isOptional);
              rel.setEndNodeType(type);
              vg.addNode(endNode);
              vg.addRelation(rel);
              break;

            case OBJECT_SOME_VALUES_FROM:
            case OBJECT_EXACT_CARDINALITY:
            case OBJECT_MIN_CARDINALITY:
            case OBJECT_MAX_CARDINALITY:
            case DATA_MIN_CARDINALITY:
            case DATA_MAX_CARDINALITY:
              GraphNode blankNode = new GraphNode(vg.nextId());
              blankNode.setType(type);
              blankNode.setLabel(DEFAULT_BLANK_NODE_LABEL);
              GraphRelation relSomeVal = new GraphRelation(vg.nextId());
              relSomeVal.setIri(propertyIri);
              relSomeVal.setLabel(labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri)));
              relSomeVal.setStart(node);
              relSomeVal.setEnd(blankNode);
              relSomeVal.setOptional(isOptional);
              relSomeVal.setEndNodeType(type);
              vg.addNode(blankNode);
              vg.addRelation(relSomeVal);
              vg.setRoot(blankNode);

              returnedVal.put(blankNode, axiom.getFiller());
              break;

            default:
              System.out.println("Unsupported switch case (ObjectType): " + objectType);

          }
        }
        return returnedVal;
      }

      @Override
      public Map<GraphNode, OWLClassExpression> visit(OWLObjectMaxCardinality axiom) {
        int cardinality = axiom.getCardinality();
        boolean isOptional = cardinality == 1;
        cardinality = cardinality == 0 ? 1 : cardinality;

        String propertyIri = null;
        propertyIri = OwlDataExtractor.extrackAxiomPropertyIri(axiom);
        ClassExpressionType objectType = axiom.getFiller().getClassExpressionType();
        Map<GraphNode, OWLClassExpression> returnedVal = new HashMap<>();

        for (int i = 0; i < cardinality; i++) {
          switch (objectType) {
            case OWL_CLASS:
              OWLClassExpression expression = axiom.getFiller().getObjectComplementOf();
              String object = null;
              object = extractStringObject(expression, object);

              GraphNode endNode = new GraphNode(vg.nextId());
              endNode.setIri(object);
              endNode.setType(type);
              if (cardinality == 0) {
                endNode.setOptional(true);
              }
              String label = labelExtractor.getLabelOrDefaultFragment(IRI.create(object));
              String labelPostfix = cardinality > 1 ? " (" + (i + 1) + ")" : "";
              //label = label.equals("rdfs:Literal") || label.equals("Rdfs:Literal") ? "Literal" : label;
              //label = label.equals("Literal") ? label : label.substring(0, 1).toLowerCase() + label.substring(1);
              endNode.setLabel(label + labelPostfix);

              GraphRelation rel = new GraphRelation(vg.nextId());
              rel.setIri(propertyIri);
              rel.setLabel(labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri)));
              rel.setStart(node);
              rel.setEnd(endNode);
              rel.setOptional(isOptional);
              rel.setEndNodeType(type);
              vg.addNode(endNode);
              vg.addRelation(rel);
              break;

            case OBJECT_SOME_VALUES_FROM:
            case OBJECT_EXACT_CARDINALITY:
            case OBJECT_MIN_CARDINALITY:
            case OBJECT_MAX_CARDINALITY:
            case DATA_MIN_CARDINALITY:
            case DATA_MAX_CARDINALITY:
              GraphNode blankNode = new GraphNode(vg.nextId());
              blankNode.setType(type);
              blankNode.setLabel(DEFAULT_BLANK_NODE_LABEL);
              GraphRelation relSomeVal = new GraphRelation(vg.nextId());
              relSomeVal.setIri(propertyIri);
              relSomeVal.setLabel(labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri)));
              relSomeVal.setStart(node);
              relSomeVal.setEnd(blankNode);
              relSomeVal.setOptional(isOptional);
              relSomeVal.setEndNodeType(type);
              vg.addNode(blankNode);
              vg.addRelation(relSomeVal);
              vg.setRoot(blankNode);
              vg.setRoot(blankNode);
              returnedVal.put(blankNode, axiom.getFiller());
              break;

            default:
              System.out.println("Unsupported switch case (ObjectType): " + objectType);

          }
        }
        return returnedVal;
      }

      @Override
      public Map<GraphNode, OWLClassExpression> visit(OWLDataMaxCardinality axiom) {
        int cardinality = axiom.getCardinality();
        boolean isOptional = cardinality == 1;
        cardinality = cardinality == 0 ? 1 : cardinality;

        String propertyIri = null;
        propertyIri = OwlDataExtractor.extrackAxiomPropertyIri(axiom);
        DataRangeType objectType = axiom.getFiller().getDataRangeType();
        Map<GraphNode, OWLClassExpression> returnedVal = new HashMap<>();

        for (int i = 0; i < cardinality; i++) {

          switch (objectType) {
            case DATATYPE:
              //OWLClassExpression expression = axiom.getFiller().;
              String object = axiom.getFiller().toString();
              //object = extractStringObject(expression, object);

              GraphNode endNode = new GraphNode(vg.nextId());
              endNode.setIri(object);
              endNode.setType(type);
              String label = labelExtractor.getLabelOrDefaultFragment(IRI.create(object));
              String labelPostfix = cardinality > 1 ? " (" + (i + 1) + ")" : "";
              //label = label.equals("rdfs:Literal") || label.equals("Rdfs:Literal") ? "Literal" : label;
              //label = label.equals("Literal") ? label : label.substring(0, 1).toLowerCase() + label.substring(1);
              endNode.setLabel(label + labelPostfix);

              GraphRelation rel = new GraphRelation(vg.nextId());
              rel.setIri(propertyIri);
              rel.setLabel(labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri)));
              rel.setStart(node);
              rel.setEnd(endNode);
              rel.setOptional(isOptional);
              rel.setEndNodeType(type);
              vg.addNode(endNode);
              vg.addRelation(rel);

              return null;

            default:
              System.out.println("Unsupported switch case (DataRangeType): " + objectType);
          }

        }
        return returnedVal;
      }

      @Override
      public Map<GraphNode, OWLClassExpression> visit(OWLDataMinCardinality axiom) {
        int cardinality = axiom.getCardinality();
        boolean isOptional = cardinality == 0;
        cardinality = cardinality == 0 ? 1 : cardinality;

        String propertyIri = null;
        propertyIri = OwlDataExtractor.extrackAxiomPropertyIri(axiom);
        DataRangeType objectType = axiom.getFiller().getDataRangeType();
        Map<GraphNode, OWLClassExpression> returnedVal = new HashMap<>();

        for (int i = 0; i < cardinality; i++) {

          switch (objectType) {
            case DATATYPE:
              //OWLClassExpression expression = axiom.getFiller().;
              String object = axiom.getFiller().toString();
              //object = extractStringObject(expression, object);

              GraphNode endNode = new GraphNode(vg.nextId());
              endNode.setIri(object);
              endNode.setType(type);
              String label = labelExtractor.getLabelOrDefaultFragment(IRI.create(object));
              String labelPostfix = cardinality > 1 ? " (" + (i + 1) + ")" : "";
              //label = label.equals("rdfs:Literal") || label.equals("Rdfs:Literal") ? "Literal" : label;
              //label = label.equals("Literal") ? label : label.substring(0, 1).toLowerCase() + label.substring(1);
              endNode.setLabel(label + labelPostfix);

              GraphRelation rel = new GraphRelation(vg.nextId());
              rel.setIri(propertyIri);
              rel.setLabel(labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri)));
              rel.setStart(node);
              rel.setEnd(endNode);
              rel.setOptional(isOptional);
              rel.setEndNodeType(type);
              vg.addNode(endNode);
              vg.addRelation(rel);

              return null;

            default:
              System.out.println("Unsupported switch case (DataRangeType): " + objectType);
          }

        }
        return returnedVal;
      }
      // TODO: same for AllValuesFrom etc.
    };
  }

  private final String extractStringObject(OWLClassExpression expression, String object) {
    for (OWLEntity oWLEntity : expression.signature().collect(Collectors.toList())) {
      object = oWLEntity.toStringID();
    }
    return object;
  }
}
