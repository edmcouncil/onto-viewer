package org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph;

import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNode;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNodeType;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphRelation;
import org.edmcouncil.spec.ontoviewer.core.model.graph.OntologyGraph;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.graph.GraphObjGenerator;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VisitDataPropertyAssertionAxiom {

    private static final Logger LOG = LoggerFactory.getLogger(VisitDataPropertyAssertionAxiom.class);

    public static GraphNode visit(OWLDataPropertyAssertionAxiom axiom, OntologyGraph vg, GraphNode node, GraphNodeType type, LabelProvider labelProvider) {
        GraphObjGenerator gog = new GraphObjGenerator(vg, labelProvider);

        GraphNode endNode = gog.createNode("http://www.w3.org/2000/01/rdf-schema#Literal", type, false);
        String label = axiom.getObject().toString().replaceAll("\"", "").replaceAll("\\^\\^[\\w+|\\w+:\\w+]+", "");
        endNode.setLabel(label);
        String propertyIri = null;
        for (OWLEntity oWLEntity : axiom.getProperty().signature().collect(Collectors.toList())) {
            propertyIri = oWLEntity.toStringID();
        }
        GraphRelation rel = gog.createRelation(node, propertyIri, endNode, type, "");
        vg.addNode(endNode);
        vg.addRelation(rel);

        return endNode;

    }
}
