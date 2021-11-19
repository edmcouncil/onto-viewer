package org.edmcouncil.spec.ontoviewer.core.ontology.data.graph;

import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNode;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNodeType;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphRelation;
import org.edmcouncil.spec.ontoviewer.core.model.graph.OntologyGraph;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphObjGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(GraphObjGenerator.class);
    private static final String THING_IRI = "http://www.w3.org/2002/07/owl#Thing";
    public static final String DEFAULT_BLANK_NODE_LABEL = "Thing";
    private static final String POSTFIX_FORMAT = " (%s..%s)";
    
    private LabelProvider labelExtractor;

    private OntologyGraph vg;

    public GraphObjGenerator(OntologyGraph vg, LabelProvider labelProvider) {
        this.vg = vg;
        this.labelExtractor = labelProvider;
    }

    public GraphNode createNode(String iri, GraphNodeType type, boolean not) {
        GraphNode node = new GraphNode(vg.nextId());
        node.setIri(iri);
        String label = getPrepareLabel(iri, not);
        node.setLabel(label);
        node.setType(type);
        return node;
    }

    public GraphNode createBlankNode(GraphNodeType type) {
        GraphNode node = new GraphNode(vg.nextId());
        node.setIri(THING_IRI);
        node.setLabel(DEFAULT_BLANK_NODE_LABEL);
        node.setType(type);
        return node;
    }

    public GraphNode createOrBlankNode(GraphNodeType type) {
        GraphNode node = new GraphNode(vg.nextId());
        node.setIri(THING_IRI);
        node.setLabel("or");
        node.setType(type);
        return node;
    }

    public GraphRelation createRelation(GraphNode startNode, String propertyIri, GraphNode endNode, GraphNodeType type, String labelPostfix) {
        GraphRelation rel = new GraphRelation(vg.nextId());
        rel.setIri(propertyIri);

        String relLabel = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
        rel.setLabel(relLabel + labelPostfix);
        rel.setStart(startNode);
        rel.setEnd(endNode);
        endNode.setIncommingRelation(rel);
        if (startNode.getIncommingRelation() != null) {
            rel.setOptional(startNode.getIncommingRelation().isOptional());
        } else {
            rel.setOptional(false);
        }
        rel.setEndNodeType(type);
        return rel;
    }

    public GraphRelation createCardinalityRelation(GraphNode startNode, String propertyIri, GraphNode endNode, GraphNodeType type, int cardinalityLeft, int cardinalityRight) {
        return createCardinalityRelation(startNode, propertyIri, endNode, type, String.valueOf(cardinalityLeft), String.valueOf(cardinalityRight));
    }

    public GraphRelation createCardinalityRelation(GraphNode startNode, String propertyIri, GraphNode endNode, GraphNodeType type, String cardinalityLeft, String cardinalityRight) {
        GraphRelation rel = new GraphRelation(vg.nextId());
        rel.setIri(propertyIri);

        String relLabel = labelExtractor.getLabelOrDefaultFragment(IRI.create(propertyIri));
        String labelPostfix = String.format(POSTFIX_FORMAT, cardinalityLeft, cardinalityRight);//" (1..1)";
        rel.setLabel(relLabel + labelPostfix);
        rel.setStart(startNode);
        rel.setEnd(endNode);
        endNode.setIncommingRelation(rel);
        if (startNode.getIncommingRelation() != null) {
            rel.setOptional(startNode.getIncommingRelation().isOptional());
        } else {
            rel.setOptional(false);
        }
        rel.setEndNodeType(type);
        return rel;
    }

    public GraphRelation createBlankRelation(GraphNode startNode, GraphNode endNode, GraphNodeType type, boolean equivalent) {
        String propertyIri = THING_IRI;
        GraphRelation rel = new GraphRelation(vg.nextId());
        rel.setIri(propertyIri);

        rel.setStart(startNode);
        rel.setEnd(endNode);
        endNode.setIncommingRelation(rel);
        if (startNode.getIncommingRelation() != null) {
            rel.setOptional(startNode.getIncommingRelation().isOptional());
        } else {
            rel.setOptional(false);
        }
        rel.setEndNodeType(type);
        rel.setEquivalentTo(equivalent);
        return rel;
    }

    private String getPrepareLabel(String iri, Boolean not) {
        if(iri==null || iri.trim().equals("")) return "";
        String label = labelExtractor.getLabelOrDefaultFragment(IRI.create(iri));
        if (not.equals(true)) {
            label = "not " + label;
        }
        return label;
    }

}
