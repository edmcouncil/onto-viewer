package org.edmcouncil.spec.fibo.weasel.model.graph;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class GraphNode extends GraphElement{
  
  private int cardinality;

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
    String format = "{id: %s, label: '%s', font:{size:15}}";
    return String.format(format, super.getId(), super.getLabel());
  }
  
}
