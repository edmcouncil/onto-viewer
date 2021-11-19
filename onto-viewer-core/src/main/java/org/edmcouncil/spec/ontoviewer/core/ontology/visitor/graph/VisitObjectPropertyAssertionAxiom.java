package org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph;

import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNode;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNodeType;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphRelation;
import org.edmcouncil.spec.ontoviewer.core.model.graph.OntologyGraph;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.graph.GraphObjGenerator;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VisitObjectPropertyAssertionAxiom {

    private static final Logger LOG = LoggerFactory.getLogger(VisitObjectPropertyAssertionAxiom.class);

    public static GraphNode visit(OWLObjectPropertyAssertionAxiom axiom, OntologyGraph vg, GraphNode node, GraphNodeType type, LabelProvider labelProvider) {
        GraphObjGenerator gog = new GraphObjGenerator(vg, labelProvider);

        LOG.debug("sub {}", axiom.getSubject().toStringID());
        LOG.debug("obj {}", axiom.getObject().toStringID());
        LOG.debug("prop {}", axiom.getProperty().toString());

        GraphNode endNode = gog.createNode(axiom.getObject().toStringID(), type, false);
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
