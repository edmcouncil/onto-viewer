package org.edmcouncil.spec.fibo.weasel.model.graph.viewer.converter.vis;

import org.edmcouncil.spec.fibo.weasel.model.graph.GraphNodeType;
import org.edmcouncil.spec.fibo.weasel.model.graph.viewer.ViewerNode;
import org.edmcouncil.spec.fibo.weasel.model.graph.vis.VisNode;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class VisNodeConverter {

  private String iri;
  private String color;
  private String label;
  private String shape;

  private ViewerNode viewerNode;
  private VisRelationConverter visRelationConverter;
  private VisNode visNode;

  public VisNodeConverter(String iri, String color, String label, String shape, ViewerNode viewerNode) {
    this.iri = iri;
    this.color = color;
    this.label = label;
    this.shape = shape;
    this.viewerNode = viewerNode;
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

  public void convertNode() {
    viewerNode.setIri(iri);
    //visNode.setIri(iri);
    //visNode.setShape(shape);
visRelationConverter. setEnd(visNode);
    visRelationConverter.setStart(visNode);

  }
}
