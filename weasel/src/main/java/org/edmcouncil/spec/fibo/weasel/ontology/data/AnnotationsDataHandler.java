package org.edmcouncil.spec.fibo.weasel.ontology.data;

import java.util.Iterator;
import java.util.stream.Stream;
import org.edmcouncil.spec.fibo.weasel.model.PropertyValue;
import org.edmcouncil.spec.fibo.weasel.model.WeaselOwlType;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlAnnotationPropertyValue;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlDetailsProperties;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationsDataHandler.class);
  private static final OWLObjectRenderer rendering = new ManchesterOWLSyntaxOWLObjectRendererImpl();

  @Autowired
  private OwlDataExtractor dataExtractor;

  public OwlDetailsProperties<PropertyValue> handleAnnotations(IRI iri, OWLOntology ontology) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();

    Iterator<OWLAnnotationAssertionAxiom> annotationAssertionAxiom
        = ontology.annotationAssertionAxioms(iri).iterator();
    while (annotationAssertionAxiom.hasNext()) {
      OWLAnnotationAssertionAxiom next = annotationAssertionAxiom.next();
      String property = rendering.render(next.getProperty());
      String value = next.getValue().toString();

      LOGGER.trace("[Data Handler] Find annotation, value: \"{}\", property: \"{}\" ", value, property);

      OwlAnnotationPropertyValue opv = new OwlAnnotationPropertyValue();

      opv.setType(dataExtractor.extractAnnotationType(next));
      if (opv.getType().equals(WeaselOwlType.ANY_URI)) {
        opv.setValue(dataExtractor.extractAnyUriToString(value));
      } else {
        opv.setValue(value);
      }
      result.addProperty(property, opv);
    }
    return result;
  }

  public OwlDetailsProperties<PropertyValue> handleOntologyAnnotations(Stream<OWLAnnotation> annotations) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();

    Iterator<OWLAnnotation> annotationIterator = annotations.iterator();
    while (annotationIterator.hasNext()) {
      OWLAnnotation next = annotationIterator.next();
      String property = rendering.render(next.getProperty());
      String value = next.getValue().toString();

      LOGGER.trace("[Data Handler] Find annotation, value: \"{}\", property: \"{}\" ", value, property);

      OwlAnnotationPropertyValue opv = new OwlAnnotationPropertyValue();

      opv.setType(dataExtractor.extractAnnotationType(next));
      if (opv.getType().equals(WeaselOwlType.ANY_URI)) {
        opv.setValue(dataExtractor.extractAnyUriToString(value));
      } else {
        opv.setValue(value);
      }
      result.addProperty(property, opv);
    }
    return result;
  }
}
