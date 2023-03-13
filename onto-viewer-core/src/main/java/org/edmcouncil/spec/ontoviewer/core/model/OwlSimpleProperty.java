
package org.edmcouncil.spec.ontoviewer.core.model;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */

public class OwlSimpleProperty {

  private String iri;
  private String label;
  private boolean deprecated;

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

  public boolean isDeprecated() { return deprecated; }

  public void setDeprecated(boolean deprecated) { this.deprecated = deprecated; }
}
