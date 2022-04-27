package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class MaturityLevelFactory {

  public static MaturityLevel prod = new AppMaturityLevel("prod");
  public static MaturityLevel dev = new AppMaturityLevel("dev");
  public static MaturityLevel prodDev = new AppMaturityLevel("prodDev");
  public static MaturityLevel INFO = new AppMaturityLevel("info");

  public static OntoMaturityLevel create(String label, IRI iri, String icon) {
    return create(label, iri.toString(), icon);
  }

  public static OntoMaturityLevel create(String label, String iri, String icon) {
    return new OntoMaturityLevel(label, iri, icon);
  }

  public static OntoMaturityLevel empty() {
    return create("", "", "");
  }

  public static MaturityLevel emptyAppFiboMaturityLabel() {
    return new AppMaturityLevel("");
  }
}