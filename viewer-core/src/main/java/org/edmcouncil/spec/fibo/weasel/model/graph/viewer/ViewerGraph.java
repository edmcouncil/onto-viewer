package org.edmcouncil.spec.fibo.weasel.model.graph.viewer;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class ViewerGraph {

  private int lastId = 0;

  public ViewerGraph() {
  }

  public int getLastId() {
    return lastId;
  }

  public void setLastId(int lastId) {
    this.lastId = lastId;
  }

}
