package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom;

import java.io.Serializable;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.util.ShortFormProvider;

public class IriAsShortProvider implements ShortFormProvider, Serializable {

  public IriAsShortProvider() {
  }

  public String getShortForm(OWLEntity entity) {
    return entity.getIRI().getIRIString();
  }
}

