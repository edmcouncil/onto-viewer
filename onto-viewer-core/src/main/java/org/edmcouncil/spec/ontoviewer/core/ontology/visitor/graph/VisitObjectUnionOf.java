package org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNode;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNodeType;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphRelation;
import org.edmcouncil.spec.ontoviewer.core.model.graph.OntologyGraph;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.graph.GraphObjGenerator;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.ExpressionReturnedClass;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VisitObjectUnionOf {

  private static final Logger LOG = LoggerFactory.getLogger(VisitObjectUnionOf.class);

  public static Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectUnionOf axiom, Map<GraphNode, Set<ExpressionReturnedClass>> returnedVal, OntologyGraph vg, GraphNode node, GraphNodeType type, Boolean equivalentTo, Boolean not, LabelProvider labelProvider) {
    GraphObjGenerator gog = new GraphObjGenerator(vg, labelProvider);
    LOG.debug("visit OWLObjectUnionOf: {}", axiom.toString());
    String propertyIri = "";
    LOG.debug("object type: {}", axiom.getClassExpressionType().toString());
    ClassExpressionType objectType = axiom.getClassExpressionType();

    if (node.getLabel().equals(gog.DEFAULT_BLANK_NODE_LABEL) || node.getLabel().equals("or")) {
      switch (objectType) {
        case OWL_CLASS:
          LOG.debug("OWL_CLASS: {}", axiom.getObjectComplementOf().toString());
          OWLClassExpression expression = axiom.getObjectComplementOf();
          String object = null;

          object = GraphVisitorUtils.extractStringObject(expression, object);

          GraphNode endNode = gog.createNode(object, type, not);

          GraphRelation rel = gog.createRelation(node, propertyIri, endNode, type, "");
          rel.setEquivalentTo(equivalentTo);
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

          GraphNode blankNode = gog.createBlankNode(type);
          GraphRelation relSomeVal = gog.createRelation(node, propertyIri, blankNode, type, "");
          relSomeVal.setIri(propertyIri);

          relSomeVal.setEquivalentTo(equivalentTo);
          relSomeVal.setOptional(true);
          vg.addNode(blankNode);
          vg.addRelation(relSomeVal);
          vg.setRoot(blankNode);

          GraphVisitorUtils.addValue(returnedVal, blankNode, axiom.getObjectComplementOf());
          break;
        case OBJECT_UNION_OF:

          GraphNode unionRootNode = node;
          unionRootNode.setLabel("or");

          LOG.debug("OWLObjectUnionOf axiom with owl entity {}", axiom.signature().collect(Collectors.toList()));
          for (OWLEntity owlEntity : axiom.signature().collect(Collectors.toList())) {
            LOG.debug("OWLObjectUnionOf axiom with owl entity {}", owlEntity);
            GraphNode unionNode = gog.createNode(owlEntity.getIRI().toString(), type, not);
            GraphRelation unionRel = gog.createBlankRelation(unionRootNode, unionNode, type, equivalentTo);
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
      GraphNode blankNode = gog.createOrBlankNode(type);
      blankNode.setOptional(true);
      GraphRelation relSomeVal = gog.createBlankRelation(node, blankNode, type, equivalentTo);
      relSomeVal.setOptional(true);
      vg.setRoot(blankNode);
      vg.addNode(blankNode);
      vg.addRelation(relSomeVal);

      for (OWLClassExpression classExpression : axiom.getOperandsAsList()) {

        ClassExpressionType expressionType = classExpression.getClassExpressionType();
        if (expressionType.equals(ClassExpressionType.OWL_CLASS)) {
          String object = null;
          object = GraphVisitorUtils.extractStringObject(classExpression, object);

          GraphNode endNode = gog.createNode(object, type, not);
          GraphRelation rel = gog.createRelation(blankNode, propertyIri, endNode, type, "");
          rel.setEquivalentTo(equivalentTo);
          rel.setOptional(true);
          vg.addNode(endNode);
          vg.addRelation(rel);
        } else {
          GraphVisitorUtils.addValue(returnedVal, vg.getRoot(), classExpression);
        }
      }
    }
    return returnedVal;
  }
}
