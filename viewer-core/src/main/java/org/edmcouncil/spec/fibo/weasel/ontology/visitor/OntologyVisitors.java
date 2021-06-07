package org.edmcouncil.spec.fibo.weasel.ontology.visitor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphNode;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphNodeType;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphRelation;
import org.edmcouncil.spec.fibo.weasel.model.graph.OntologyGraph;
import org.edmcouncil.spec.fibo.weasel.ontology.data.extractor.OwlDataExtractor;
import org.edmcouncil.spec.fibo.weasel.ontology.data.label.provider.LabelProvider;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.DataRangeType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLEntity;
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

  private static final String THING_IRI = "http://www.w3.org/2002/07/owl#Thing";
  private static final Logger LOG = LoggerFactory.getLogger(OntologyVisitors.class);
  private static final String DEFAULT_BLANK_NODE_LABEL = "Thing";
  private static final String POSTFIX_FORMAT = " (%s..%s)";

  @Autowired
  private LabelProvider labelExtractor;

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
        LOG.debug("visit OWLObjectSomeValuesFrom: {}", someValuesFromAxiom.toString());
        String propertyIri = null;
        propertyIri = OwlDataExtractor.extractAxiomPropertyIri(someValuesFromAxiom);
        ClassExpressionType objectType = someValuesFromAxiom.getFiller().getClassExpressionType();

        switch (objectType) {
          case OWL_CLASS:
            OWLClassExpression expression = someValuesFromAxiom.getFiller().getObjectComplementOf();
            String iri = null;
            iri = extractStringObject(expression, iri);

            GraphNode endNode = new GraphNode(vg.nextId());
            endNode.setIri(iri);
            endNode.setType(type);
            String label = getPrepareLabel(labelExtractor, iri, not);
            endNode.setLabel(label);
            endNode.setType(type);

            GraphRelation rel = new GraphRelation(vg.nextId());
            rel.setIri(propertyIri);

            String labelPostfix = " (1..*)";
            String relLabel = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
            rel.setLabel(relLabel + labelPostfix);

            rel.setStart(node);
            rel.setEnd(endNode);
            endNode.setIncommingRelation(rel);
            if (node.getIncommingRelation() != null) {
              rel.setOptional(node.getIncommingRelation().isOptional());
            } else rel.setOptional(false);
            rel.setEndNodeType(type);
            vg.addNode(endNode);
            vg.addRelation(rel);

            return null;

          case OBJECT_ALL_VALUES_FROM:
          case OBJECT_EXACT_CARDINALITY:
          case OBJECT_MIN_CARDINALITY:
          case OBJECT_MAX_CARDINALITY:
          case DATA_MIN_CARDINALITY:
          case DATA_MAX_CARDINALITY:
          case OBJECT_INTERSECTION_OF:

          case OBJECT_UNION_OF:
            GraphNode blankNode = new GraphNode(vg.nextId());
            blankNode.setType(type);
            blankNode.setLabel(DEFAULT_BLANK_NODE_LABEL);
            blankNode.setIri(THING_IRI);
            GraphRelation relSomeVal = new GraphRelation(vg.nextId());
            blankNode.setIncommingRelation(relSomeVal);
            relSomeVal.setIri(propertyIri);
            String labelPostfix2 = " (1..*)";
            relSomeVal.setLabel(labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri)) + labelPostfix2);
            relSomeVal.setStart(node);
            relSomeVal.setEnd(blankNode);
            relSomeVal.setEndNodeType(type);
