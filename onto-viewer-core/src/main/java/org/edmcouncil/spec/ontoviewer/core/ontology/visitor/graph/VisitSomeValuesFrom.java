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
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VisitSomeValuesFrom {

    private static final Logger LOG = LoggerFactory.getLogger(VisitSomeValuesFrom.class);
    private static final String RELATION_LABEL_POSTFIX = " (1..*)";

    public static Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectSomeValuesFrom someValuesFromAxiom, Map<GraphNode, Set<ExpressionReturnedClass>> returnedVal, OntologyGraph vg, GraphNode node, GraphNodeType type, Boolean equivalentTo, Boolean not, LabelProvider labelProvider) {

        LOG.debug("visit OWLObjectSomeValuesFrom: {}", someValuesFromAxiom.toString());
        String propertyIri = null;
        propertyIri = OwlDataExtractor.extractAxiomPropertyIri(someValuesFromAxiom);
        ClassExpressionType objectType = someValuesFromAxiom.getFiller().getClassExpressionType();
            
        GraphObjGenerator gog = new GraphObjGenerator(vg, labelProvider);
        
        switch (objectType) {
          case OWL_CLASS:
            OWLClassExpression expression = someValuesFromAxiom.getFiller().getObjectComplementOf();
            String iri = null;
            iri = GraphVisitorUtils.extractStringObject(expression, iri);
            GraphNode endNode = gog.createNode(iri, type, not);
            GraphRelation rel = gog.createRelation(node ,propertyIri, endNode, type, RELATION_LABEL_POSTFIX);
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
              
            GraphNode blankNode = gog.createBlankNode(type);
            GraphRelation relSomeVal = gog.createRelation(node, propertyIri, blankNode, type, RELATION_LABEL_POSTFIX);

            relSomeVal.setOptional(false);
            vg.addNode(blankNode);
            vg.addRelation(relSomeVal);
            vg.setRoot(blankNode);
            LOG.debug("{}->{}", objectType.toString(), someValuesFromAxiom.toString());
            GraphVisitorUtils.addValue(returnedVal, blankNode, someValuesFromAxiom.getFiller());
            return returnedVal;
          default:
            LOG.debug("Unsupported expression type in somevaluesfrom {}", objectType);
            GraphVisitorUtils.addValue(returnedVal, node, someValuesFromAxiom.getFiller());
            return returnedVal;
        }
    }

}