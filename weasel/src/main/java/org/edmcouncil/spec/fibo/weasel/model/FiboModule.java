package org.edmcouncil.spec.fibo.weasel.model;

import java.util.List;

/**
 * Class will be used to storage data about fibo modules.
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class FiboModule {

  private String iri;
  private String label;
  private List<FiboModule> subModule;

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

  public List<FiboModule> getSubModule() {
    return subModule;
  }

  public void setSubModule(List<FiboModule> subModule) {
    this.subModule = subModule;
  }

}
