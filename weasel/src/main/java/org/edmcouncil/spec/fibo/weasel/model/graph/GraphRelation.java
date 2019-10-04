package org.edmcouncil.spec.fibo.weasel.model.graph;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class GraphRelation extends GraphElement{
  
  private GraphNode start;
  private GraphNode end;

  public GraphNode getStart() {
    return start;
  }

  public void setStart(GraphNode start) {
    this.start = start;
  }

  public GraphNode getEnd() {
    return end;
  }

  public void setEnd(GraphNode end) {
    this.end = end;
  }

  @Override
  public String toString() {
    return "{" + "start=" + start + ", end=" + end + " " + super.toString();
  }
  
}
