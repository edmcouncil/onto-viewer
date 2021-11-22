package org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNode;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNodeType;
import org.edmcouncil.spec.ontoviewer.core.model.graph.OntologyGraph;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.graph.GraphObjGenerator;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.ExpressionReturnedClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class VisitEquivalentClasses {

    private static final Logger LOG = LoggerFactory.getLogger(VisitEquivalentClasses.class);

    public static Map<GraphNode, Set<ExpressionReturnedClass>> visit(OWLEquivalentClassesAxiom axiom, Map<GraphNode, Set<ExpressionReturnedClass>> returnedVal, OntologyGraph vg, GraphNode node, GraphNodeType type, Boolean equivalentTo, Boolean not, LabelProvider labelProvider) {
        GraphObjGenerator gog = new GraphObjGenerator(vg, labelProvider);
        LOG.debug("visit OWLEquivalentClassesAxiom");
        Set<OWLClassExpression> set = axiom.classExpressions().collect(Collectors.toSet());
        for (OWLClassExpression owlClassExpression : set) {
            LOG.debug("Visitor owlClassExpression {}", owlClassExpression.toString());
            Set<OWLEntity> classExprssionSignature = owlClassExpression.signature().collect(Collectors.toSet());
            boolean isTheSameIri = false;
            for (OWLEntity owlEntity : classExprssionSignature) {
                LOG.debug("Class Expression signature {}", owlEntity.toStringID());
                if (node.getIri().equals(owlEntity.toStringID())) {
                    isTheSameIri = true;
                }
            }
            if (classExprssionSignature.size() == 1 && isTheSameIri) {
                continue;
            }
            GraphVisitorUtils.addValueWithNotAndEquivalent(returnedVal, node, owlClassExpression, Boolean.TRUE, Boolean.FALSE);
        }

        return returnedVal;
    }
}
