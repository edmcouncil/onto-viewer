package org.edmcouncil.spec.ontoviewer.core.model.graph;

import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    GraphRelation that = (GraphRelation) o;

    if (optional != that.optional) {
      return false;
    }
    if (!start.equals(that.start)) {
      return false;
    }
    if (!end.equals(that.end)) {
      return false;
    }
    if (endNodeType != that.endNodeType) {
      return false;
    }
    return Objects.equals(equivalentTo, that.equivalentTo);
  }

  @Override
  public int hashCode() {
    int result = start.hashCode();
    result = 31 * result + end.hashCode();
    result = 31 * result + (optional ? 1 : 0);
    result = 31 * result + (endNodeType != null ? endNodeType.hashCode() : 0);
    result = 31 * result + (equivalentTo != null ? equivalentTo.hashCode() : 0);
    return result;
  }
}
