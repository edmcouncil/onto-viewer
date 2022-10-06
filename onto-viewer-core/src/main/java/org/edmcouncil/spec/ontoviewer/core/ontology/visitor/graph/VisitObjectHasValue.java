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
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisitObjectHasValue {

  private static final Logger LOG = LoggerFactory.getLogger(VisitObjectHasValue.class);

  public static Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLObjectHasValue axiom, Map<GraphNode, Set<ExpressionReturnedClass>> returnedVal, OntologyGraph ontologyGraph, GraphNode node, GraphNodeType type, Boolean equivalentTo, Boolean not, LabelProvider labelProvider) {

    LOG.debug("visit OWLObjectHasValue");

    GraphObjGenerator graphObjGenerator = new GraphObjGenerator(ontologyGraph, labelProvider);
    String propertyIri = OwlDataExtractor.extractAxiomPropertyIri(axiom);
    IRI datatypeIri = axiom.getFiller().signature().findFirst().get().getIRI();
    GraphNode endNode = graphObjGenerator.createNode(datatypeIri.toString(), type, not);
    GraphRelation graphRelation = graphObjGenerator.createRelation(node, propertyIri, endNode, type, "");
    graphRelation.setIri(propertyIri);
    ontologyGraph.addNode(endNode);
    ontologyGraph.addRelation(graphRelation);
    return null;
  }
}
