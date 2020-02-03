package org.edmcouncil.spec.fibo.weasel.model.module;

import java.util.List;
import java.util.stream.Collectors;
import org.edmcouncil.spec.fibo.weasel.ontology.data.handler.fibo.FiboMaturityLevel;

/**
 * Class will be used to storage data about fibo modules.
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class FiboModule {

  private String iri;
  private String label;
  private List<FiboModule> subModule;
  private FiboMaturityLevel maturityLevel = null;

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

  public FiboMaturityLevel getMaturityLevel() {
    return maturityLevel;
  }

  public void setMaturityLevel(FiboMaturityLevel maturityLevel) {
    this.maturityLevel = maturityLevel;
  }

  public int compareTo(FiboModule o) {
    return this.label.compareTo(o.getLabel());
  }

  public void sort() {
    if (this.subModule != null && this.subModule.size() > 0) {
      subModule = subModule.stream()
          .sorted((obj1, obj2) -> obj1.getLabel().compareTo(obj2.getLabel()))
          .map(r -> {
            r.sort();
            return r;
          }).collect(Collectors.toList());
    }
  }

}
