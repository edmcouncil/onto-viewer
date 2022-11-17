package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.data;

import static org.edmcouncil.spec.ontoviewer.core.model.OwlType.AXIOM_OBJECT_PROPERTY;

import java.util.List;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.Pair;
import org.edmcouncil.spec.ontoviewer.core.exception.OntoViewerException;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlListDetails;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDirectedSubClassesProperty;
import org.edmcouncil.spec.ontoviewer.core.model.taxonomy.OwlTaxonomyImpl;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.CopyrightHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.extractor.TaxonomyExtractor;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.LicenseHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.QnameHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.StringIdentifier;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom.AxiomsHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.classes.ClassDataHelper;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.edmcouncil.spec.ontoviewer.core.service.EntitiesCacheService;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ObjectDataHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DataTypeHandler.class);
  private final EntitiesCacheService entitiesCacheService;
  private final LabelProvider labelProvider;
  private final AxiomsHandler axiomsHandler;
  private final OntologyManager ontologyManager;
  private final QnameHandler qnameHandler;
  private final LicenseHandler licenseHandler;
  private final CopyrightHandler copyrightHandler;
  private final AnnotationsDataHandler annotationsDataHandler;
  private final ClassDataHelper extractSubAndSuper;
  private final TaxonomyExtractor taxonomyExtractor;

  public ObjectDataHandler(EntitiesCacheService entitiesCacheService, LabelProvider labelProvider,
      AxiomsHandler axiomsHandler, OntologyManager ontologyManager, QnameHandler qnameHandler,
      LicenseHandler licenseHandler, CopyrightHandler copyrightHandler,
      AnnotationsDataHandler annotationsDataHandler, ClassDataHelper extractSubAndSuper,
      TaxonomyExtractor taxonomyExtractor) {
    this.entitiesCacheService = entitiesCacheService;
    this.labelProvider = labelProvider;
    this.axiomsHandler = axiomsHandler;
    this.ontologyManager = ontologyManager;
    this.qnameHandler = qnameHandler;
    this.licenseHandler = licenseHandler;
    this.copyrightHandler = copyrightHandler;
    this.annotationsDataHandler = annotationsDataHandler;
    this.extractSubAndSuper = extractSubAndSuper;
    this.taxonomyExtractor = taxonomyExtractor;
  }

  public OwlListDetails handle(IRI iri) {
    OwlListDetails resultDetails = new OwlListDetails();

    var entityEntry = entitiesCacheService.getEntityEntry(iri, OwlType.OBJECT_PROPERTY);

    try {
      if (entityEntry.isPresent()) {
        var objectProperty = entityEntry.getEntityAs(OWLObjectProperty.class);

        resultDetails = handle(objectProperty);
      }
    } catch (OntoViewerException ex) {
      LOG.warn("Unable to handle object property {}. Details: {}", iri, ex.getMessage());
    }

    return resultDetails;
  }

  public OwlListDetails handle(OWLObjectProperty objectProperty) {
    var ontology = ontologyManager.getOntology();
    var iri = objectProperty.getIRI();
    var resultDetails = new OwlListDetails();

    try {
      resultDetails.setLabel(labelProvider.getLabelOrDefaultFragment(objectProperty.getIRI()));

      OwlDetailsProperties<PropertyValue> axioms = axiomsHandler.handle(objectProperty,
          ontology);
      OwlDetailsProperties<PropertyValue> directSubObjectProperty =
          handleDirectSubObjectProperty(ontology, objectProperty);

      List<PropertyValue> subElements =
          extractSubAndSuper.getSuperElements(objectProperty, ontology, AXIOM_OBJECT_PROPERTY);
      OwlTaxonomyImpl taxonomy =
          taxonomyExtractor.extractTaxonomy(subElements, iri, ontology, AXIOM_OBJECT_PROPERTY);
      taxonomy.sort();

      subElements = subElements.stream()
          .filter(pv -> (!pv.getType().equals(OwlType.TAXONOMY)))
          .collect(Collectors.toList());

      OwlDetailsProperties<PropertyValue> annotations =
          annotationsDataHandler.handleAnnotations(objectProperty.getIRI(), ontology,
              resultDetails);

      for (PropertyValue subElement : subElements) {
        axioms.addProperty(StringIdentifier.subObjectPropertyOfIriString, subElement);
      }
      var qname = qnameHandler.getQName(iri);
      resultDetails.setqName(qname);
      resultDetails.addAllProperties(axioms);
      resultDetails.addAllProperties(annotations);
      resultDetails.addAllProperties(directSubObjectProperty);
      resultDetails.setTaxonomy(taxonomy);
      resultDetails.addAllProperties(licenseHandler.getLicense(iri));
      resultDetails.addAllProperties(copyrightHandler.getCopyright(iri));
    } catch (Exception ex) {
      LOG.warn("Unable to handle object property " + iri + ". Details: " + ex.getMessage());
    }
    return resultDetails;
  }


  /**
   * This method is used to display sub-object property
   *
   * @param ontology This is a loaded ontology.
   * @param obj      Obj are all properties of direct subObjectProperty.
   * @return Properties of direct subObjectProperty.
   */
  public OwlDetailsProperties<PropertyValue> handleDirectSubObjectProperty(OWLOntology ontology,
      OWLObjectProperty obj) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();

    var subObjectPropertyAxioms = ontology.importsClosure()
        .flatMap(currentOntology -> currentOntology.objectSubPropertyAxiomsForSuperProperty(obj))
        .collect(Collectors.toSet());

    for (OWLSubObjectPropertyOfAxiom next : subObjectPropertyAxioms) {
      IRI iri = next.getSubProperty().asOWLObjectProperty().getIRI();
      LOG.debug("OwlDataHandler -> handleDirectSubObjectProperty2 {}", iri.toString());

      OwlDirectedSubClassesProperty r = new OwlDirectedSubClassesProperty();

      r.setType(OwlType.DIRECT_SUBCLASSES);
      r.setValue(new Pair(labelProvider.getLabelOrDefaultFragment(iri), iri.toString()));

      LOG.debug("OwlDataHandler -> handleDirectSubObjectProperty3 {}", r);
      String key = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.function,
          OwlType.DIRECT_SUB_OBJECT_PROPERTY.name().toLowerCase());
      result.addProperty(key, r);
    }
    result.sortPropertiesInAlphabeticalOrder();
    return result;
  }
}
