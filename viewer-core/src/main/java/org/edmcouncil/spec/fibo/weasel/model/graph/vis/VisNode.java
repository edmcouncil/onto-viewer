package org.edmcouncil.spec.fibo.weasel.model.graph.vis;

import org.edmcouncil.spec.fibo.weasel.model.graph.GraphNode;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphNodeType;
import org.edmcouncil.spec.fibo.weasel.model.graph.viewer.ViewerNode;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class VisNode extends GraphNode{

  private String iri;
  //private String nodeStyle;
  //private String nodeShape;
  private String color;
  private String label;
  private String shape;

  public VisNode(String iri, String color, String label, String shape, int id) {
    super(id);
    this.iri = iri;
    this.color = color;
    this.label = label;
    this.shape = shape;
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

  
}
