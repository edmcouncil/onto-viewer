
package org.edmcouncil.spec.fibo.weasel.model.graph.vis;

import org.edmcouncil.spec.fibo.weasel.model.graph.GraphNode;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphNodeType;
import org.edmcouncil.spec.fibo.weasel.model.graph.viewer.ViewerRelation;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com) 
 */

public class VisRelation extends ViewerRelation{
  private VisNode start;
  private VisNode end;

  public VisNode getStart() {
    return start;
  }

  public void setStart(VisNode start) {
    this.start = start;
  }

  public VisNode getEnd() {
    return end;
  }

  public void setEnd(VisNode end) {
    this.end = end;
  }

  @Override
  public String toString() {
    return "VisRelation{" + "start=" + start + ", end=" + end + '}';
  }


  
  
}
