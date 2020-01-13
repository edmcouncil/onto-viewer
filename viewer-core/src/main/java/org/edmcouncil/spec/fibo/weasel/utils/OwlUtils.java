package org.edmcouncil.spec.fibo.weasel.utils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.OntologyVisitors;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class OwlUtils {

  @Autowired
  private OntologyVisitors ontologyVisitors;

  public final <T extends OWLAxiom> Boolean isRestriction(T axiom) {
    Boolean isRestriction = axiom.accept(ontologyVisitors.isRestrictionVisitor);
    if (isRestriction == null) {
      isRestriction = Boolean.FALSE;
    }
    return isRestriction;
  }

  public Set<OWLClass> getSuperClasses(OWLClass clazz, OWLOntology ontology) {
    Set<OWLClass> result = new HashSet<>();

    ontology.subClassAxiomsForSubClass(clazz)
        .collect(Collectors.toSet())
        .stream()
        .filter((axiom) -> (axiom.getSubClass().getClassExpressionType() == ClassExpressionType.OWL_CLASS
        && axiom.getSuperClass().getClassExpressionType() == ClassExpressionType.OWL_CLASS)).forEachOrdered((axiom) -> {
      OWLClass owlClass = axiom.getSuperClass().asOWLClass();
      if (axiom.getSubClass().equals(clazz)) {
        result.add(owlClass);
        result.addAll(getSuperClasses(owlClass, ontology));
      }
    });

    return result;
  }

}
