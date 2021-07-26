package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class FiboMaturityLevelFactory {
    
    public static FiboMaturityLevel prod = new AppFiboMaturityLevel("prod"); 
    public static FiboMaturityLevel dev = new AppFiboMaturityLevel("dev"); 
    public static FiboMaturityLevel prodDev = new AppFiboMaturityLevel("prodDev"); 

  public static OntoFiboMaturityLevel create(String label, IRI iri) {
    return create(label, iri.toString());
  }

  public static OntoFiboMaturityLevel create(String label, String iri) {
    return new OntoFiboMaturityLevel(label, iri);
  }

  public static OntoFiboMaturityLevel empty() {
    return create("", "");
  }
  public static FiboMaturityLevel emptyAppFiboMaturityLabel() {
    return  new AppFiboMaturityLevel("");
  }
}
