package org.edmcouncil.spec.fibo.weasel.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.edmcouncil.spec.fibo.weasel.ontology.data.OwlDataHandler;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.OntologyVisitors;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class OwlUtils {

  private static final Logger LOG = LoggerFactory.getLogger(OwlDataHandler.class);

  @Autowired
  private OntologyVisitors ontologyVisitors;

  public final <T extends OWLAxiom> Boolean isRestriction(T axiom) {
    Boolean isRestriction = axiom.accept(ontologyVisitors.isRestrictionVisitor);
    if (isRestriction == null) {
      isRestriction = Boolean.FALSE;
    }
    return isRestriction;
  }

  /**
   * The method collects all subclasses of the given class, excluding direct classes.
   *
   * @param clazz Clazz are all SubClasses of given class.
   * @param ontology This is a loaded ontology.
   * @return All subclasses;
   */
  public Set<OWLClass> getSuperClasses(OWLClass clazz, OWLOntology ontology) {
    Set<OWLClass> result = new HashSet<>();
    List<OWLClassExpression> subClasses = EntitySearcher.getSuperClasses(clazz, ontology).collect(Collectors.toList());
    for (OWLClassExpression subClass : subClasses) {
      LOG.debug("getSuperClasses -> subClass {}", subClass);
      Optional<OWLEntity> e = subClass.signature().findFirst();
      LOG.debug("\tgetSuperClasses -> enity iri {}", e.get().getIRI());
      if (subClass.getClassExpressionType() == ClassExpressionType.OWL_CLASS) {
        result.add(e.get().asOWLClass());
        result.addAll(getSuperClasses(e.get().asOWLClass(), ontology));
      }

    }
    return result;

  }

}