//            if (node.getIncommingRelation() != null) {
//              relSomeVal.setOptional(node.getIncommingRelation().isOptional());
//            }
            relSomeVal.setOptional(false);
            vg.addNode(blankNode);
            vg.addRelation(relSomeVal);
            vg.setRoot(blankNode);
            LOG.debug("{}->{}", objectType.toString(), someValuesFromAxiom.toString());
            addValue(returnedVal, blankNode, someValuesFromAxiom.getFiller());
            return returnedVal;
          default:
               LOG.debug("Unsupported expression type in somevaluesfrom {}", objectType);
            addValue(returnedVal, node, someValuesFromAxiom.getFiller());
            return returnedVal;

        }

      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectComplementOf axiom) {
        LOG.debug("visit OWLObjectComplementOf: {}", axiom.toString());
        String propertyIri = "";

        LOG.debug("Class Expresion OOCO: {}", axiom.getClassExpressionType().toString());

        ClassExpressionType objectType = axiom.getClassExpressionType();
        OWLClassExpression expression = axiom.getNNF();
        objectType = expression.getClassExpressionType();
        LOG.debug("Expresion:  {}", axiom.getNNF().toString());
        switch (objectType) {

          case OWL_CLASS:

            String iri = null;
            iri = extractStringObject(expression, iri);

            GraphNode endNode = new GraphNode(vg.nextId());
            endNode.setIri(iri);
            endNode.setType(type);
            String label = getPrepareLabel(labelExtractor, iri, Boolean.TRUE);

            endNode.setLabel(label);
            endNode.setType(type);

            GraphRelation rel = new GraphRelation(vg.nextId());
            endNode.setIncommingRelation(rel);
            if (node.getIncommingRelation() != null) {
              rel.setOptional(node.getIncommingRelation().isOptional());
            }
            rel.setIri(propertyIri);

            String labelPostfix = " (1..*)";
            String relLabel = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
            rel.setLabel(relLabel + labelPostfix);

            rel.setStart(node);
            rel.setEnd(endNode);
            rel.setEndNodeType(type);
            vg.addNode(endNode);
            vg.addRelation(rel);

            return null;

          case OBJECT_ALL_VALUES_FROM:
          case OBJECT_SOME_VALUES_FROM:
          case OBJECT_EXACT_CARDINALITY:
          case OBJECT_MIN_CARDINALITY:
          case OBJECT_MAX_CARDINALITY:
          case DATA_MIN_CARDINALITY:
          case DATA_MAX_CARDINALITY:
          case OBJECT_UNION_OF:

            addValueWithNotAndEquivalent(returnedVal, node, axiom.getOperand(), false, true);
            return returnedVal;

          default:
            LOG.debug("Unsupported expression type {}", objectType);
            break;

        }

        return returnedVal;
      }

      @Override
      public OWLRestriction doDefault(Object object) {
        LOG.debug("Unsupported axiom: " + object);
        LOG.debug("Unsupported axiom type: " + object.getClass().getName());
        return null;
      }
      
       @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLDataHasValue axiom) {
        LOG.debug("visit OWLDataHasValue");
        
        String xsdBooleanIri  = "http://www.w3.org/2001/XMLSchema#boolean";
        String val = axiom.getFiller().getLiteral();
        
        
        GraphNode endNode = new GraphNode(vg.nextId());
            endNode.setIri(xsdBooleanIri);
            endNode.setType(type);
            endNode.setLabel(val);
            endNode.setType(type);

        String propertyIri =  OwlDataExtractor.extractAxiomPropertyIri(axiom);
        
        GraphRelation rel = new GraphRelation(vg.nextId());
            endNode.setIncommingRelation(rel);
            if (node.getIncommingRelation() != null) {
              rel.setOptional(node.getIncommingRelation().isOptional());
            }
            rel.setIri(propertyIri);
        
        String relLabel = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
            rel.setLabel(relLabel);

            rel.setStart(node);
            rel.setEnd(endNode);
            rel.setEndNodeType(type);
            vg.addNode(endNode);
            vg.addRelation(rel);
        
        return null;
        
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectIntersectionOf axiom) {
        LOG.debug("visit OWLObjectIntersectionOf");
        Set<OWLClassExpression> axiomConjunct = axiom.conjunctSet().collect(Collectors.toSet());

        if (couldAndRelationBeShorter(axiomConjunct)) {
          LOG.debug("And Relation Can Be Shorter!");
          addValue(returnedVal, node, axiomConjunct.stream().findAny().orElse(null));
          return returnedVal;
        }
        GraphNode blankNode = null;
        if(node.getLabel().equals(DEFAULT_BLANK_NODE_LABEL)) blankNode = node;
        else blankNode = new GraphNode(vg.nextId());
            
        
        blankNode.setType(type);
        blankNode.setIri(THING_IRI);
        blankNode.setLabel("and");
        GraphRelation relSomeVal = new GraphRelation(vg.nextId());
        relSomeVal.setIri(DEFAULT_BLANK_NODE_LABEL);

//        String relIri = null;
//              relIri = extractStringObject(axiom, relIri);
//         OWLClassExpression firstLabel = axiomConjunct.stream().findFirst().orElse(null);
//         LOG.debug("OntologyVisitors -> firstLabel {}", firstLabel.toString());
//        if (relIri != null) {
//         String label = labelExtractor.getLabelOrDefaultFragment(IRI.create(relIri));
//         relSomeVal.setLabel(label);
//         relSomeVal.setIri(relIri);
//        }


if(blankNode.getId()!=node.getId()){
        relSomeVal.setStart(node);
        relSomeVal.setEnd(blankNode);
        relSomeVal.setEndNodeType(type);
        relSomeVal.setEquivalentTo(equivalentTo);
        blankNode.setIncommingRelation(relSomeVal);
        if (node.getIncommingRelation() != null) {
          relSomeVal.setOptional(node.getIncommingRelation().isOptional());
        }
        vg.addNode(blankNode);
        vg.addRelation(relSomeVal);
        vg.setRoot(blankNode);
        vg.setRoot(blankNode);
    }
        for (OWLClassExpression owlClassExpression : axiomConjunct) {
          LOG.debug("getClassExpressionType {}", owlClassExpression.getClassExpressionType());

          ClassExpressionType objectType = owlClassExpression.getClassExpressionType();
          switch (objectType) {
            case OWL_CLASS:
              String iri = null;
              iri = extractStringObject(owlClassExpression, iri);

              GraphNode endNode = new GraphNode(vg.nextId());
              endNode.setIri(iri);
              endNode.setType(type);
              String label = labelExtractor.getLabelOrDefaultFragment(IRI.create(iri));
              endNode.setLabel(label);

              GraphRelation rel = new GraphRelation(vg.nextId());
              String labelPostfix = " (1..*)";
              //String relLabel = labelExtractor.getLabelOrDefaultFragment(IRI.create(iri));
              //rel.setLabel( labelPostfix);
              rel.setStart(blankNode);
              rel.setEnd(endNode);
              rel.setEndNodeType(type);
              endNode.setIncommingRelation(rel);
              if (blankNode.getIncommingRelation() != null) {
                rel.setOptional(blankNode.getIncommingRelation().isOptional());
              }
              vg.addNode(endNode);
              vg.addRelation(rel);

              break;

            default:
              LOG.debug("Object type {}, Expression {}", objectType, owlClassExpression);
              addValue(returnedVal, blankNode, owlClassExpression);

          }
        }
        return returnedVal;
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLEquivalentClassesAxiom axiom) {
        LOG.debug("visit OWLEquivalentClassesAxiom");

        Set<OWLClassExpression> set = axiom.classExpressions().collect(Collectors.toSet());
        for (OWLClassExpression owlClassExpression : set) {
          LOG.debug("Visitor owlClassExpression {}", owlClassExpression.toString());
          Set<OWLEntity> classExprssionSignature = owlClassExpression.signature().collect(Collectors.toSet());
          boolean isTheSameIri = false;
          for (OWLEntity owlEntity : classExprssionSignature) {
            LOG.debug("Class Expression signature {}", owlEntity.toStringID());
            if (node.getIri().equals(owlEntity.toStringID())) {
              isTheSameIri = true;
            }
          }
          if (classExprssionSignature.size() == 1 && isTheSameIri) {
            continue;
          }
          addValueWithNotAndEquivalent(returnedVal, node, owlClassExpression, Boolean.TRUE, Boolean.FALSE);
        }

        return returnedVal;

      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectExactCardinality axiom) {
        int cardinality = axiom.getCardinality();
        LOG.debug("visit OWLObjectExactCardinality: {}", axiom.toString());
        String propertyIri = null;
        propertyIri = OwlDataExtractor.extractAxiomPropertyIri(axiom);
        ClassExpressionType objectType = axiom.getFiller().getClassExpressionType();

        switch (objectType) {
          case OWL_CLASS:
            OWLClassExpression expression = axiom.getFiller().getObjectComplementOf();
            String iri = null;
            iri = extractStringObject(expression, iri);

            GraphNode endNode = new GraphNode(vg.nextId());
            endNode.setIri(iri);
            endNode.setType(type);
            String label = getPrepareLabel(labelExtractor, iri, not);
            endNode.setLabel(label);
            String labelPostfix = String.format(POSTFIX_FORMAT, cardinality, cardinality);//" (1..1)";

            GraphRelation rel = new GraphRelation(vg.nextId());
            rel.setIri(propertyIri);
            String relLabel = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
            rel.setLabel(relLabel + labelPostfix);

            rel.setStart(node);
            rel.setEnd(endNode);
            rel.setEndNodeType(type);
            endNode.setIncommingRelation(rel);
            if (node.getIncommingRelation() != null) {
              rel.setOptional(node.getIncommingRelation().isOptional());
            }
            vg.addNode(endNode);
            vg.addRelation(rel);

            break;

          case OBJECT_SOME_VALUES_FROM:
          case OBJECT_EXACT_CARDINALITY:
          case OBJECT_MIN_CARDINALITY:
          case OBJECT_MAX_CARDINALITY:
          case DATA_MIN_CARDINALITY:
          case DATA_MAX_CARDINALITY:
          case OBJECT_ALL_VALUES_FROM:
          case OBJECT_UNION_OF:
            GraphNode blankNode = new GraphNode(vg.nextId());
            blankNode.setType(type);
            blankNode.setLabel(DEFAULT_BLANK_NODE_LABEL);
            blankNode.setIri(THING_IRI);
            GraphRelation relSomeVal = new GraphRelation(vg.nextId());
            relSomeVal.setIri(propertyIri);

            String labelPostfix2 = " (1..1)";

            String relLabel2 = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
            relSomeVal.setLabel(relLabel2 + labelPostfix2);

            relSomeVal.setStart(node);
            relSomeVal.setEnd(blankNode);
            relSomeVal.setEndNodeType(type);
            blankNode.setIncommingRelation(relSomeVal);
            if (node.getIncommingRelation() != null) {
              relSomeVal.setOptional(node.getIncommingRelation().isOptional());
            }
            vg.addNode(blankNode);
            vg.addRelation(relSomeVal);
            vg.setRoot(blankNode);

            addValue(returnedVal, blankNode, axiom.getFiller());
            break;

          default:
            LOG.debug("Unsupported switch case (ObjectType): " + objectType);

        }
        return returnedVal;
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectAllValuesFrom axiom) {
        LOG.debug("visit OWLObjectAllValuesFrom: {}", axiom.toString());

        String propertyIri = null;
        propertyIri = OwlDataExtractor.extractAxiomPropertyIri(axiom);
        ClassExpressionType objectType = axiom.getFiller().getClassExpressionType();

        switch (objectType) {
          case OWL_CLASS:
            OWLClassExpression expression = axiom.getFiller().getObjectComplementOf();
            String iri = null;
            iri = extractStringObject(expression, iri);

            GraphNode endNode = new GraphNode(vg.nextId());
            endNode.setIri(iri);
            endNode.setType(type);

            String label = getPrepareLabel(labelExtractor, iri, not);
            endNode.setLabel(label);
            endNode.setOptional(true);

            GraphRelation rel = new GraphRelation(vg.nextId());
            rel.setIri(propertyIri);

            String labelPostfix = " (0..*)";
            String relLabel = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
            rel.setLabel(relLabel + labelPostfix);

            rel.setStart(node);
            rel.setEnd(endNode);
            rel.setEndNodeType(type);
            rel.setOptional(true);
            endNode.setIncommingRelation(rel);
            
            rel.setOptional(true);
            endNode.setOptional(true);
            
            //if (node.getIncommingRelation() != null) {
              //node.getIncommingRelation().setOptional(true);
            //}
            vg.addNode(endNode);
            vg.addRelation(rel);

            break;

          case OBJECT_SOME_VALUES_FROM:
          case OBJECT_EXACT_CARDINALITY:
          case OBJECT_MIN_CARDINALITY:
          case OBJECT_MAX_CARDINALITY:
          case DATA_MIN_CARDINALITY:
          case DATA_MAX_CARDINALITY:
          case OBJECT_ALL_VALUES_FROM:
          case OBJECT_UNION_OF:
            GraphNode blankNode = new GraphNode(vg.nextId());
            blankNode.setType(type);
            blankNode.setLabel(DEFAULT_BLANK_NODE_LABEL);
            GraphRelation relSomeVal = new GraphRelation(vg.nextId());
            relSomeVal.setIri(propertyIri);
            blankNode.setIri(THING_IRI);
            blankNode.setOptional(true);
            blankNode.setIncommingRelation(relSomeVal);

            String labelPostfix2 = "(1..*)";
            String relLabel2 = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
            relSomeVal.setLabel(relLabel2 + labelPostfix2);

            relSomeVal.setStart(node);
            relSomeVal.setEnd(blankNode);
            relSomeVal.setEndNodeType(type);
            relSomeVal.setOptional(true);
            if (node.getIncommingRelation() != null) {
              node.getIncommingRelation().setOptional(true);
            }
            vg.addNode(blankNode);
            vg.addRelation(relSomeVal);
            vg.setRoot(blankNode);
            addValue(returnedVal, blankNode, axiom.getFiller());
            break;
          default:
            LOG.debug("Unsupported switch case (ObjectType): " + objectType);
        }
        return returnedVal;
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLDataSomeValuesFrom axiom) {
        LOG.debug("visit OWLDataSomeValuesFrom: {}", axiom.toString());
        String propertyIri = null;
        propertyIri = OwlDataExtractor.extractAxiomPropertyIri(axiom);
        DataRangeType objectType = axiom.getFiller().getDataRangeType();

        switch (objectType) {
          case DATATYPE:
            IRI datatypeIri = axiom.getFiller().signature().findFirst().get().getIRI();

            GraphNode endNode = new GraphNode(vg.nextId());
            endNode.setIri(datatypeIri.toString());
            endNode.setType(type);
            String label = labelExtractor.getLabelOrDefaultFragment(datatypeIri);
            endNode.setLabel(label);

            GraphRelation rel = new GraphRelation(vg.nextId());
            rel.setIri(propertyIri);
            String labelPostfix = " (1..*)";
            String relLabel = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
            rel.setLabel(relLabel + labelPostfix);

            rel.setStart(node);
            rel.setEnd(endNode);
            rel.setEndNodeType(type);
            rel.setOptional(node.isOptional());
            endNode.setIncommingRelation(rel);
            if (node.getIncommingRelation() != null) {
              rel.setOptional(node.getIncommingRelation().isOptional());
            }
            vg.addNode(endNode);
            vg.addRelation(rel);

            return null;

          default:
            LOG.debug("Unsupported switch case (DataRangeType): {}", objectType);
        }

        return returnedVal;
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLDataExactCardinality axiom) {
        LOG.debug("visit OWLDataExactCardinality: {}", axiom.toString());
        String propertyIri = null;
        int cardinality = axiom.getCardinality();
        propertyIri = OwlDataExtractor.extractAxiomPropertyIri(axiom);
        DataRangeType objectType = axiom.getFiller().getDataRangeType();

        switch (objectType) {
          case DATATYPE:
            IRI datatypeIri = axiom.getFiller().signature().findFirst().get().getIRI();

            GraphNode endNode = new GraphNode(vg.nextId());
            endNode.setIri(datatypeIri.toString());
            endNode.setType(type);
            String label = labelExtractor.getLabelOrDefaultFragment(datatypeIri);
            endNode.setLabel(label);

            GraphRelation rel = new GraphRelation(vg.nextId());
            rel.setIri(propertyIri);

            String labelPostfix = String.format(POSTFIX_FORMAT, cardinality, cardinality);
            String relLabel = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
            rel.setLabel(relLabel + labelPostfix);

            rel.setStart(node);
            rel.setEnd(endNode);
            rel.setEndNodeType(type);
            //rel.setOptional(node.isOptional());
            endNode.setIncommingRelation(rel);
            if (node.getIncommingRelation() != null) {
              rel.setOptional(node.getIncommingRelation().isOptional());
            }
            vg.addNode(endNode);
            vg.addRelation(rel);

            return null;

          default:
            LOG.debug("Unsupported switch case (DataRangeType): {}", objectType);
        }

        return returnedVal;
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectMinCardinality axiom) {
        LOG.debug("visit OWLObjectMinCardinality: {}", axiom.toString());
        int cardinality = axiom.getCardinality();
        boolean isOptional = cardinality == 0;

        String propertyIri = null;
        propertyIri = OwlDataExtractor.extractAxiomPropertyIri(axiom);
        ClassExpressionType objectType = axiom.getFiller().getClassExpressionType();

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

            endNode.setLabel(label);

            GraphRelation rel = new GraphRelation(vg.nextId());
            rel.setIri(propertyIri);
            String labelPostfix = String.format(POSTFIX_FORMAT, cardinality, "*");
            String relLabel = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
            rel.setLabel(relLabel + labelPostfix);

            rel.setStart(node);
            rel.setEnd(endNode);
            endNode.setIncommingRelation(rel);
            rel.setOptional(isOptional);
            if (isOptional && node.getIncommingRelation() != null) {
              node.getIncommingRelation().setOptional(isOptional);
            }
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
          case OBJECT_ALL_VALUES_FROM:
          case OBJECT_INTERSECTION_OF:
          case OBJECT_UNION_OF:
            GraphNode blankNode = new GraphNode(vg.nextId());
            blankNode.setType(type);
            blankNode.setLabel(DEFAULT_BLANK_NODE_LABEL);
            blankNode.setIri(THING_IRI);
            GraphRelation relSomeVal = new GraphRelation(vg.nextId());
            relSomeVal.setIri(propertyIri);

//            String labelPostfix2 = " (0..*)";
//            String relLabel2 = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
//            relSomeVal.setLabel(relLabel2 + labelPostfix2);
            String labelPostfix2 = String.format(POSTFIX_FORMAT, cardinality, "*");
            String relLabel2 = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
            relSomeVal.setLabel(relLabel2 + labelPostfix2);

            relSomeVal.setStart(node);
            relSomeVal.setEnd(blankNode);
            blankNode.setIncommingRelation(relSomeVal);
            if (isOptional && node.getIncommingRelation() != null) {
              node.getIncommingRelation().setOptional(isOptional);
            }
            relSomeVal.setOptional(isOptional);
            relSomeVal.setEndNodeType(type);
            vg.addNode(blankNode);
            vg.addRelation(relSomeVal);
            vg.setRoot(blankNode);

            addValue(returnedVal, blankNode, axiom.getFiller());
            break;          
            
            
          default:
              addValue(returnedVal, node, axiom.getFiller());
            LOG.debug("Unsupported switch case (ObjectType): {}", objectType);

        }
        return returnedVal;
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectUnionOf axiom) {
        LOG.debug("visit OWLObjectUnionOf: {}", axiom.toString());
        String propertyIri = "";
        LOG.debug("object type: {}", axiom.getClassExpressionType().toString());
        ClassExpressionType objectType = axiom.getClassExpressionType();

        if (node.getLabel().equals(DEFAULT_BLANK_NODE_LABEL) || node.getLabel().equals("or")) {
          switch (objectType) {
            case OWL_CLASS:
              LOG.debug("OWL_CLASS: {}", axiom.getObjectComplementOf().toString());
              OWLClassExpression expression = axiom.getObjectComplementOf();
              String object = null;

              object = extractStringObject(expression, object);

              GraphNode endNode = new GraphNode(vg.nextId());
              endNode.setIri(object);
              endNode.setType(type);
              String label = labelExtractor.getLabelOrDefaultFragment(IRI.create(object));

              endNode.setLabel(label);

              GraphRelation rel = new GraphRelation(vg.nextId());
              rel.setIri(propertyIri);

              String relLabel = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
              rel.setLabel(relLabel);
              rel.setEquivalentTo(equivalentTo);
              rel.setStart(node);
              rel.setEnd(endNode);
              rel.setEndNodeType(type);
              endNode.setIncommingRelation(rel);
              if (node.getIncommingRelation() != null) {
                node.getIncommingRelation().setOptional(true);
              }
              rel.setOptional(true);
              vg.addNode(endNode);
              vg.addRelation(rel);
              break;

            case OBJECT_SOME_VALUES_FROM:
            case OBJECT_EXACT_CARDINALITY:
            case OBJECT_MIN_CARDINALITY:
            case OBJECT_MAX_CARDINALITY:
            case DATA_MIN_CARDINALITY:
            case DATA_MAX_CARDINALITY:
            case OBJECT_ALL_VALUES_FROM:

              GraphNode blankNode = new GraphNode(vg.nextId());
              blankNode.setType(type);
              blankNode.setLabel(DEFAULT_BLANK_NODE_LABEL);
              blankNode.setIri(THING_IRI);
              GraphRelation relSomeVal = new GraphRelation(vg.nextId());
              relSomeVal.setIri(propertyIri);

              String relLabel2 = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
              relSomeVal.setLabel(relLabel2);
              relSomeVal.setEquivalentTo(equivalentTo);
              relSomeVal.setStart(node);
              relSomeVal.setEnd(blankNode);
              relSomeVal.setOptional(true);
              relSomeVal.setEndNodeType(type);
              blankNode.setIncommingRelation(relSomeVal);
              if (node.getIncommingRelation() != null) {
                node.getIncommingRelation().setOptional(true);
              }
              vg.addNode(blankNode);
              vg.addRelation(relSomeVal);
              vg.setRoot(blankNode);

              addValue(returnedVal, blankNode, axiom.getObjectComplementOf());
              break;
            case OBJECT_UNION_OF:

              GraphNode unionRootNode = node;
              unionRootNode.setLabel("or");

              LOG.debug("OWLObjectUnionOf axiom with owl entity {}", axiom.signature().collect(Collectors.toList()));
              for (OWLEntity owlEntity : axiom.signature().collect(Collectors.toList())) {
                LOG.debug("OWLObjectUnionOf axiom with owl entity {}", owlEntity);
                GraphNode unionNode = new GraphNode(vg.nextId());
                unionNode.setLabel(labelExtractor.getLabelOrDefaultFragment(owlEntity));
                unionNode.setIri(owlEntity.getIRI().toString());
                unionNode.setType(type);

                GraphRelation unionRel = new GraphRelation(vg.nextId());
                unionRel.setStart(unionRootNode);
                unionRel.setEnd(unionNode);
                unionRel.setEquivalentTo(equivalentTo);
                unionRel.setEndNodeType(type);
                unionNode.setIncommingRelation(unionRel);
                if (node.getIncommingRelation() != null) {
                  unionRel.setOptional(unionRootNode.getIncommingRelation().isOptional());
                }

                vg.addNode(unionNode);
                vg.addRelation(unionRel);
              }

              break;
            default:
              LOG.debug("Unsupported switch case (ObjectType): {}", objectType);

          }
        } else {
          LOG.debug("node label is not a default label: {}", node.getLabel());
          LOG.debug("Object Type: {}", objectType);
          GraphNode blankNode = new GraphNode(vg.nextId());
          blankNode.setType(type);
          blankNode.setLabel("or");
          blankNode.setIri(THING_IRI);
          blankNode.setOptional(true);
          GraphRelation relSomeVal = new GraphRelation(vg.nextId());
          relSomeVal.setStart(node);
          relSomeVal.setEquivalentTo(equivalentTo);
          relSomeVal.setEnd(blankNode);
          relSomeVal.setOptional(true);
          relSomeVal.setEndNodeType(type);
          blankNode.setIncommingRelation(relSomeVal);
          if (node.getIncommingRelation() != null) {
            node.getIncommingRelation().setOptional(true);
          }
          vg.setRoot(blankNode);
          vg.addNode(blankNode);
          vg.addRelation(relSomeVal);

          for (OWLClassExpression classExpression : axiom.getOperandsAsList()) {
              
              addValue(returnedVal, vg.getRoot(), classExpression);
              
              /*
            ClassExpressionType cet = classExpression.getClassExpressionType();
            switch (cet) {
              case OWL_CLASS:
                LOG.debug("OWL_CLASS expression: {}", classExpression);
                OWLClassExpression expression = classExpression;
                String object = null;

                object = extractStringObject(expression, object);

                GraphNode endNode = new GraphNode(vg.nextId());
                endNode.setIri(object);
                endNode.setType(type);
                String label = labelExtractor.getLabelOrDefaultFragment(IRI.create(object));

                endNode.setLabel(label);

                GraphRelation rel = new GraphRelation(vg.nextId());
                rel.setIri(propertyIri);

                String relLabel = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
                rel.setLabel(relLabel);
                rel.setStart(blankNode);
                rel.setEnd(endNode);
                rel.setEndNodeType(type);
                rel.setOptional(true);
                endNode.setIncommingRelation(rel);
                if (blankNode.getIncommingRelation() != null) {
                  blankNode.getIncommingRelation().setOptional(true);
                }
                if (equivalentTo) {
                  rel.setOptional(false);
                }

                vg.addNode(endNode);
                vg.addRelation(rel);
                break;

              case OBJECT_SOME_VALUES_FROM:
              case OBJECT_EXACT_CARDINALITY:
              case OBJECT_MIN_CARDINALITY:
              case OBJECT_MAX_CARDINALITY:
              case DATA_MIN_CARDINALITY:
              case DATA_MAX_CARDINALITY:
              case OBJECT_ALL_VALUES_FROM:
                GraphNode add = null;
                if (relSomeVal.getLabel() != null && !relSomeVal.getLabel().trim().equals("")) {

                  GraphNode blankNode2 = new GraphNode(vg.nextId());
                  blankNode2.setType(type);
                  blankNode2.setLabel(DEFAULT_BLANK_NODE_LABEL);
                  blankNode2.setIri(THING_IRI);
                  GraphRelation relSomeVal2 = new GraphRelation(vg.nextId());
                  relSomeVal2.setIri(propertyIri);

                  String relLabel2 = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
                  relSomeVal2.setLabel(relLabel2);

                  relSomeVal2.setStart(blankNode);
                  relSomeVal2.setEnd(blankNode2);
                  relSomeVal2.setOptional(true);
                  relSomeVal2.setEndNodeType(type);
                  blankNode2.setIncommingRelation(relSomeVal2);
                  if (blankNode.getIncommingRelation() != null) {
                    blankNode.getIncommingRelation().setOptional(true);
                  }
                  vg.addNode(blankNode2);
                  vg.addRelation(relSomeVal2);
                  vg.setRoot(blankNode2);
                  add = blankNode2;
                } else {
                  add = blankNode;

                }

                addValue(returnedVal, vg.getRoot(), classExpression);
                break;
              case OBJECT_UNION_OF:

                GraphNode unionRootNode = blankNode;

                unionRootNode.setLabel(" or");

                LOG.debug("Object Union Of case before for: {}", axiom);
                for (OWLEntity owlEntity : classExpression.signature().collect(Collectors.toList())) {
                  LOG.debug("OWLObjectUnionOf axiom with owl entity {}", owlEntity);
                  GraphNode unionNode = new GraphNode(vg.nextId());
                  unionNode.setLabel(labelExtractor.getLabelOrDefaultFragment(owlEntity));
                  unionNode.setIri(owlEntity.getIRI().toString());
                  unionNode.setType(type);

                  GraphRelation unionRel = new GraphRelation(vg.nextId());
                  unionRel.setStart(unionRootNode);
                  unionRel.setEnd(unionNode);
                  unionRel.setEndNodeType(type);
                  unionNode.setIncommingRelation(unionRel);
                  if (node.getIncommingRelation() != null) {
                    unionRel.setOptional(unionRootNode.getIncommingRelation().isOptional());
                  }
                  vg.addNode(unionNode);
                  vg.addRelation(unionRel);
                }

                break;
              default:
                LOG.debug("Unsupported switch case (ObjectType): {}", objectType);
                LOG.debug("OWLExpression {}", classExpression);
                addValue(returnedVal, blankNode, classExpression);
                LOG.debug("Added value: {}", returnedVal);

            }
            
            
            
            
            
            */
          }
        }
        return returnedVal;
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectMaxCardinality axiom) {
        LOG.debug("visit OWLObjectMaxCardinality: {}", axiom.toString());
        int cardinality = axiom.getCardinality();
        boolean isOptional = cardinality == 1;

        String propertyIri = null;
        propertyIri = OwlDataExtractor.extractAxiomPropertyIri(axiom);
        ClassExpressionType objectType = axiom.getFiller().getClassExpressionType();

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

            endNode.setLabel(label);

            GraphRelation rel = new GraphRelation(vg.nextId());

            String labelPostfix = String.format(POSTFIX_FORMAT, "0", cardinality); //" (1..*)";
            rel.setIri(propertyIri);
            String relLabel = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
            rel.setLabel(relLabel + labelPostfix);
            rel.setStart(node);
            rel.setEnd(endNode);
            rel.setOptional(isOptional);
            rel.setEndNodeType(type);
            endNode.setIncommingRelation(rel);
            if (isOptional && node.getIncommingRelation() != null) {
              node.getIncommingRelation().setOptional(true);
            }
            vg.addNode(endNode);
            vg.addRelation(rel);
            break;

          case OBJECT_SOME_VALUES_FROM:
          case OBJECT_EXACT_CARDINALITY:
          case OBJECT_MIN_CARDINALITY:
          case OBJECT_MAX_CARDINALITY:
          case DATA_MIN_CARDINALITY:
          case DATA_MAX_CARDINALITY:
          case OBJECT_ALL_VALUES_FROM:
            GraphNode blankNode = new GraphNode(vg.nextId());
            blankNode.setType(type);
            blankNode.setLabel(DEFAULT_BLANK_NODE_LABEL);
            blankNode.setIri(THING_IRI);
            GraphRelation relSomeVal = new GraphRelation(vg.nextId());
            relSomeVal.setIri(propertyIri);

            String labelPostfix2 = String.format(POSTFIX_FORMAT, cardinality, "*");
            String relLabel2 = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
            relSomeVal.setLabel(relLabel2 + labelPostfix2);

            relSomeVal.setStart(node);
            relSomeVal.setEnd(blankNode);
            relSomeVal.setOptional(isOptional);
            relSomeVal.setEndNodeType(type);
            blankNode.setIncommingRelation(relSomeVal);
            if (isOptional && node.getIncommingRelation() != null) {
              node.getIncommingRelation().setOptional(true);
            }
            vg.addNode(blankNode);
            vg.addRelation(relSomeVal);
            vg.setRoot(blankNode);
            vg.setRoot(blankNode);
            addValue(returnedVal, blankNode, axiom.getFiller());
            break;

          default:
            LOG.debug("Unsupported switch case (ObjectType): {}", objectType);

        }
        return returnedVal;
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLDataMaxCardinality axiom) {
        LOG.debug("visit OWLDataMaxCardnality: {}", axiom.toString());
        int cardinality = axiom.getCardinality();
        boolean isOptional = cardinality == 1;

        String propertyIri = null;
        propertyIri = OwlDataExtractor.extractAxiomPropertyIri(axiom);
        DataRangeType objectType = axiom.getFiller().getDataRangeType();

        switch (objectType) {
          case DATATYPE:
            IRI datatypeIri = axiom.getFiller().signature().findFirst().get().getIRI();

            GraphNode endNode = new GraphNode(vg.nextId());
            endNode.setIri(datatypeIri.toString());
            endNode.setType(type);
            String label = labelExtractor.getLabelOrDefaultFragment(datatypeIri);

            endNode.setLabel(label);
            GraphRelation rel = new GraphRelation(vg.nextId());
            rel.setIri(propertyIri);

            String labelPostfix = String.format(POSTFIX_FORMAT, "0", cardinality);
            String relLabel = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
            rel.setLabel(relLabel + labelPostfix);

            rel.setStart(node);
            rel.setEnd(endNode);
            rel.setOptional(isOptional);
            rel.setEndNodeType(type);
            endNode.setIncommingRelation(rel);
            if (isOptional && node.getIncommingRelation() != null) {
              node.getIncommingRelation().setOptional(true);
            }
            vg.addNode(endNode);
            vg.addRelation(rel);

            return null;

          default:
            LOG.debug("Unsupported switch case (DataRangeType): {}", objectType);

        }
        return returnedVal;
      }

      @Override
      public Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLDataMinCardinality axiom) {
        LOG.debug("visit OWLDataMinCardinality: {}", axiom.toString());
        int cardinality = axiom.getCardinality();
        boolean isOptional = cardinality == 0;

        String propertyIri = null;
        propertyIri = OwlDataExtractor.extractAxiomPropertyIri(axiom);
        DataRangeType objectType = axiom.getFiller().getDataRangeType();

        switch (objectType) {
          case DATATYPE:
            IRI datatypeIri = axiom.getFiller().signature().findFirst().get().getIRI();

            GraphNode endNode = new GraphNode(vg.nextId());
            endNode.setIri(datatypeIri.toString());
            endNode.setType(type);
            String label = labelExtractor.getLabelOrDefaultFragment(datatypeIri);

            endNode.setLabel(label);

            GraphRelation rel = new GraphRelation(vg.nextId());
            rel.setIri(propertyIri);
            String labelPostfix = String.format(POSTFIX_FORMAT, cardinality, "*");
            String relLabel = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));

            rel.setLabel(relLabel + labelPostfix);
            rel.setStart(node);
            rel.setEnd(endNode);
            rel.setOptional(isOptional);
            rel.setEndNodeType(type);
            endNode.setIncommingRelation(rel);
            if (isOptional && node.getIncommingRelation() != null) {
              node.getIncommingRelation().setOptional(true);
            }
            vg.addNode(endNode);
            vg.addRelation(rel);

            return null;

          default:
            LOG.debug("Unsupported switch case (DataRangeType): {}", objectType);
        }

        return returnedVal;
      }

    };
  }

  private boolean couldAndRelationBeShorter(Set<OWLClassExpression> expressions) {
    boolean hasMin = Boolean.FALSE, hasMax = Boolean.FALSE;

    if (expressions == null || expressions.size() != 2) {
      return false;
    }
    for (OWLClassExpression expression : expressions) {
      ClassExpressionType type = expression.getClassExpressionType();
      LOG.debug("Check expression: {}, type: {}", expression, type);
      if (type == ClassExpressionType.OBJECT_MAX_CARDINALITY || type == ClassExpressionType.DATA_MAX_CARDINALITY) {
        hasMax = Boolean.TRUE;
      }
      if (type == ClassExpressionType.OBJECT_MIN_CARDINALITY || type == ClassExpressionType.DATA_MIN_CARDINALITY) {
        hasMin = Boolean.TRUE;
      }
    }
    return hasMin == Boolean.TRUE && hasMax == Boolean.TRUE;
  }

  public final OWLAxiomVisitorEx<GraphNode> individualCompleteGraphNode(OntologyGraph vg, GraphNode node, GraphNodeType type) {

    return new OWLAxiomVisitorEx<GraphNode>() {

      @Override
      public GraphNode visit(OWLObjectPropertyAssertionAxiom ax) {

        LOG.debug("sub {}", ax.getSubject().toStringID());
        LOG.debug("obj {}", ax.getObject().toStringID());
        LOG.debug("prop {}", ax.getProperty().toString());

        GraphNode endNode = new GraphNode(vg.nextId());
        endNode.setIri(ax.getObject().toStringID());
        endNode.setType(type);
        String label = labelExtractor.getLabelOrDefaultFragment(IRI.create(ax.getObject().toStringID()));
        endNode.setLabel(label);
        endNode.setType(type);

        String propertyIri = null;
        for (OWLEntity oWLEntity : ax.getProperty().signature().collect(Collectors.toList())) {
          propertyIri = oWLEntity.toStringID();
        }

        GraphRelation rel = new GraphRelation(vg.nextId());
        rel.setIri(propertyIri);
        rel.setLabel(labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri)));
        rel.setStart(node);
        rel.setEnd(endNode);
        rel.setEndNodeType(type);
        vg.addNode(endNode);
        vg.addRelation(rel);

        return endNode;
      }

      @Override
      public GraphNode visit(OWLDataPropertyAssertionAxiom ax) {

        GraphNode endNode = new GraphNode(vg.nextId());
        endNode.setIri("http://www.w3.org/2000/01/rdf-schema#Literal");
        endNode.setType(type);
        String label = ax.getObject().toString().replaceAll("\"", "").replaceAll("\\^\\^[\\w+|\\w+:\\w+]+", "");//labelExtractor.getLabelOrDefaultFragment(IRI.create(ax.getObject().toStringID()));
        endNode.setLabel(label);
        endNode.setType(type);

        String propertyIri = null;
        for (OWLEntity oWLEntity : ax.getProperty().signature().collect(Collectors.toList())) {
          propertyIri = oWLEntity.toStringID();
        }

        GraphRelation rel = new GraphRelation(vg.nextId());
        rel.setIri(propertyIri);
        rel.setLabel(labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri)));
        rel.setStart(node);
        rel.setEnd(endNode);
        rel.setEndNodeType(type);
        vg.addNode(endNode);
        vg.addRelation(rel);

        return endNode;
      }

      @Override
      public GraphNode visit(OWLClassAssertionAxiom ax) {
        for (OWLClass owlClass : ax.getClassExpression().classesInSignature().collect(Collectors.toList())) {
          String iri = owlClass.getIRI().toString();

          GraphNode endNode = new GraphNode(vg.nextId());
          endNode.setIri(iri);
          endNode.setType(type);

          String label = getPrepareLabel(labelExtractor, iri, Boolean.FALSE);
          endNode.setLabel(label);
          endNode.setType(type);

          GraphRelation rel = new GraphRelation(vg.nextId());
          rel.setIri("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
          rel.setLabel(labelExtractor.getLabelOrDefaultFragment(IRI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")));
          rel.setStart(node);
          rel.setEnd(endNode);
          rel.setEndNodeType(type);
          vg.addNode(endNode);
          vg.addRelation(rel);

          return endNode;
        }
        return null;
      }

    };
  }

  private final String extractStringObject(OWLClassExpression expression, String object) {
    for (OWLEntity oWLEntity : expression.signature().collect(Collectors.toList())) {
      LOG.debug("extract String Object: {}", oWLEntity);
      object = oWLEntity.toStringID();
    }
    return object;
  }

  private void addValue(Map<GraphNode, Set<ExpressionReturnedClass>> map, GraphNode node, OWLClassExpression expression) {
    addValueWithNotAndEquivalent(map, node, expression, Boolean.FALSE, Boolean.FALSE);
  }

  private void addValueWithNotAndEquivalent(Map<GraphNode, Set<ExpressionReturnedClass>> map, GraphNode node, OWLClassExpression expression, Boolean equivalent, Boolean not) {
    Set<ExpressionReturnedClass> values = map.getOrDefault(node, new HashSet<>());
    ExpressionReturnedClass expressionClass = new ExpressionReturnedClass();
    expressionClass.setOwlClassExpression(expression);
    expressionClass.setNot(not);
    expressionClass.setEquivalent(equivalent);
    values.add(expressionClass);
    map.put(node, values);

  }

  private String getPrepareLabel(LabelProvider labelProvider, String iri, Boolean not) {
    String label = labelProvider.getLabelOrDefaultFragment(IRI.create(iri));
    if (not.equals(true)) {
      label = "not " + label;
    }
    return label;
  }

}
