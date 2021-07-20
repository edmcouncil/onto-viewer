package org.edmcouncil.spec.fibo.weasel.ontology.visitor;

import org.edmcouncil.spec.fibo.weasel.ontology.visitor.graph.VisitClassAssertionAxiom;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.graph.VisitDataExactCardinality;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.graph.VisitDataHasValue;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.graph.VisitDataMaxCardinality;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.graph.VisitDataMinCardinality;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.graph.VisitDataPropertyAssertionAxiom;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.graph.VisitDataSomeValuesFrom;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.graph.VisitEquivalentClasses;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.graph.VisitIntersectionOf;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.graph.VisitObjectAllValuesFrom;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.graph.VisitObjectComplementOf;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.graph.VisitObjectExaclyCardinality;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.graph.VisitObjectMaxCardinality;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.graph.VisitObjectMinCardinality;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.graph.VisitObjectPropertyAssertionAxiom;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.graph.VisitObjectUnionOf;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.graph.VisitSomeValuesFrom;

public class Visitors {

    VisitSomeValuesFrom someValuesFrom;
    VisitObjectComplementOf objectComplementOf;
    VisitDataHasValue dataHasValue;
    VisitIntersectionOf intersectionOf;
    VisitEquivalentClasses equivalentClasses;
    VisitObjectExaclyCardinality objectExaclyCardinality;
    VisitObjectAllValuesFrom objectAllValuesFrom;
    VisitDataSomeValuesFrom dataSomeValuesFrom;
    VisitDataExactCardinality dataExactCardinality;
    VisitObjectMinCardinality objectMinCardinality;
    VisitObjectUnionOf objectUnionOf;
    VisitObjectMaxCardinality objectMaxCardinality;
    VisitDataMaxCardinality dataMaxCardinality;
    VisitDataMinCardinality dataMinCardinality;
    VisitObjectPropertyAssertionAxiom objectPropertyAssertionAxiom;
    VisitDataPropertyAssertionAxiom dataPropertyAssertionAxiom;
    VisitClassAssertionAxiom classAssertionAxiom;

}
