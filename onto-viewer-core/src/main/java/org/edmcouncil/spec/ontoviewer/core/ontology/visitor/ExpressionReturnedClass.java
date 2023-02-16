package org.edmcouncil.spec.ontoviewer.core.ontology.visitor;

import java.util.stream.Collectors;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ExpressionReturnedClass that = (ExpressionReturnedClass) o;

    if(!owlClassExpression.getClassExpressionType().equals(that.owlClassExpression.getClassExpressionType())
            && !owlClassExpression.signature()
                    .collect(Collectors.toSet()).equals(that.owlClassExpression.signature().collect(Collectors.toSet())))
    {
      return false;
    }
    
    if (!not.equals(that.not)) {
      return false;
    }
    return equivalent.equals(that.equivalent);
  }

  @Override
  public int hashCode() {
    int result = owlClassExpression.signature().collect(Collectors.toSet()).hashCode();
    result = 31 * result + not.hashCode();
    result = 31 * result + equivalent.hashCode();
    return result;
  }
}
