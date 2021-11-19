package org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph;

import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNode;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNodeType;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphRelation;
import org.edmcouncil.spec.ontoviewer.core.model.graph.OntologyGraph;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.graph.GraphObjGenerator;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VisitClassAssertionAxiom {
    private static final Logger LOG = LoggerFactory.getLogger(VisitClassAssertionAxiom.class);
    

    public static GraphNode visit(OWLClassAssertionAxiom axiom, OntologyGraph vg, GraphNode node, GraphNodeType type, LabelProvider labelProvider) {
        GraphObjGenerator gog = new GraphObjGenerator(vg, labelProvider);

        for (OWLClass owlClass : axiom.getClassExpression().classesInSignature().collect(Collectors.toList())) {
            String iri = owlClass.getIRI().toString();
            GraphNode endNode = gog.createNode(iri, type, false);
            GraphRelation rel = gog.createRelation(node, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", endNode, type, "");
            vg.addNode(endNode);
            vg.addRelation(rel);
            return endNode;
        }
        return null;

    }
}
