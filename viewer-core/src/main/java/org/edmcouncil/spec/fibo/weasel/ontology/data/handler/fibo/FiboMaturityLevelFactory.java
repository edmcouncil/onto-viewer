package org.edmcouncil.spec.fibo.weasel.ontology.data.handler.fibo;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class FiboMaturityLevelFactory {

  public static FiboMaturityLevel create(String label, IRI iri) {
    return create(label, iri.toString());
  }

  public static FiboMaturityLevel create(String label, String iri) {
    return new FiboMaturityLevel(label, iri);
  }

}
