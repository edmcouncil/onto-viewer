package org.edmcouncil.spec.fibo.weasel.ontology.visitor.graph;

import java.util.stream.Collectors;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphNode;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphNodeType;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphRelation;
import org.edmcouncil.spec.fibo.weasel.model.graph.OntologyGraph;
import org.edmcouncil.spec.fibo.weasel.ontology.data.graph.GraphObjGenerator;
import org.edmcouncil.spec.fibo.weasel.ontology.data.label.provider.LabelProvider;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
