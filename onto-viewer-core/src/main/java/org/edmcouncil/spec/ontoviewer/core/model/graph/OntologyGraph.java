package org.edmcouncil.spec.ontoviewer.core.model.graph;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
public class OntologyGraph {

  private Set<GraphNode> nodes;
  private Set<GraphRelation> relations;
  private GraphNode root;
  private int lastId = 0;

  public OntologyGraph() {
    this.nodes = new HashSet<>();
    this.relations = new HashSet<>();
  }

  public Set<GraphNode> getNodes() {
    return nodes;
  }

  public int nextId() {
    lastId++;
    return lastId;
  }

  public void setNodes(Set<GraphNode> nodes) {
    this.nodes = nodes;
  }

  public GraphNode getRoot() {
    return root;
  }

  public void setRoot(GraphNode root) {
    this.root = root;
  }

  public Set<GraphRelation> getRelations() {
    return relations;
  }

  public void setRelations(Set<GraphRelation> relations) {
    this.relations = relations;
  }

  public void addNode(GraphNode node) {
    if (nodes == null) {
      nodes = new HashSet<>();
    }
    nodes.add(node);
  }

  public void addRelation(GraphRelation rel) {
    if (relations == null) {
      relations = new HashSet<>();
    }
    relations.add(rel);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("ViewerGraph \n");
    if (relations != null) {
      sb.append("Relations: \n");
      relations.forEach((rel) -> {
        sb.append("\t").append(rel.toString()).append("\n");
      });
    }

    if (nodes != null) {
      sb.append("Nodes: \n");
      nodes.forEach((node) -> {
        sb.append("\t").append(node.toString()).append("\n");
      });
    }
    if (root != null) {
      sb.append("Root Node: \n");
      sb.append(root.toString());
    }

    return sb.toString();
  }

  public boolean isEmpty() {
    return nodes.isEmpty() && relations.isEmpty();
  }
}
