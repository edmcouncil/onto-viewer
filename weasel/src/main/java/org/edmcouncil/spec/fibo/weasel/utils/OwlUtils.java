package org.edmcouncil.spec.fibo.weasel.utils;

import org.edmcouncil.spec.fibo.weasel.ontology.visitor.WeaselOntologyVisitors;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OwlUtils {

  public static <T extends OWLAxiom> Boolean isRestriction(T axiom) {
    Boolean isRestriction = axiom.accept(WeaselOntologyVisitors.isRestrictionVisitor);
    if (isRestriction == null) {
      isRestriction = Boolean.FALSE;
    }
    return isRestriction;
  }

}
