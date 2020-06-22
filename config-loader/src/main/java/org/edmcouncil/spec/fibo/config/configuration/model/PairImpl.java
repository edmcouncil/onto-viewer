package org.edmcouncil.spec.fibo.config.configuration.model;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 *
 * @param <String>
 * @param <IRI>
 */
@Deprecated
public class PairImpl<String, IRI> implements Pair<String, IRI> {

  private String label;
  private IRI iri;

  public PairImpl() {
  }

  public PairImpl(String label, IRI iri) {
    this.label = label;
    this.iri = iri;
  }

  @Override
  public String getLabel() {
    return this.label;
  }

  @Override
  public IRI getIri() {
    return this.iri;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setIri(IRI iri) {
    this.iri = iri;
  }
}
