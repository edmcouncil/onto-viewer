package org.edmcouncil.spec.ontoviewer.core.model.details;

import java.util.List;
import java.util.Objects;
import org.edmcouncil.spec.ontoviewer.core.model.OwlTaxonomy;
import org.edmcouncil.spec.ontoviewer.core.model.graph.vis.VisGraph;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevel;

/**
 * Created by Michał Daniel (michal.mateusz.daniel@gmail.com)
 */
public class OwlDetails {

  private String label;
  private String iri;
  private String qName = "";
  private OwlTaxonomy taxonomy;
  private List<String> locationInModules;
  private VisGraph graph;
  private MaturityLevel maturityLevel;
  private Boolean deprecated;
  private String versionIri;

  public VisGraph getGraph() {
    return graph;
  }

  public void setGraph(VisGraph graph) {
    this.graph = graph;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getqName() {
    return qName;
  }

  public void setqName(String qName) {
    this.qName = qName;
  }

  public String getIri() {
    return iri;
  }

  public void setIri(String iri) {
    this.iri = iri;
  }

  public List<String> getLocationInModules() {
    return locationInModules;
  }

  public void setLocationInModules(List<String> locationInModules) {
    this.locationInModules = locationInModules;
  }

  public void setTaxonomy(OwlTaxonomy tax) {
    this.taxonomy = tax;
  }

  public OwlTaxonomy getTaxonomy() {
    return this.taxonomy;
  }

  public void setMaturityLevel(MaturityLevel maturityLevel) {
    this.maturityLevel = maturityLevel;
  }

  public MaturityLevel getMaturityLevel() {
    return maturityLevel;
  }
  
    public Boolean getDeprecated() {
    return deprecated;
  }

  public void setDeprecated(Boolean deprecated) {
    this.deprecated = deprecated;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 59 * hash + Objects.hashCode(this.label);
    hash = 59 * hash + Objects.hashCode(this.qName);
    hash = 59 * hash + Objects.hashCode(this.iri);
    hash = 59 * hash + Objects.hashCode(this.taxonomy);
    hash = 59 * hash + Objects.hashCode(this.deprecated);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final OwlDetails other = (OwlDetails) obj;
    if (!Objects.equals(this.label, other.label)) {
      return false;
    }
    if (!Objects.equals(this.iri, other.iri)) {
      return false;
    }
    if (!Objects.equals(this.taxonomy, other.taxonomy)) {
      return false;
    }
    if (!Objects.equals(this.deprecated, other.deprecated)) {
      return false;
    }
    return true;
  }

  public void setVersionIri(String versionIri) {
    this.versionIri = versionIri;
  }

  public String getVersionIri() {
    return versionIri;
  }

  @Override
  public String toString() {
    return "{" + "label=" + label + ", iri=" + iri + '}';
  }

}
