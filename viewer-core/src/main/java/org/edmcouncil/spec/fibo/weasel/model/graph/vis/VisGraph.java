package org.edmcouncil.spec.fibo.weasel.model.graph.vis;

import com.google.gson.Gson;
import java.util.Set;
import org.edmcouncil.spec.fibo.weasel.model.graph.viewer.ViewerGraph;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */

public class VisGraph extends ViewerGraph {

  private Set<VisNode> nodes;
  private Set<VisRelation> edges;
  private VisNode root;

  public Set<VisNode> getNodes() {
    return nodes;
  }

  public void setNodes(Set<VisNode> nodes) {
    this.nodes = nodes;
  }

  public Set<VisRelation> getEdges() {
    return edges;
  }

  public void setEdges(Set<VisRelation> edges) {
    this.edges = edges;
  }

  public VisNode getRoot() {
    return root;
  }

  public void setRoot(VisNode root) {
    this.root = root;
  }

  public String getJsonNodes() {
    Gson gson = new Gson();
    return gson.toJson(nodes);

  }

  public String getJsonEdges() {
    Gson gson = new Gson();
    return gson.toJson(edges);

  }

}
