package org.edmcouncil.spec.ontoviewer.core.model.graph;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class GraphNode extends GraphElement {

  private int cardinality;
  private boolean optional;
  private GraphNodeType type;
  private GraphRelation incommingRelation;

  public boolean isOptional() {
    return optional;
  }

  public void setOptional(boolean optional) {
    this.optional = optional;
  }

  public GraphNodeType getType() {
    return type;
  }

  public void setType(GraphNodeType type) {
    this.type = type;
  }

  public GraphNode(int id) {
    super(id);
  }

  public int getCardinality() {
    return cardinality;
  }

  public void setCardinality(int cardinality) {
    this.cardinality = cardinality;
  }

  @Override
  public String toString() {
    return "{" + "cardinality=" + cardinality + " " + super.toString();
  }

  public void setIncommingRelation(GraphRelation rel) {
    this.incommingRelation = rel;
  }

  public GraphRelation getIncommingRelation() {
    return incommingRelation;
  }

}
