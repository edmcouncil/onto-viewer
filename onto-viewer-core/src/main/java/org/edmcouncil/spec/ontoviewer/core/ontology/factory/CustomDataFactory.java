package org.edmcouncil.spec.ontoviewer.core.ontology.factory;

import org.edmcouncil.spec.ontoviewer.core.model.OwlSimpleProperty;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationIri;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class CustomDataFactory {

  private static final Logger LOG = LoggerFactory.getLogger(CustomDataFactory.class);

  @Autowired
  private LabelProvider labelExtractor;

  /**
   *
   * @param iri IRI element for which we create annotationIRI
   * @return AnnotationIri contains iri and label
   */
  public OwlAnnotationIri createAnnotationIri(String iri) {
    LOG.debug("[Custom Data Factory] Create annotation for IRI: {}", iri);

    OwlAnnotationIri owlAnnotationIri = new OwlAnnotationIri();
    OwlSimpleProperty osp = new OwlSimpleProperty();
    osp.setLabel(labelExtractor.getLabelOrDefaultFragment(IRI.create(iri)));
    osp.setIri(iri);
    owlAnnotationIri.setValue(osp);
    owlAnnotationIri.setType(OwlType.IRI);
    return owlAnnotationIri;
  }

  public OwlAnnotationPropertyValue createAnnotationAnyUri(String iri) {
    LOG.debug("[Custom Data Factory] Create annotation for URI: {}", iri);

    OwlAnnotationPropertyValue val = new OwlAnnotationPropertyValue();
    val.setType(OwlType.ANY_URI);
    val.setValue(iri);

    return val;
  }

}
