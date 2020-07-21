package org.edmcouncil.spec.fibo.weasel.model.graph.viewer.converter.vis;

import org.edmcouncil.spec.fibo.weasel.model.graph.GraphNodeType;
import org.edmcouncil.spec.fibo.weasel.model.graph.GraphRelation;
import org.edmcouncil.spec.fibo.weasel.model.graph.vis.VisRelation;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
public class VisRelationConverter {

    public VisRelation convert(GraphRelation gr) {

        VisRelation result = new VisRelation();

        result.setFrom(gr.getStart().getId());
        result.setTo(gr.getEnd().getId());

        String iriDto = gr.getIri();
        iriDto = iriDto == null || iriDto.isEmpty() ? "http://www.w3.org/2002/07/owl#Thing" : iriDto;
        String outIri = iriDto.replaceAll("#", "%23");
        result.setIri(outIri);

        result.setOptional((gr.isOptional() ? "optional" : "non_optional"));
        result.setDashes(gr.isOptional());
        
        String jLabel = gr.getLabel();
        jLabel = jLabel.replaceAll("'", "\u0027");
        result.setLabel(jLabel);

        String typeVariable = (gr.getEndNodeType() == GraphNodeType.INTERNAL ? "internal" : "external");
        result.setType(typeVariable);
         result.setEquivalentTo(gr.getEquivalentTo());
    if (gr.getEquivalentTo()) {
      result.setArrows("to;from");
    }
        return result;
    }

}
