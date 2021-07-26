package org.edmcouncil.spec.ontoviewer.core.ontology.visitor;

import org.semanticweb.owlapi.model.OWLClassExpression;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class ExpressionReturnedClass {

  private OWLClassExpression owlClassExpression;
  private Boolean not;
  private Boolean equivalent = false;

  public OWLClassExpression getOwlClassExpression() {
    return owlClassExpression;
  }

  public void setOwlClassExpression(OWLClassExpression owlClassExpression) {
    this.owlClassExpression = owlClassExpression;
  }

  public Boolean getNot() {
    return not;
  }

  public void setNot(Boolean not) {
    this.not = not;
  }

  public Boolean getEquivalent() {
    return equivalent;
  }

  public void setEquivalent(Boolean equivalent) {
    this.equivalent = equivalent;
  }

  @Override
  public String toString() {
    return "ExpressionReturnedClass{" + "owlClassExpression=" + owlClassExpression + ", not=" + not + ", equivalent=" + equivalent + '}';
  }

}
