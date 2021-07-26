package org.edmcouncil.spec.ontoviewer.core.model.graph.vis;

import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNodeType;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class VisNode {

  private String iri;
  private String label;
  private VisFont font = VisFont.createDefault();
  private String color;
  private String shape;
  private int id;
  private boolean optional;
  private GraphNodeType type;

  public VisNode(int id) {
    this.id = id;
  }

  public String getIri() {
    return iri;
  }

  public void setIri(String iri) {
    this.iri = iri;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getShape() {
    return shape;
  }

  public void setShape(String shape) {
    this.shape = shape;
  }

  @Override
  public String toString() {
    return "VisNode{" + "iri=" + iri + ", color=" + color + ", label=" + label + ", shape=" + shape + '}';
  }

  public void setOptional(boolean optional) {
    this.optional = optional;
  }

  public void setType(GraphNodeType type) {
    this.type = type;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public VisFont getFont() {
    return font;
  }

  public boolean isOptional() {
    return optional;
  }

  public GraphNodeType getType() {
    return type;
  }

  public void setFont(VisFont font) {
    this.font = font;
  }

}
