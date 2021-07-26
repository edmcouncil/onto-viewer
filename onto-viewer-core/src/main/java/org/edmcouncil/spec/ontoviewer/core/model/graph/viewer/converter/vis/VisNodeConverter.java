package org.edmcouncil.spec.ontoviewer.core.model.graph.viewer.converter.vis;

import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNode;
import org.edmcouncil.spec.ontoviewer.core.model.graph.GraphNodeType;
import org.edmcouncil.spec.ontoviewer.core.model.graph.vis.VisNode;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class VisNodeConverter {

  public VisNode convert(GraphNode gn) {
    VisNode result = new VisNode(gn.getId());

    String jLabel = gn.getLabel();
    jLabel = jLabel.replaceAll("'", "\u0027");

    result.setLabel(jLabel);
    result.setOptional(gn.isOptional());
    result.setType(gn.getType());

    String iriDto = gn.getIri();
    iriDto = iriDto == null || iriDto.isEmpty() ? "http://www.w3.org/2002/07/owl#Thing" : iriDto;
    String outIri = iriDto.replaceAll("#", "%23");
    result.setIri(outIri);

    if (gn.getType() == GraphNodeType.MAIN) {
      result.setColor("rgb(255,168,7)");
    } else if (gn.getType() == GraphNodeType.INTERNAL) {
      result.setColor("#C2FABC");
    }

    result.setShape("box");
    return result;
  }
}
