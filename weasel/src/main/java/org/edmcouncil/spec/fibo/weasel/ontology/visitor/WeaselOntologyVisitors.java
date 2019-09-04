package org.edmcouncil.spec.fibo.weasel.ontology.visitor;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class WeaselOntologyVisitors {

  public static OWLObjectVisitorEx<Boolean> isRestrictionVisitor
      = new OWLObjectVisitorEx<Boolean>() {
    @Override
    public Boolean visit(OWLSubClassOfAxiom subClassAxiom) {
      OWLClassExpression superClass = subClassAxiom.getSuperClass();
      ClassExpressionType classExpressionType = superClass.getClassExpressionType();
      return !classExpressionType.equals(ClassExpressionType.OWL_CLASS);
    }
  };

}
