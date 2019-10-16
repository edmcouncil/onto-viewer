package org.edmcouncil.spec.fibo.weasel.model.graph;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class GraphNode extends GraphElement {

  private int cardinality;
  private boolean optional;
  private GraphNodeType type;

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

  //
  @Override
  public String toSimpleJson() {
    //TODO: String.format in this case
    String shape = super.getLabel().isEmpty() ? "" : ", shape: 'box'";
    String optionalVar = this.optional ? ", shapeProperties:{borderDashes:true}" : "";
    String nodeStyle = "";
    if (type == GraphNodeType.MAIN) {
      nodeStyle = ", color: 'rgb(255,168,7)'";
    } else if (type == GraphNodeType.INTERNAL) {
      nodeStyle = ", color:'#C2FABC'";
    }
    String format = "{id: %s, label: '%s' " + shape + ",font:{size:15}" + nodeStyle + optionalVar + "}";
    return String.format(format, super.getId(), super.getLabel());
  }

}
