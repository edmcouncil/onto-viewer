package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity;

import org.semanticweb.owlapi.model.IRI;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class MaturityLevelFactory {

  public static MaturityLevel PROD = new AppMaturityLevel("prod");
  public static MaturityLevel DEV = new AppMaturityLevel("dev");
  public static MaturityLevel PROD_DEV_MIXED = new AppMaturityLevel("prod_and_dev_mixed");
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