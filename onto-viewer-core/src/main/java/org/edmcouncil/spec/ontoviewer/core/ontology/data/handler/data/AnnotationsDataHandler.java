package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.data;

import static org.edmcouncil.spec.ontoviewer.core.model.OwlType.AXIOM_ANNOTATION_PROPERTY;
import static org.semanticweb.owlapi.model.parameters.Imports.INCLUDED;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.Pair;
import org.edmcouncil.spec.ontoviewer.core.exception.OntoViewerException;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlListDetails;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationIri;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationPropertyValueWithSubAnnotations;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDirectedSubClassesProperty;
import org.edmcouncil.spec.ontoviewer.core.model.taxonomy.OwlTaxonomyImpl;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.extractor.OwlDataExtractor;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.CopyrightHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.classes.ClassDataHelper;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.LicenseHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.extractor.TaxonomyExtractor;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.QnameHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevel;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevelFactory;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom.AxiomsHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.CustomDataFactory;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.edmcouncil.spec.ontoviewer.core.ontology.scope.ScopeIriOntology;
import org.edmcouncil.spec.ontoviewer.core.service.EntitiesCacheService;
import org.semanticweb.owlapi.model.HasAnnotationValue;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;
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
  private static final String FIBO_QNAME = "QName:";
  private final OwlDataExtractor dataExtractor;
  private final CustomDataFactory customDataFactory;
  private final ScopeIriOntology scopeIriOntology;
  private final MaturityLevelFactory maturityLevelFactory;
  private final EntitiesCacheService entitiesCacheService;
  private final LabelProvider labelProvider;
  private final AxiomsHandler axiomsHandler;
  private final QnameHandler qnameHandler;
  private final LicenseHandler licenseHandler;
  private final CopyrightHandler copyrightHandler;
  private final ClassDataHelper extractSubAndSuper;
  private final TaxonomyExtractor taxonomyExtractor;

  public AnnotationsDataHandler(OwlDataExtractor dataExtractor, CustomDataFactory customDataFactory,
      ScopeIriOntology scopeIriOntology, MaturityLevelFactory maturityLevelFactory,
      EntitiesCacheService entitiesCacheService, LabelProvider labelProvider,
      AxiomsHandler axiomsHandler, QnameHandler qnameHandler, LicenseHandler licenseHandler,
      CopyrightHandler copyrightHandler, ClassDataHelper extractSubAndSuper,
      TaxonomyExtractor taxonomyExtractor) {
    this.dataExtractor = dataExtractor;
    this.customDataFactory = customDataFactory;
    this.scopeIriOntology = scopeIriOntology;
    this.maturityLevelFactory = maturityLevelFactory;
    this.entitiesCacheService = entitiesCacheService;
    this.labelProvider = labelProvider;
    this.axiomsHandler = axiomsHandler;
    this.qnameHandler = qnameHandler;
    this.licenseHandler = licenseHandler;
    this.copyrightHandler = copyrightHandler;
    this.extractSubAndSuper = extractSubAndSuper;
    this.taxonomyExtractor = taxonomyExtractor;
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
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();

    var annotationAssertions = ontology
        .annotationAssertionAxioms(iri, INCLUDED)
        .collect(Collectors.toSet());
    for (OWLAnnotationAssertionAxiom annotationAssertion : annotationAssertions) {
      Map<String, PropertyValue> annotationsForAnnotationAssertion = new LinkedHashMap<>();
      IRI propertyIri = annotationAssertion.getProperty().getIRI();

      for (OWLAnnotation owlAnnotation : annotationAssertion.annotations()
          .collect(Collectors.toSet())) {

        var annotationValue = extractSubAnnotation(owlAnnotation);
        annotationsForAnnotationAssertion.put(owlAnnotation.getProperty().getIRI().getIRIString(),
            annotationValue);
      }
 
      String value = annotationAssertion.annotationValue().toString();
      PropertyValue annotationPropertyValue = new OwlAnnotationPropertyValueWithSubAnnotations();
      ((OwlAnnotationPropertyValueWithSubAnnotations) annotationPropertyValue).setSubAnnotations(
          annotationsForAnnotationAssertion);

      annotationPropertyValue.setType(dataExtractor.extractAnnotationType(annotationAssertion));

      annotationPropertyValue = getAnnotationPropertyValue(annotationAssertion, value,
          annotationPropertyValue);
      
      if (propertyIri.equals(COMMENT_IRI) && value.contains(FIBO_QNAME)) {
        details.setqName(value);
        continue;
      }
      LOG.debug("[Data Handler] Find annotation, value: \"{}\", property iri: \"{}\" ",
          annotationPropertyValue,
          propertyIri);

      result.addProperty(propertyIri.toString(), annotationPropertyValue);
    }
    result.sortPropertiesInAlphabeticalOrder();
    return result;
  }

  private PropertyValue extractSubAnnotation(OWLAnnotation owlAnnotation) {
    String value = owlAnnotation.annotationValue().toString();
    PropertyValue annotationValue = new OwlAnnotationPropertyValue();
    annotationValue.setType(dataExtractor.extractAnnotationType(owlAnnotation));
    annotationValue = getAnnotationPropertyValue(owlAnnotation, value, annotationValue);
    return annotationValue;
  }

  private <T extends HasAnnotationValue> PropertyValue getAnnotationPropertyValue(
      T annotationAssertion,
      String value, PropertyValue annotationPropertyValue) {
    if (annotationAssertion.annotationValue().isIRI()) {
      annotationPropertyValue = getPropertyValueAsIri(value, annotationPropertyValue);
    } else if (annotationAssertion.annotationValue().isLiteral()) {
      annotationPropertyValue = getPropertyValueAsLiteral(value, annotationPropertyValue,
          annotationAssertion);
    }
    return annotationPropertyValue;
  }

  private <T extends HasAnnotationValue> PropertyValue getPropertyValueAsLiteral(String value,
      PropertyValue annotationPropertyValue,
      T annotationAssertion) {
    Optional<OWLLiteral> asLiteral = annotationAssertion.annotationValue().asLiteral();
    if (asLiteral.isPresent()) {
      value = asLiteral.get().getLiteral();

      String lang = asLiteral.get().getLang();
      value = lang.isEmpty() ? value : value.concat(" @").concat(lang);

      checkUriAsIri(annotationPropertyValue, value);
      annotationPropertyValue.setValue(value);
      if (annotationPropertyValue.getType() == OwlType.IRI) {
        annotationPropertyValue = customDataFactory.createAnnotationIri(value);
      }
    }
    return annotationPropertyValue;
  }

  private PropertyValue getPropertyValueAsIri(String value, PropertyValue annotationPropertyValue) {
    if (scopeIriOntology.scopeIri(value)) {
      annotationPropertyValue = customDataFactory.createAnnotationIri(value);
    } else {
      annotationPropertyValue = customDataFactory.createAnnotationAnyUri(value);
    }
    return annotationPropertyValue;
  }

  /**
   * @param annotations Stream of OWL annotations
   * @param details     <i>QName</i> will be set for this object if found
   * @return Processed annotations
   */
  public OwlDetailsProperties<PropertyValue> handleOntologyAnnotations(
      Stream<OWLAnnotation> annotations, OwlListDetails details) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();
    for (OWLAnnotation next : annotations.collect(Collectors.toSet())) {
      IRI propertyiri = next.getProperty().getIRI();

      String value = next.annotationValue().toString();

      PropertyValue propertyValue = new OwlAnnotationPropertyValue();
      OwlType extractAnnotationType = dataExtractor.extractAnnotationType(next);
      propertyValue.setType(extractAnnotationType);

      if (next.getValue().isIRI()) {
        propertyValue = getPropertyValueAsIri(value, propertyValue);

        if (propertyValue instanceof OwlAnnotationIri) {
          setMaturityLevel(details, propertyiri, (OwlAnnotationIri) propertyValue);
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
          propertyValue.setValue(value);
          checkUriAsIri(propertyValue, value);
          if (propertyValue.getType() == OwlType.IRI) {
            propertyValue = customDataFactory.createAnnotationIri(value);
            if (propertyValue instanceof OwlAnnotationIri) {
              setMaturityLevel(details, propertyiri, (OwlAnnotationIri) propertyValue);
            }
          }
        }
      }
      LOG.debug("[Data Handler] Find annotation, value: \"{}\", propertyIRI: \"{}\" ",
          propertyValue,
          propertyiri);

      result.addProperty(propertyiri.toString(), propertyValue);
    }

    return result;
  }

  private void setMaturityLevel(OwlListDetails details, IRI propertyIri,
      OwlAnnotationIri propertyValue) {
    var levelString = maturityLevelFactory.getMaturityLevels()
        .stream()
        .map(maturityLevel -> maturityLevel.getIri())
        .collect(Collectors.toSet());
    if (!propertyValue.getValue().getIri().isEmpty()
        && levelString.contains(propertyValue.getValue().getIri())) {
      String annotationIri = propertyValue.getValue().getIri();
      Optional<MaturityLevel> maturityLevel = maturityLevelFactory.getByIri(annotationIri);
      if (maturityLevel.isPresent()) {
        details.setMaturityLevel(maturityLevel.get());
      } else {
        details.setMaturityLevel(maturityLevelFactory.notSet());
      }
    }
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

  public OwlListDetails handleParticularAnnotationProperty(IRI iri, OWLOntology ontology) {
    OwlListDetails resultDetails = new OwlListDetails();

    var entityEntry =
        entitiesCacheService.getEntityEntry(iri, OwlType.ANNOTATION_PROPERTY);

    try {
      if (entityEntry.isPresent()) {
        var annotationProperty = entityEntry.getEntityAs(OWLAnnotationProperty.class);

        if (annotationProperty.getIRI().equals(iri)) {
          resultDetails.setLabel(labelProvider.getLabelOrDefaultFragment(iri));

          OwlDetailsProperties<PropertyValue> directSubAnnotationProperty =
              handleDirectSubAnnotationProperty(ontology, annotationProperty);

          OwlDetailsProperties<PropertyValue> axioms = axiomsHandler.handle(
              annotationProperty, ontology);

          List<PropertyValue> subElements =
              extractSubAndSuper.getSuperElements(annotationProperty, ontology,
                  AXIOM_ANNOTATION_PROPERTY);
          OwlTaxonomyImpl taxonomy =
              taxonomyExtractor.extractTaxonomy(subElements, iri, ontology,
                  AXIOM_ANNOTATION_PROPERTY);
          taxonomy.sort();

          OwlDetailsProperties<PropertyValue> annotations =
              handleAnnotations(annotationProperty.getIRI(), ontology, resultDetails);

          var qname = qnameHandler.getQName(iri);
          resultDetails.setqName(qname);
          resultDetails.addAllProperties(annotations);
          resultDetails.addAllProperties(directSubAnnotationProperty);
          resultDetails.addAllProperties(axioms);
          resultDetails.setTaxonomy(taxonomy);
          resultDetails.addAllProperties(licenseHandler.getLicense(iri));
          resultDetails.addAllProperties(copyrightHandler.getCopyright(iri));
        }
      }
    } catch (OntoViewerException ex) {
      LOG.warn("Unable to handle annotation property {}. Details: {}", iri, ex.getMessage());
    }

    return resultDetails;
  }

  /**
   * This method is used to display sub-annotation property
   *
   * @param ontology           This is a loaded ontology.
   * @param annotationProperty AnnotationProperty are all properties of direct
   *                           subAnnotationProperty.
   * @return Properties of direct subAnnotationProperty.
   */
  public OwlDetailsProperties<PropertyValue> handleDirectSubAnnotationProperty(OWLOntology ontology,
      OWLAnnotationProperty annotationProperty) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();

    //  Iterator<OWLSubAnnotationPropertyOfAxiom> iterator = ontology.subAnnotationPropertyOfAxioms(annotationProperty).iterator();
    Iterator<OWLAnnotationProperty> iterator = EntitySearcher.getSubProperties(annotationProperty,
        ontology).iterator();
    while (iterator.hasNext()) {

      LOG.debug("OwlDataHandler -> handleDirectSubAnnotationProperty {}", iterator.hasNext());
      OWLAnnotationProperty next = iterator.next();

      IRI iri = next.getIRI();

      OwlDirectedSubClassesProperty r = new OwlDirectedSubClassesProperty();

      r.setType(OwlType.DIRECT_SUBCLASSES);
      r.setValue(new Pair(labelProvider.getLabelOrDefaultFragment(iri), iri.toString()));

      String key = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.function,
          OwlType.DIRECT_SUB_ANNOTATION_PROPERTY.name().toLowerCase());
      result.addProperty(key, r);
    }
    result.sortPropertiesInAlphabeticalOrder();
    return result;
  }
}
