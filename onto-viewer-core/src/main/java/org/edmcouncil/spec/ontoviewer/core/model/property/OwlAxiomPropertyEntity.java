package org.edmcouncil.spec.ontoviewer.core.model.property;

import org.edmcouncil.spec.ontoviewer.core.mapping.OntoViewerEntityType;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OwlAxiomPropertyEntity {

  private String iri;
  private String label;
  private OntoViewerEntityType entityType;

  public OwlAxiomPropertyEntity() {
  }

  public OwlAxiomPropertyEntity(String iri, String label, OntoViewerEntityType entityType) {
    this.iri = iri;
    this.label = label;
    this.entityType = entityType;
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

  public OntoViewerEntityType getEntityType() {
    return entityType;
  }

  public void setEntityType(OntoViewerEntityType entityType) {
    this.entityType = entityType;
  }

  @Override
  public String toString() {
    return "OwlAxiomPropertyEntity{" + "iri=" + iri + ", label=" + label + '}';
  }
}