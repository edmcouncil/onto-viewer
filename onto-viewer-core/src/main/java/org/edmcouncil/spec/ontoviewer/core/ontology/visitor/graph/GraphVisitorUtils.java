package org.edmcouncil.spec.ontoviewer.core.ontology.visitor.graph;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNode;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.visitor.ExpressionReturnedClass;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;

public class GraphVisitorUtils {

    static String extractStringObject(OWLClassExpression expression, String object) {
        for (OWLEntity oWLEntity : expression.signature().collect(Collectors.toList())) {

            object = oWLEntity.toStringID();
        }
        return object;
    }

    static void addValue(Map<GraphNode, Set<ExpressionReturnedClass>> map, GraphNode node, OWLClassExpression expression) {
        addValueWithNotAndEquivalent(map, node, expression, Boolean.FALSE, Boolean.FALSE);
    }

    static void addValueWithNotAndEquivalent(Map<GraphNode, Set<ExpressionReturnedClass>> map, GraphNode node, OWLClassExpression expression, Boolean equivalent, Boolean not) {
        Set<ExpressionReturnedClass> values = map.getOrDefault(node, new HashSet<>());
        ExpressionReturnedClass expressionClass = new ExpressionReturnedClass();
        expressionClass.setOwlClassExpression(expression);
        expressionClass.setNot(not);
        expressionClass.setEquivalent(equivalent);
        values.add(expressionClass);
        map.put(node, values);
    }

    static String getPrepareLabel(LabelProvider labelProvider, String iri, Boolean not) {
        String label = labelProvider.getLabelOrDefaultFragment(IRI.create(iri));
        if (not.equals(true)) {
            label = "not " + label;
        }
        return label;
    }

    static boolean couldAndRelationBeShorter(Set<OWLClassExpression> expressions) {
        boolean hasMin = Boolean.FALSE, hasMax = Boolean.FALSE;

        if (expressions == null || expressions.size() != 2) {
            return false;
        }
        for (OWLClassExpression expression : expressions) {
            ClassExpressionType type = expression.getClassExpressionType();
            if (type == ClassExpressionType.OBJECT_MAX_CARDINALITY || type == ClassExpressionType.DATA_MAX_CARDINALITY) {
                hasMax = Boolean.TRUE;
            }
            if (type == ClassExpressionType.OBJECT_MIN_CARDINALITY || type == ClassExpressionType.DATA_MIN_CARDINALITY) {
                hasMin = Boolean.TRUE;
            }
        }
        return hasMin == Boolean.TRUE && hasMax == Boolean.TRUE;
    }
}
