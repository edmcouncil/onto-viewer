package org.edmcouncil.spec.ontoviewer.core.model.property;

import java.util.List;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OwlLabeledMultiAxiom extends MultiValue<OwlAxiomPropertyValue> {

  private OwlAxiomPropertyEntity entityLabel;

  public OwlAxiomPropertyEntity getEntityLabel() {
    return entityLabel;
  }

  public void setEntityLabel(OwlAxiomPropertyEntity entityLabel) {
    this.entityLabel = entityLabel;
  }

}
