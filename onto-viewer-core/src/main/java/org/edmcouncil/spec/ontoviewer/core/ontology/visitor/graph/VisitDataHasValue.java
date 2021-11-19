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
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VisitDataHasValue {

    private static final Logger LOG = LoggerFactory.getLogger(VisitDataHasValue.class);
    private static final String RELATION_LABEL_POSTFIX = " (1..*)";

    public static Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLDataHasValue axiom, Map<GraphNode, Set<ExpressionReturnedClass>> returnedVal, OntologyGraph vg, GraphNode node, GraphNodeType type, Boolean equivalentTo, Boolean not, LabelProvider labelProvider) {

        LOG.debug("visit OWLDataHasValue");

        String xsdBooleanIri = "http://www.w3.org/2001/XMLSchema#boolean";
        String val = axiom.getFiller().getLiteral();
        GraphObjGenerator gog = new GraphObjGenerator(vg, labelProvider);

        GraphNode endNode = gog.createNode(xsdBooleanIri, type, not);
        endNode.setLabel(val);

        String propertyIri = OwlDataExtractor.extractAxiomPropertyIri(axiom);

        GraphRelation rel = gog.createRelation(node, propertyIri, endNode, type, "");

        vg.addNode(endNode);
        vg.addRelation(rel);

        return null;

    }
}
