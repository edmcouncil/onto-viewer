package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import static org.semanticweb.owlapi.model.parameters.Imports.INCLUDED;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlListDetails;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationIri;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.extractor.OwlDataExtractor;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo.FiboMaturityLevelFactory;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo.OntoFiboMaturityLevel;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.CustomDataFactory;
import org.edmcouncil.spec.ontoviewer.core.ontology.scope.ScopeIriOntology;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class AnnotationsDataHandler {

  private static final Logger LOG = LoggerFactory.getLogger(AnnotationsDataHandler.class);
  private static final IRI COMMENT_IRI = OWLRDFVocabulary.RDFS_COMMENT.getIRI();
  private static final String RELEASE_LABEL = "release";
  private static final String PROVISIONAL_LABEL = "provisional";
  private static final String INFORMATIVE_LABEL = "informative" ;
  private static final String RELEASE_ICON = "release" ;
  private static final String DEVELOP_ICON = "develop" ;
  
  private final String FIBO_QNAME = "QName:";
  private final IRI HAS_MATURITY_LEVEL_IRI = IRI.create(
      "https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/hasMaturityLevel");

  private final OwlDataExtractor dataExtractor;
  private final CustomDataFactory customDataFactory;
  private final ConfigurationService appConfig;
  private final ScopeIriOntology scopeIriOntology;

  public AnnotationsDataHandler(OwlDataExtractor dataExtractor, CustomDataFactory customDataFactory,
      ConfigurationService appConfig, ScopeIriOntology scopeIriOntology) {
    this.dataExtractor = dataExtractor;
    this.customDataFactory = customDataFactory;
    this.appConfig = appConfig;
    this.scopeIriOntology = scopeIriOntology;
  }

  /**
   * @param iri      IRI element with annotations to capture
   * @param ontology Loaded ontology
   * @param details  <i>QName</i> will be set for this object if found
   * @return Processed annotations
   */
  public OwlDetailsProperties<PropertyValue> handleAnnotations(IRI iri,
      OWLOntology ontology,
      OwlListDetails details) {
    Set<String> ignoredToDisplay = appConfig.getCoreConfiguration().getIgnoredElements();

    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();

    var annotationAssertions = ontology.annotationAssertionAxioms(iri, INCLUDED)
        .collect(Collectors.toSet());
    for (OWLAnnotationAssertionAxiom annotationAssertion : annotationAssertions) {
      IRI propertyIri = annotationAssertion.getProperty().getIRI();
      if (ignoredToDisplay.contains(propertyIri.toString())) {
        continue;
      }
      String value = annotationAssertion.annotationValue().toString(); // TODO
      PropertyValue annotationPropertyValue = new OwlAnnotationPropertyValue();
      annotationPropertyValue.setType(dataExtractor.extractAnnotationType(annotationAssertion));

      if (annotationAssertion.getValue().isIRI()) {
        if (scopeIriOntology.scopeIri(value)) {
          annotationPropertyValue = customDataFactory.createAnnotationIri(value);
        } else {
          annotationPropertyValue = customDataFactory.createAnnotationAnyUri(value);
        }
      } else if (annotationAssertion.getValue().isLiteral()) {
        Optional<OWLLiteral> asLiteral = annotationAssertion.getValue().asLiteral();
        if (asLiteral.isPresent()) {
          value = asLiteral.get().getLiteral();

          if (propertyIri.equals(COMMENT_IRI) && value.contains(FIBO_QNAME)) {
            details.setqName(value);
            continue;
          }

          String lang = asLiteral.get().getLang();
          value = lang.isEmpty() ? value : value.concat(" @").concat(lang);

          checkUriAsIri(annotationPropertyValue, value);
          annotationPropertyValue.setValue(value);
          if (annotationPropertyValue.getType() == OwlType.IRI) {
            annotationPropertyValue = customDataFactory.createAnnotationIri(value);
          }
        }
      }

      LOG.debug("[Data Handler] Find annotation, value: \"{}\", property iri: \"{}\" ",
          annotationPropertyValue,
          propertyIri);

      result.addProperty(propertyIri.toString(), annotationPropertyValue);
    }
    result.sortPropertiesInAlphabeticalOrder();
    return result;
  }

  /**
   * @param annotations Stream of OWL annotations
   * @param details     <i>QName</i> will be set for this object if found
   * @return Processed annotations
   */
  public OwlDetailsProperties<PropertyValue> handleOntologyAnnotations(
      Stream<OWLAnnotation> annotations, OwlListDetails details) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();
    Set<String> ignoredToDisplay = appConfig.getCoreConfiguration().getIgnoredElements();
    for (OWLAnnotation next : annotations.collect(Collectors.toSet())) {
      IRI propertyiri = next.getProperty().getIRI();

      if (ignoredToDisplay.contains(propertyiri.toString())) {
        continue;
      }

      String value = next.annotationValue().toString();

      PropertyValue opv = new OwlAnnotationPropertyValue();
      OwlType extractAnnotationType = dataExtractor.extractAnnotationType(next);
      opv.setType(extractAnnotationType);

      if (next.getValue().isIRI()) {
        if (scopeIriOntology.scopeIri(value)) {
          opv = customDataFactory.createAnnotationIri(value);

        } else {
          opv = customDataFactory.createAnnotationAnyUri(value);
        }

        if (propertyiri.equals(HAS_MATURITY_LEVEL_IRI)) {
          OwlAnnotationIri oai = (OwlAnnotationIri) opv;
          OntoFiboMaturityLevel fml = FiboMaturityLevelFactory.create(oai.getValue().getLabel(),
              oai.getValue().getIri(), getIconForMaturityLevel(oai.getValue().getLabel()));
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
          if (opv.getType() == OwlType.IRI) {
            opv = customDataFactory.createAnnotationIri(value);
            if (propertyiri.equals(HAS_MATURITY_LEVEL_IRI)) {
              OwlAnnotationIri oai = (OwlAnnotationIri) opv;
              OntoFiboMaturityLevel fml = FiboMaturityLevelFactory.create(oai.getValue().getLabel(),
                  oai.getValue().getIri(), getIconForMaturityLevel(oai.getValue().getLabel()));
              details.setMaturityLevel(fml);
              LOG.debug(fml.toString());
            }
          }
        }
      }
      LOG.debug("[Data Handler] Find annotation, value: \"{}\", propertyIRI: \"{}\" ", opv,
          propertyiri);

      result.addProperty(propertyiri.toString(), opv);
    }

    return result;
  }

  //TODO: change method name
  private void checkUriAsIri(PropertyValue opv, String value) {
    //TODO: Change this to more pretty solution
    if (opv.getType() == OwlType.ANY_URI) {

      if (scopeIriOntology.scopeIri(value)) {
        opv.setType(OwlType.IRI);

      } else {
        opv.setType(OwlType.ANY_URI);
      }
    }
  }
  
  public String getIconForMaturityLevel(String label) {
        switch (label) {
            case RELEASE_LABEL:
                return RELEASE_ICON;
            case PROVISIONAL_LABEL:
            case INFORMATIVE_LABEL:
                return DEVELOP_ICON;
            default:
                return "";
        }
    }

  
}
