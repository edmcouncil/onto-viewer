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
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VisitIntersectionOf {

    private static final Logger LOG = LoggerFactory.getLogger(VisitIntersectionOf.class);
    private static final String RELATION_LABEL_POSTFIX = " (1..*)";
    

    public static Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectIntersectionOf axiom, Map<GraphNode, Set<ExpressionReturnedClass>> returnedVal, OntologyGraph vg, GraphNode node, GraphNodeType type, Boolean equivalentTo, Boolean not, LabelProvider labelProvider) {
        GraphObjGenerator gog = new GraphObjGenerator(vg, labelProvider);

       LOG.debug("visit OWLObjectIntersectionOf");
        Set<OWLClassExpression> axiomConjunct = axiom.conjunctSet().collect(Collectors.toSet());

        if (GraphVisitorUtils.couldAndRelationBeShorter(axiomConjunct)) {
          LOG.debug("`and` Relation Can Be Shorter!");
          GraphVisitorUtils.addValue(returnedVal, node, axiomConjunct.stream().findAny().orElse(null));
          return returnedVal;
        }
        GraphNode blankNode = null;
        if(node.getLabel().equals(gog.DEFAULT_BLANK_NODE_LABEL)) blankNode = node;
        else blankNode = gog.createBlankNode(type);
            
        blankNode.setLabel("and");
        
        GraphRelation relSomeVal = new GraphRelation(vg.nextId());

        if(blankNode.getId()!=node.getId()){
            relSomeVal = gog.createBlankRelation(node, blankNode, type, equivalentTo);
            vg.addNode(blankNode);
            vg.addRelation(relSomeVal);
            vg.setRoot(blankNode);
    }
        for (OWLClassExpression owlClassExpression : axiomConjunct) {
          LOG.debug("getClassExpressionType {}", owlClassExpression.getClassExpressionType());

          ClassExpressionType objectType = owlClassExpression.getClassExpressionType();
          switch (objectType) {
            case OWL_CLASS:
              String iri = null;
              iri = GraphVisitorUtils.extractStringObject(owlClassExpression, iri);
                GraphNode endNode = gog.createNode(iri, type, not);
                GraphRelation rel = gog.createBlankRelation(blankNode, endNode, type, equivalentTo); 
              vg.addNode(endNode);
              vg.addRelation(rel);
              break;

            default:
              LOG.debug("Object type {}, Expression {}", objectType, owlClassExpression);
              GraphVisitorUtils.addValue(returnedVal, blankNode, owlClassExpression);

          }
        }
        return returnedVal;
        
        
        
        
        
        
        
        
        
        
        

    }
}
