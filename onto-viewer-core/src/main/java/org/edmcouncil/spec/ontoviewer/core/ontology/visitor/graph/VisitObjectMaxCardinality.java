package org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph;

import java.util.Map;
import java.util.Set;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNode;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNodeType;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphRelation;
import org.edmcouncil.spec.ontoviewer.core.model.graph.OntologyGraph;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.extractor.OwlDataExtractor;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.graph.GraphObjGenerator;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.ExpressionReturnedClass;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VisitObjectMaxCardinality {

    private static final Logger LOG = LoggerFactory.getLogger(VisitObjectMaxCardinality.class);

    public static Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectMaxCardinality axiom, Map<GraphNode, Set<ExpressionReturnedClass>> returnedVal, OntologyGraph vg, GraphNode node, GraphNodeType type, Boolean equivalentTo, Boolean not, LabelProvider labelProvider) {
        GraphObjGenerator gog = new GraphObjGenerator(vg, labelProvider);

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
                object = GraphVisitorUtils.extractStringObject(expression, object);

                GraphNode endNode = gog.createNode(object, type, not);
                if (cardinality == 0) {
                    endNode.setOptional(true);
                }
                GraphRelation rel = gog.createCardinalityRelation(node, propertyIri, endNode, type, "0", String.valueOf(cardinality));
                rel.setOptional(isOptional);
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
                GraphRelation relSomeVal = gog.createCardinalityRelation(node, propertyIri, blankNode, type, String.valueOf(cardinality), "*");
                relSomeVal.setOptional(isOptional);
                vg.addNode(blankNode);
                vg.addRelation(relSomeVal);
                vg.setRoot(blankNode);
                GraphVisitorUtils.addValue(returnedVal, blankNode, axiom.getFiller());
                break;

            default:
                LOG.debug("Unsupported switch case (ObjectType): {}", objectType);

        }
        return returnedVal;

    }
}
