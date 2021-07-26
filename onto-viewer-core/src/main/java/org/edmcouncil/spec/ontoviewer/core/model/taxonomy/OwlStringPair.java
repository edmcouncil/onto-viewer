package org.edmcouncil.spec.ontoviewer.core.model.taxonomy;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class OwlStringPair {

  private String iri;
  private String label;

  public String getIri() {
    return iri;
  }

  public void setIri(String iri) {
    this.iri = iri;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public OwlStringPair() {
  }

  @Override
  public String toString() {
    return "OwlStringPair{" + "iri=" + iri + ", label=" + label + '}';
  }

  public OwlStringPair(String iri, String label) {
    this.iri = iri;
    this.label = label;
  }

}
