package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.semanticweb.owlapi.model.AxiomType;
import org.springframework.stereotype.Component;

@Component
public class StringIdentifier {

  public final String subClassOfIriString = ViewerIdentifierFactory
      .createId(ViewerIdentifierFactory.Type.axiom, AxiomType.SUBCLASS_OF.getName());
  public final String subObjectPropertyOfIriString = ViewerIdentifierFactory
      .createId(ViewerIdentifierFactory.Type.axiom, AxiomType.SUB_OBJECT_PROPERTY.getName());
}
