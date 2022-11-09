package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.data;

import static org.edmcouncil.spec.ontoviewer.core.model.OwlType.AXIOM_DATA_PROPERTY;

import java.util.Iterator;
import java.util.List;
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
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.StringIdentifier;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.classes.ClassDataHelper;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.LicenseHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.extractor.TaxonomyExtractor;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.QnameHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom.AxiomsHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.edmcouncil.spec.ontoviewer.core.service.EntitiesCacheService;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataPropertyHandler {

  private static final Logger LOG = LoggerFactory.getLogger(DataTypeHandler.class);

  private final EntitiesCacheService entitiesCacheService;
  private final LabelProvider labelProvider;
  private final AxiomsHandler axiomsHandler;
  private final OntologyManager ontologyManager;
  private final QnameHandler qnameHandler;
  private final LicenseHandler licenseHandler;
  private final CopyrightHandler copyrightHandler;
  private final AnnotationsDataHandler particularAnnotationPropertyHandler;
  private final ClassDataHelper extractSubAndSuper;
  private final TaxonomyExtractor taxonomyExtractor;

  public DataPropertyHandler(EntitiesCacheService entitiesCacheService, LabelProvider labelProvider,
      AxiomsHandler axiomsHandler, OntologyManager ontologyManager, QnameHandler qnameHandler,
      LicenseHandler licenseHandler, CopyrightHandler copyrightHandler,
      AnnotationsDataHandler particularAnnotationPropertyHandler,
      ClassDataHelper extractSubAndSuper,
      TaxonomyExtractor taxonomyExtractor) {
    this.entitiesCacheService = entitiesCacheService;
    this.labelProvider = labelProvider;
    this.axiomsHandler = axiomsHandler;
    this.ontologyManager = ontologyManager;
    this.qnameHandler = qnameHandler;
    this.licenseHandler = licenseHandler;
    this.copyrightHandler = copyrightHandler;
    this.particularAnnotationPropertyHandler = particularAnnotationPropertyHandler;
    this.extractSubAndSuper = extractSubAndSuper;
    this.taxonomyExtractor = taxonomyExtractor;
  }

  public OwlListDetails handleParticularDataProperty(IRI iri) {
    OwlListDetails resultDetails = new OwlListDetails();

    var entityEntry = entitiesCacheService.getEntityEntry(iri, OwlType.DATA_PROPERTY);

    try {
      if (entityEntry.isPresent()) {
        var dataProperty = entityEntry.getEntityAs(OWLDataProperty.class);

        resultDetails = handleParticularDataProperty(dataProperty);
      }
    } catch (OntoViewerException ex) {
      LOG.warn("Unable to handle data property {}. Details: {}", iri, ex.getMessage());
    }

    return resultDetails;
  }

  public OwlListDetails handleParticularDataProperty(OWLDataProperty dataProperty) {
    var ontology = ontologyManager.getOntology();
    var resultDetails = new OwlListDetails();
    var iri = dataProperty.getIRI();

    try {
      resultDetails.setLabel(labelProvider.getLabelOrDefaultFragment(dataProperty.getIRI()));

      OwlDetailsProperties<PropertyValue> axioms = axiomsHandler.handleAxioms(dataProperty,
          ontology);
      OwlDetailsProperties<PropertyValue> directSubDataProperty = handleDirectSubDataProperty(
          ontology, dataProperty);

      List<PropertyValue> subElements =
          extractSubAndSuper.getSuperElements(dataProperty, ontology, AXIOM_DATA_PROPERTY);
      OwlTaxonomyImpl taxonomy =
          taxonomyExtractor.extractTaxonomy(subElements, iri, ontology, AXIOM_DATA_PROPERTY);
      taxonomy.sort();

      OwlDetailsProperties<PropertyValue> annotations =
          particularAnnotationPropertyHandler.handleAnnotations(dataProperty.getIRI(), ontology,
              resultDetails);
      var qname = qnameHandler.getQName(iri);
      resultDetails.setqName(qname);
      resultDetails.addAllProperties(axioms);
      resultDetails.addAllProperties(annotations);
      resultDetails.addAllProperties(directSubDataProperty);
      resultDetails.setTaxonomy(taxonomy);
      resultDetails.addAllProperties(licenseHandler.getLicense(iri));
      resultDetails.addAllProperties(copyrightHandler.getCopyright(iri));
    } catch (Exception ex) {
      LOG.warn("Unable to handle data property {}. Details: {}", iri, ex.getMessage());
    }

    return resultDetails;
  }

  /**
   * This method is used to display sub-data property
   *
   * @param ontology This is a loaded ontology.
   * @param odj      Odj are all properties of direct subDataProperty.
   * @return Properties of direct subDataProperty.
   */
  public OwlDetailsProperties<PropertyValue> handleDirectSubDataProperty(OWLOntology ontology,
      OWLDataProperty odj) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();

    Iterator<OWLDataProperty> iterator = EntitySearcher.getSubProperties(odj, ontology).iterator();

    while (iterator.hasNext()) {
      LOG.debug("OwlDataHandler -> handleDirectSubDataProperty {}", iterator.hasNext());
      OWLDataProperty next = iterator.next();

      IRI iri = next.getIRI();

      OwlDirectedSubClassesProperty r = new OwlDirectedSubClassesProperty();

      r.setType(OwlType.DIRECT_SUBCLASSES);
      r.setValue(new Pair(labelProvider.getLabelOrDefaultFragment(iri), iri.toString()));

      String key = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.function,
          OwlType.DIRECT_SUB_DATA_PROPERTY.name().toLowerCase());
      result.addProperty(key, r);
    }
    result.sortPropertiesInAlphabeticalOrder();
    return result;
  }
}
