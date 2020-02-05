package org.edmcouncil.spec.fibo.weasel.ontology.data.handler;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.edmcouncil.spec.fibo.config.configuration.model.AppConfiguration;
import org.edmcouncil.spec.fibo.config.configuration.model.impl.ViewerCoreConfiguration;
import org.edmcouncil.spec.fibo.weasel.model.PropertyValue;
import org.edmcouncil.spec.fibo.weasel.model.WeaselOwlType;
import org.edmcouncil.spec.fibo.weasel.model.details.OwlListDetails;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlAnnotationIri;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlAnnotationPropertyValue;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.fibo.weasel.ontology.factory.CustomDataFactory;
import org.edmcouncil.spec.fibo.weasel.ontology.data.extractor.OwlDataExtractor;
import org.edmcouncil.spec.fibo.weasel.ontology.data.handler.fibo.FiboMaturityLevel;
import org.edmcouncil.spec.fibo.weasel.ontology.data.handler.fibo.FiboMaturityLevelFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;
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
  private static final IRI COMMENT_IRI = IRI.create("http://www.w3.org/2000/01/rdf-schema#comment");
  private final String FIBO_QNAME = "QName:";
  private final IRI HAS_MATURITY_LEVEL_IRI = IRI.create("https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/hasMaturityLevel");

  @Autowired
  private OwlDataExtractor dataExtractor;
  @Autowired
  private CustomDataFactory customDataFactory;
  @Autowired
  private AppConfiguration appConfig;

  /**
   * @param iri IRI element with annotations to capture
   * @param ontology Loaded ontology
   * @param details <i>QName</i> will be set for this object if found
   * @return Processed annotations
   */
  public OwlDetailsProperties<PropertyValue> handleAnnotations(IRI iri, OWLOntology ontology, OwlListDetails details) {

    Set<String> ignoredToDisplay = appConfig.getViewerCoreConfig().getIgnoredElements();

    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();

    Iterator<OWLAnnotationAssertionAxiom> annotationAssertionAxiom
        = ontology.annotationAssertionAxioms(iri).iterator();
    while (annotationAssertionAxiom.hasNext()) {
      OWLAnnotationAssertionAxiom next = annotationAssertionAxiom.next();
      IRI propertyiri = next.getProperty().getIRI();
      if (ignoredToDisplay.contains(propertyiri.toString())) {
        continue;
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

          if (propertyiri.equals(COMMENT_IRI) && value.contains(FIBO_QNAME)) {
            details.setqName(value);
            continue;
          }

          String lang = asLiteral.get().getLang();
          value = lang.isEmpty() ? value : value.concat(" [").concat(lang).concat("]");

          checkUriAsIri(opv, value);
          opv.setValue(value);
          if (opv.getType() == WeaselOwlType.IRI) {
            opv = customDataFactory.createAnnotationIri(value);
          }
        }
      }
      LOG.info("[Data Handler] Find annotation, value: \"{}\", property iri: \"{}\" ", opv, propertyiri.toString());

      result.addProperty(propertyiri.toString(), opv);
    }
    result.sortPropertiesInAlphabeticalOrder();
    return result;
  }

  /**
   *
   * @param annotations Stream of OWL annotations
   * @param ontology Loaded ontology
   * @param details <i>QName</i> will be set for this object if found
   * @return Processed annotations
   */
  public OwlDetailsProperties<PropertyValue> handleOntologyAnnotations(Stream<OWLAnnotation> annotations, OWLOntology ontology, OwlListDetails details) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();
    Set<String> ignoredToDisplay = appConfig.getViewerCoreConfig().getIgnoredElements();
    for (OWLAnnotation next : annotations.collect(Collectors.toSet())) {
      IRI propertyiri = next.getProperty().getIRI();

      if (ignoredToDisplay.contains(propertyiri.toString())) {
        continue;
      }

      String value = next.annotationValue().toString();

      PropertyValue opv = new OwlAnnotationPropertyValue();
      WeaselOwlType extractAnnotationType = dataExtractor.extractAnnotationType(next);
      opv.setType(extractAnnotationType);

      if (next.getValue().isIRI()) {

        opv = customDataFactory.createAnnotationIri(value);

        if (propertyiri.equals(HAS_MATURITY_LEVEL_IRI)) {
          OwlAnnotationIri oai = (OwlAnnotationIri) opv;
          FiboMaturityLevel fml = FiboMaturityLevelFactory.create(oai.getValue().getLabel(), oai.getValue().getIri());
          details.setMaturityLevel(fml);
          LOG.debug(fml.toString());
        }

      } else if (next.getValue().isLiteral()) {
        Optional<OWLLiteral> asLiteral = next.getValue().asLiteral();
        if (asLiteral.isPresent()) {
          value = asLiteral.get().getLiteral();
          if (propertyiri.equals(COMMENT_IRI) && value.contains(FIBO_QNAME)) {
            details.setqName(value);
            continue;
          }

          String lang = asLiteral.get().getLang();
          value = lang.isEmpty() ? value : value.concat(" [").concat(lang).concat("]");
          opv.setValue(value);
          checkUriAsIri(opv, value);
          if (opv.getType() == WeaselOwlType.IRI) {
            opv = customDataFactory.createAnnotationIri(value);
            if (propertyiri.equals(HAS_MATURITY_LEVEL_IRI)) {
              OwlAnnotationIri oai = (OwlAnnotationIri) opv;
              FiboMaturityLevel fml = FiboMaturityLevelFactory.create(oai.getValue().getLabel(), oai.getValue().getIri());
              details.setMaturityLevel(fml);
              LOG.debug(fml.toString());
            }
          }
        }
      }
      LOG.info("[Data Handler] Find annotation, value: \"{}\", propertyIRI: \"{}\" ", opv, propertyiri.toString());

      result.addProperty(propertyiri.toString(), opv);
    }
    
    return result;
  }

  //TODO: change method name
  private void checkUriAsIri(PropertyValue opv, String value) {
    //TODO: Change this to more pretty solution
    if (opv.getType() == WeaselOwlType.ANY_URI) {
      ViewerCoreConfiguration weaselConfiguration = (ViewerCoreConfiguration) appConfig.getViewerCoreConfig();
      if (weaselConfiguration.isUriIri(value)) {
        opv.setType(WeaselOwlType.IRI);
      }
    }
  }

  /**
   * 
   * @param iri an IRI element used to extract the maturity level
   * @param o ontology with element for given iri
   * @return 
   */
  public FiboMaturityLevel getMaturityLevelForOntology(IRI iri, OWLOntology o) {

    OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();

    Stream<OWLAnnotation> annotations = EntitySearcher
        .getAnnotations(iri, o,
            dataFactory.getOWLAnnotationProperty(HAS_MATURITY_LEVEL_IRI));

    for (OWLAnnotation object : annotations.collect(Collectors.toSet())) {
      OwlAnnotationIri oai = customDataFactory.createAnnotationIri(object.getValue().asIRI().get().toString());
      return FiboMaturityLevelFactory.create(oai.getValue().getLabel(), oai.getValue().getIri());
    }

    return null;
  }

}
