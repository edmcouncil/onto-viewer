package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.semanticweb.owlapi.model.AxiomType;

public class StringIdentifier {

  public static final String subClassOfIriString = ViewerIdentifierFactory
      .createId(ViewerIdentifierFactory.Type.axiom, AxiomType.SUBCLASS_OF.getName());
  public static final String subObjectPropertyOfIriString = ViewerIdentifierFactory
      .createId(ViewerIdentifierFactory.Type.axiom, AxiomType.SUB_OBJECT_PROPERTY.getName());
}
