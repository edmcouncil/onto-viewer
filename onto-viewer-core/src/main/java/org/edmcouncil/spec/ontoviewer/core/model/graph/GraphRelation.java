package org.edmcouncil.spec.ontoviewer.core.model.graph;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class GraphRelation extends GraphElement {

  private GraphNode start;
  private GraphNode end;
  private boolean optional;
  private GraphNodeType endNodeType;
  private Boolean equivalentTo = false;


  public GraphRelation(int id) {
    super(id);
  }

  public GraphNode getStart() {
    return start;
  }

  public GraphNodeType getEndNodeType() {
    return endNodeType;
  }

  public void setEndNodeType(GraphNodeType endNodeType) {
    this.endNodeType = endNodeType;
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

  public void setOptional(boolean b) {
    optional = b;
  }

  public boolean isOptional() {
    return optional;
  }

  public Boolean getEquivalentTo() {
    return equivalentTo;
  }

  public void setEquivalentTo(Boolean equivalentTo) {
    this.equivalentTo = equivalentTo;
  }
  
  @Override
  public String toString() {
    return "{" + "start=" + start + ", end=" + end + " " + super.toString();
  }


}
