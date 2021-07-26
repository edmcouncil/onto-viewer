package org.edmcouncil.spec.ontoviewer.core.model.graph.vis;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class VisRelation {

  private int from;
  private int to;
  private String arrows = "to";
  private String label;
  private final VisColor color = VisColorFacory.getBlack();
  private boolean dashes = true;
  private String optional;
  private String type;
  private String iri;
  private Boolean equivalentTo;

  public Boolean getEquivalentTo() {
    return equivalentTo;
  }

  public void setEquivalentTo(Boolean equivalentTo) {
    this.equivalentTo = equivalentTo;
  }

  public int getFrom() {
    return from;
  }

  public void setFrom(int from) {
    this.from = from;
  }

  public int getTo() {
    return to;
  }

  public void setTo(int to) {
    this.to = to;
  }

  public String getArrows() {
    return arrows;
  }

  public void setArrows(String arrows) {
    this.arrows = arrows;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public boolean isDashes() {
    return dashes;
  }

  public void setDashes(boolean dashes) {
    this.dashes = dashes;
  }

  public String getOptional() {
    return optional;
  }

  public void setOptional(String optional) {
    this.optional = optional;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getIri() {
    return iri;
  }

  public void setIri(String iri) {
    this.iri = iri;
  }

  public VisColor getColor() {
    return color;
  }

}
