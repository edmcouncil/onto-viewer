package org.edmcouncil.spec.ontoviewer.core.ontology.factory;

import org.edmcouncil.spec.ontoviewer.core.model.OwlSimpleProperty;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationIri;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.DeprecatedHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class CustomDataFactory {

  private static final Logger LOG = LoggerFactory.getLogger(CustomDataFactory.class);

  private final LabelProvider labelExtractor;
  private final DeprecatedHandler deprecatedHandler;

  public CustomDataFactory(LabelProvider labelExtractor, DeprecatedHandler deprecatedHandler) {
    this.labelExtractor = labelExtractor;
    this.deprecatedHandler = deprecatedHandler;
  }

  /**
   *
   * @param iriString IRI element for which we create annotationIRI
   * @return AnnotationIri contains iri and label
   */
  public OwlAnnotationIri createAnnotationIri(String iriString) {
    LOG.debug("[Custom Data Factory] Create annotation for IRI: {}", iriString);

    OwlAnnotationIri owlAnnotationIri = new OwlAnnotationIri();
    OwlSimpleProperty simpleProperty = new OwlSimpleProperty();
    var iri = IRI.create(iriString);
    simpleProperty.setLabel(labelExtractor.getLabelOrDefaultFragment(iri));
    simpleProperty.setIri(iriString);
    simpleProperty.setDeprecated(deprecatedHandler.getDeprecatedForEntity(iri));
    owlAnnotationIri.setValue(simpleProperty);
    owlAnnotationIri.setType(OwlType.IRI);
    return owlAnnotationIri;
  }

  public OwlAnnotationPropertyValue createAnnotationAnyUri(String iri) {
    LOG.debug("[Custom Data Factory] Create annotation for URI: {}", iri);

    OwlAnnotationPropertyValue annotationPropertyValue = new OwlAnnotationPropertyValue();
    annotationPropertyValue.setType(OwlType.ANY_URI);
    annotationPropertyValue.setValue(iri);

    return annotationPropertyValue;
  }

}
