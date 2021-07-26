package org.edmcouncil.spec.ontoviewer.core.model.taxonomy;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class OwlTaxonomyElementImpl {

  private String iri;
  private String label;
  private OwlTaxonomyElementImpl valThingLabel;
  private OwlTaxonomyElementImpl valThingIri;

  public OwlTaxonomyElementImpl(String iri, String label) {
    this.iri = iri;
    this.label = label;
  }

//  public OwlTaxonomyElementImpl(OwlTaxonomyElementImpl valThingLabel, OwlTaxonomyElementImpl valThingIri) {
//  this.valThingLabel = valThingLabel;
//  this.valThingIri = valThingIri;
//  }
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

  @Override
  public String toString() {
    return "OwlTaxonomyElementImpl{" + "iri=" + iri + ", label=" + label + ", valThingLabel=" + valThingLabel + ", valThingIri=" + valThingIri + '}';
  }

}
