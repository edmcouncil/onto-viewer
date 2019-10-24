package org.edmcouncil.spec.fibo.weasel.ontology.data.handler;

import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Stream;
import org.edmcouncil.spec.fibo.config.configuration.model.AppConfiguration;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.element.GroupLabelPriorityItem;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.WeaselConfiguration;
import org.edmcouncil.spec.fibo.weasel.model.PropertyValue;
import org.edmcouncil.spec.fibo.weasel.model.WeaselOwlType;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlAnnotationPropertyValue;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.fibo.weasel.ontology.data.CustomDataFactory;
import org.edmcouncil.spec.fibo.weasel.ontology.data.extractor.OwlDataExtractor;
import org.edmcouncil.spec.fibo.weasel.ontology.data.extractor.label.LabelExtractor;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class AnnotationsDataHandler {

  private static final Logger LOG = LoggerFactory.getLogger(AnnotationsDataHandler.class);
  private static final OWLObjectRenderer rendering = new ManchesterOWLSyntaxOWLObjectRendererImpl();

  @Autowired
  private OwlDataExtractor dataExtractor;
  @Autowired
  private CustomDataFactory customDataFactory;
  @Autowired
  private AppConfiguration appConfig;
  @Autowired
  private LabelExtractor labelExtractor;

  public OwlDetailsProperties<PropertyValue> handleAnnotations(IRI iri, OWLOntology ontology) {

    WeaselConfiguration config = (WeaselConfiguration) appConfig.getWeaselConfig();
    GroupLabelPriorityItem.Priority labelPriority = config.getGroupLabelPriority();

    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();

    Iterator<OWLAnnotationAssertionAxiom> annotationAssertionAxiom
        = ontology.annotationAssertionAxioms(iri).iterator();
    while (annotationAssertionAxiom.hasNext()) {
      OWLAnnotationAssertionAxiom next = annotationAssertionAxiom.next();
      IRI propertyiri = next.getProperty().getIRI();
      //String property = rendering.render(next.getProperty());
      String property = null;
      switch (labelPriority) {
        case FRAGMENT:
          property = rendering.render(next.getProperty());
          break;
        case LABEL:
          property = labelExtractor.getLabelOrDefaultFragment(propertyiri);
          break;
      }

      String value = next.annotationValue().toString();

      PropertyValue opv = new OwlAnnotationPropertyValue();
      WeaselOwlType extractAnnotationType = dataExtractor.extractAnnotationType(next);
      opv.setType(extractAnnotationType);

      if (next.getValue().isIRI()) {
        opv = customDataFactory.createAnnotationIri(value);

      } else if (next.getValue().isLiteral()) {
        Optional<OWLLiteral> asLiteral = next.getValue().asLiteral();
        if (asLiteral.isPresent()) {
          value = asLiteral.get().getLiteral();
          String lang = asLiteral.get().getLang();
          value = lang.isEmpty() ? value : value.concat(" [").concat(lang).concat("]");
          checkUriAsIri(opv, value);
          opv.setValue(value);
          if (opv.getType() == WeaselOwlType.IRI) {
            opv = customDataFactory.createAnnotationIri(value);
          }
        }
      }
      LOG.info("[Data Handler] Find annotation, value: \"{}\", property: \"{}\" ", opv, property);

      result.addProperty(property, opv);
    }
    result.sortPropertiesInAlphabeticalOrder();
    return result;
  }

  public OwlDetailsProperties<PropertyValue> handleOntologyAnnotations(Stream<OWLAnnotation> annotations, OWLOntology ontology) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();

    Iterator<OWLAnnotation> annotationIterator = annotations.iterator();
    while (annotationIterator.hasNext()) {
      OWLAnnotation next = annotationIterator.next();
      String property = rendering.render(next.getProperty());
      String value = next.annotationValue().toString();

      PropertyValue opv = new OwlAnnotationPropertyValue();
      WeaselOwlType extractAnnotationType = dataExtractor.extractAnnotationType(next);
      opv.setType(extractAnnotationType);

      if (next.getValue().isIRI()) {

        opv = customDataFactory.createAnnotationIri(value);

      } else if (next.getValue().isLiteral()) {
        Optional<OWLLiteral> asLiteral = next.getValue().asLiteral();
        if (asLiteral.isPresent()) {
          value = asLiteral.get().getLiteral();
          String lang = asLiteral.get().getLang();
          value = lang.isEmpty() ? value : value.concat(" [").concat(lang).concat("]");

          opv.setValue(value);
          checkUriAsIri(opv, value);
          if (opv.getType() == WeaselOwlType.IRI) {
            opv = customDataFactory.createAnnotationIri(value);
          }
        }
      }
      LOG.info("[Data Handler] Find annotation, value: \"{}\", property: \"{}\" ", opv, property);

      result.addProperty(property, opv);
    }
    return result;
  }

  private void checkUriAsIri(PropertyValue opv, String value) {
    //TODO: Change this to more pretty solution
    if (opv.getType() == WeaselOwlType.ANY_URI) {
      WeaselConfiguration weaselConfiguration = (WeaselConfiguration) appConfig.getWeaselConfig();
      if (weaselConfiguration.isUriIri(value)) {
        opv.setType(WeaselOwlType.IRI);
      }
    }
  }
}
