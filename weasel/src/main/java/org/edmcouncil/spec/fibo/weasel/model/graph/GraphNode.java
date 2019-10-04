package org.edmcouncil.spec.fibo.weasel.model.graph;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class GraphNode extends GraphElement{
  
  private int cardinality;

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
  
  
  
}
