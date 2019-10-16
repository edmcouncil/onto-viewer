package org.edmcouncil.spec.fibo.weasel.model.graph;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class GraphRelation extends GraphElement {

  private GraphNode start;
  private GraphNode end;
  private boolean optional;
  private GraphNodeType endNodeType;

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

  @Override
  public String toString() {
    return "{" + "start=" + start + ", end=" + end + " " + super.toString();
  }

  @Override
  public String toSimpleJson() {
    String optionalStyle = optional ? ", dashes:true" : "";
    String optionalVariable = ", optional:" + (isOptional() ? "'optional'" : "'non_optional'");
    String typeVariable = ", type:" + (endNodeType == GraphNodeType.INTERNAL ? "'internal'" : "'external'");
    String format = "{from: %s, to: %s, arrows:'to', label: '%s' " + optionalStyle + ", color:{color:'black'}" + optionalVariable + typeVariable + "}";
    return String.format(format, start.getId(), end.getId(), super.getLabel());
  }

  public void setOptional(boolean b) {
    optional = b;
  }

  public boolean isOptional() {
    return optional;
  }

}
