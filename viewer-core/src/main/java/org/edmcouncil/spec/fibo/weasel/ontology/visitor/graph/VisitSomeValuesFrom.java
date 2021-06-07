package org.edmcouncil.spec.fibo.weasel.ontology.visitor.graph;

import java.util.Map;
import java.util.Set;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphNode;
import org.edmcouncil.spec.fibo.weasel.ontology.data.extractor.OwlDataExtractor;
import org.edmcouncil.spec.fibo.weasel.ontology.data.graph.GraphObjGenerator;
import org.edmcouncil.spec.fibo.weasel.ontology.data.label.provider.LabelProvider;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.ExpressionReturnedClass;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VisitSomeValuesFrom {

    private static final Logger LOG = LoggerFactory.getLogger(VisitSomeValuesFrom.class);
    @Autowired
    private LabelProvider labelExtractor;

    public static void visit(OWLObjectSomeValuesFrom someValuesFromAxiom, Map<GraphNode, Set<ExpressionReturnedClass>> returnedVal) {

        LOG.debug("visit OWLObjectSomeValuesFrom: {}", someValuesFromAxiom.toString());
        String propertyIri = null;
        propertyIri = OwlDataExtractor.extractAxiomPropertyIri(someValuesFromAxiom);
        ClassExpressionType objectType = someValuesFromAxiom.getFiller().getClassExpressionType();

    }

}
