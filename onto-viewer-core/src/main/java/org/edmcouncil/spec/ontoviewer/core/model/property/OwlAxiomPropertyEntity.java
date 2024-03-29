package org.edmcouncil.spec.ontoviewer.core.model.property;

import java.util.StringJoiner;
import org.edmcouncil.spec.ontoviewer.core.mapping.OntoViewerEntityType;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OwlAxiomPropertyEntity {

  private String iri;
  private String label;
  private boolean deprecated;
  private OntoViewerEntityType entityType;

  public OwlAxiomPropertyEntity() {
  }

  public OwlAxiomPropertyEntity(String iri, String label, OntoViewerEntityType entityType, boolean deprecated) {
    this.iri = iri;
    this.label = label;
    this.deprecated = deprecated;
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

  public boolean isDeprecated() {
    return deprecated;
  }

  public void setDeprecated(boolean deprecated) {
    this.deprecated = deprecated;
  }

  public OntoViewerEntityType getEntityType() {
    return entityType;
  }

  public void setEntityType(OntoViewerEntityType entityType) {
    this.entityType = entityType;
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", OwlAxiomPropertyEntity.class.getSimpleName() + "[", "]")
        .add("iri='" + iri + "'")
        .add("label='" + label + "'")
        .add("deprecated=" + deprecated)
        .add("entityType=" + entityType)
        .toString();
  }
}