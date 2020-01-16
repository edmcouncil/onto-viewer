package org.edmcouncil.spec.fibo.weasel.model.graph.viewer.converter.vis;

import org.edmcouncil.spec.fibo.weasel.model.graph.vis.VisNode;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class VisRelationConverter {

  private VisNode start;
  private VisNode end;

  public VisRelationConverter(VisNode start, VisNode end) {
    this.start = start;
    this.end = end;
  }

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

}
