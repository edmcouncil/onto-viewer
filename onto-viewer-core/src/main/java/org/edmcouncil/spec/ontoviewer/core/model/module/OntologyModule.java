package org.edmcouncil.spec.ontoviewer.core.model.module;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevel;

/**
 * Class will be used to storage data about fibo modules.
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OntologyModule implements Comparable<OntologyModule> {

  private String iri;
  private String label;
  private List<OntologyModule> subModule;
  private MaturityLevel maturityLevel = null;

  public OntologyModule() {
  }

  public OntologyModule(String iri, String label, List<OntologyModule> subModule, MaturityLevel maturityLevel) {
    this.iri = iri;
    this.label = label;
    this.subModule = subModule;
    this.maturityLevel = maturityLevel;
  }

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

  public List<OntologyModule> getSubModule() {
    return subModule;
  }

  public void setSubModule(List<OntologyModule> subModule) {
    this.subModule = subModule;
  }

  public MaturityLevel getMaturityLevel() {
    return maturityLevel;
  }

  public void setMaturityLevel(MaturityLevel maturityLevel) {
    this.maturityLevel = maturityLevel;
  }

  public int compareTo(OntologyModule o) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OntologyModule)) {
      return false;
    }
    OntologyModule that = (OntologyModule) o;
    return Objects.equals(iri, that.iri) && Objects.equals(label, that.label)
        && Objects.equals(subModule, that.subModule) && Objects.equals(maturityLevel,
        that.maturityLevel);
  }

  @Override
  public int hashCode() {
    return Objects.hash(iri, label, subModule, maturityLevel);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", OntologyModule.class.getSimpleName() + "[", "]")
        .add("iri='" + iri + "'")
        .add("label='" + label + "'")
        .add("subModule=" + subModule)
        .add("maturityLevel=" + maturityLevel)
        .toString();
  }
}
