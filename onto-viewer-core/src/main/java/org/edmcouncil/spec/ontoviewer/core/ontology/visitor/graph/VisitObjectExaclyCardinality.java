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
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VisitObjectExaclyCardinality {

    private static final Logger LOG = LoggerFactory.getLogger(VisitObjectExaclyCardinality.class);

    public static Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectExactCardinality axiom, Map<GraphNode, Set<ExpressionReturnedClass>> returnedVal, OntologyGraph vg, GraphNode node, GraphNodeType type, Boolean equivalentTo, Boolean not, LabelProvider labelProvider) {
        GraphObjGenerator gog = new GraphObjGenerator(vg, labelProvider);
       int cardinality = axiom.getCardinality();
        LOG.debug("visit OWLObjectExactCardinality: {}", axiom.toString());
        String propertyIri = OwlDataExtractor.extractAxiomPropertyIri(axiom);
        ClassExpressionType objectType = axiom.getFiller().getClassExpressionType();

        switch (objectType) {
          case OWL_CLASS:
            OWLClassExpression expression = axiom.getFiller().getObjectComplementOf();
            String iri = null;
            iri = GraphVisitorUtils.extractStringObject(expression, iri);
            
            GraphNode endNode = gog.createNode(iri, type, not);
                    
            GraphRelation rel = gog.createCardinalityRelation(node, propertyIri, endNode, type, cardinality, cardinality);
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
            GraphNode blankNode = gog.createBlankNode(type);
            String labelPostfix = " (1..1)";
            GraphRelation relSomeVal = gog.createRelation(node, propertyIri, blankNode, type, labelPostfix);
            relSomeVal.setIri(propertyIri);
            vg.addNode(blankNode);
            vg.addRelation(relSomeVal);
            vg.setRoot(blankNode);

            GraphVisitorUtils.addValue(returnedVal, blankNode, axiom.getFiller());
            break;

          default:
            LOG.debug("Unsupported switch case (ObjectType): " + objectType);

        }
        return returnedVal;

    }
}
