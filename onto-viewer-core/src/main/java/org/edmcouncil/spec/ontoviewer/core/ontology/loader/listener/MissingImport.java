package org.edmcouncil.spec.ontoviewer.core.ontology.loader.listener;

/**
 * @author patrycja.miazek (patrycja.miazek@makolab.com)
 */
public class MissingImport {
  
  private String iri;
  private String cause;

  public String getIri() {
    return iri;
  }

  public void setIri(String iri) {
    this.iri = iri;
  }

  public String getCause() {
    return cause;
  }

  public void setCause(String cause) {
    this.cause = cause;
  }

  @Override
  public String toString() {
    return "{" + "iri=" + iri + ", cause=" + cause + '}';
  }
}
