package org.edmcouncil.spec.fibo.weasel.model.graph.viewer;

import java.util.HashSet;
import java.util.Set;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphNode;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphRelation;
import org.edmcouncil.spec.fibo.weasel.model.graph.OntologyGraph;
import org.edmcouncil.spec.fibo.weasel.model.graph.viewer.converter.vis.VisNodeConverter;
import org.edmcouncil.spec.fibo.weasel.model.graph.viewer.converter.vis.VisRelationConverter;
import org.edmcouncil.spec.fibo.weasel.model.graph.vis.VisGraph;
import org.edmcouncil.spec.fibo.weasel.model.graph.vis.VisNode;
import org.edmcouncil.spec.fibo.weasel.model.graph.vis.VisRelation;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class ViewerGraphFactory {

    public VisGraph convertToVisGraph(OntologyGraph og) {
        VisGraph result = new VisGraph();
        VisNodeConverter vnc = new VisNodeConverter();
        VisRelationConverter vrc = new VisRelationConverter();

        Set<VisNode> allNodes = new HashSet<>();
        for (GraphNode node : og.getNodes()) {
            allNodes.add(vnc.convert(node));
        }

        Set<VisRelation> allRelation = new HashSet<>();
        for (GraphRelation relation : og.getRelations()) {
            allRelation.add(vrc.convert(relation));
        }
        
        result.setNodes(allNodes);
        result.setEdges(allRelation);
        return result;
    }

}
