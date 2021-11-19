package org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph;

import java.util.Map;
import java.util.Set;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNode;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNodeType;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphRelation;
import org.edmcouncil.spec.ontoviewer.core.model.graph.OntologyGraph;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.graph.GraphObjGenerator;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.ExpressionReturnedClass;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VisitObjectComplementOf {

    private static final Logger LOG = LoggerFactory.getLogger(VisitObjectComplementOf.class);
    private static final String RELATION_LABEL_POSTFIX = " (1..*)";

    public static Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectComplementOf axiom, Map<GraphNode, Set<ExpressionReturnedClass>> returnedVal, OntologyGraph vg, GraphNode node, GraphNodeType type, Boolean equivalentTo, Boolean not, LabelProvider labelProvider) {

        LOG.debug("visit OWLObjectComplementOf: {}", axiom.toString());
        String propertyIri = ""; //?

        LOG.debug("Class Expresion OOCO: {}", axiom.getClassExpressionType().toString());

        ClassExpressionType objectType = axiom.getClassExpressionType();
        OWLClassExpression expression = axiom.getNNF();
        objectType = expression.getClassExpressionType();
        GraphObjGenerator gog = new GraphObjGenerator(vg, labelProvider);
        LOG.debug("Expresion:  {}", axiom.getNNF().toString());
        switch (objectType) {
          case OWL_CLASS:
            String iri = null;
            iri = GraphVisitorUtils.extractStringObject(expression, iri);
            GraphNode endNode = gog.createNode(iri, type, not);
            GraphRelation rel = gog.createRelation(node ,propertyIri, endNode, type, RELATION_LABEL_POSTFIX);
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
            GraphVisitorUtils.addValueWithNotAndEquivalent(returnedVal, node, axiom.getOperand(), false, true);
            return returnedVal;
          default:
            LOG.debug("Unsupported expression type {}", objectType);
            break;
        }
        return returnedVal;
    }
}