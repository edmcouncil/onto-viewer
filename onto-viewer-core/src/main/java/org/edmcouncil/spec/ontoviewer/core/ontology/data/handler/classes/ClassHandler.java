package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.classes;

import static org.edmcouncil.spec.ontoviewer.core.model.OwlType.AXIOM_CLASS;

import java.util.List;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.Pair;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.RestrictionGraphDataHandler;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.exception.OntoViewerException;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlListDetails;
import org.edmcouncil.spec.ontoviewer.core.model.graph.OntologyGraph;
import org.edmcouncil.spec.ontoviewer.core.model.graph.viewer.ViewerGraphFactory;
import org.edmcouncil.spec.ontoviewer.core.model.graph.vis.VisGraph;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.model.taxonomy.OwlTaxonomyImpl;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.InferableRestrictionHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.StringIdentifier;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom.AxiomsHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.CopyrightHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.axiom.InheritedAxiomsHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.extractor.UsageExtractor;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.QnameHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.data.AnnotationsDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.individual.IndividualDataHelper;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.LicenseHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.extractor.TaxonomyExtractor;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.service.EntitiesCacheService;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.util.OWLAPIStreamUtils;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
@Component
public class ClassHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ClassHandler.class);

  private final LabelProvider labelProvider;
  private final RestrictionGraphDataHandler graphDataHandler;
  private final ApplicationConfigurationService applicationConfigurationService;
  private final EntitiesCacheService entitiesCacheService;
  private final OntologyManager ontologyManager;
  private final LicenseHandler licenseHandler;
  private final CopyrightHandler copyrightHandler;
  private final AxiomsHandler axiomsHandler;
  private final UsageExtractor usageExtractor;
  private final TaxonomyExtractor taxonomyExtractor;
  private final ClassDataHelper extractSubAndSuper;
  private final QnameHandler qnameHandler;
  private final AnnotationsDataHandler particularAnnotationPropertyHandler;
  private final IndividualDataHelper individualDataHelper;
  private final InheritedAxiomsHandler inheritedAxiomsHandler;
  private final InferableRestrictionHandler inferableRestrictionHandler;

  public ClassHandler(LabelProvider labelProvider, RestrictionGraphDataHandler graphDataHandler,
      ApplicationConfigurationService applicationConfigurationService,
      EntitiesCacheService entitiesCacheService, OntologyManager ontologyManager,
      LicenseHandler licenseHandler, CopyrightHandler copyrightHandler,
      AxiomsHandler axiomsHandler, UsageExtractor usageExtractor,
      TaxonomyExtractor taxonomyExtractor, ClassDataHelper extractSubAndSuper,
      QnameHandler qnameHandler, AnnotationsDataHandler particularAnnotationPropertyHandler,
      IndividualDataHelper individualDataHelper, InheritedAxiomsHandler inheritedAxiomsHandler,
      InferableRestrictionHandler inferableRestrictionHandler) {
    this.labelProvider = labelProvider;
    this.graphDataHandler = graphDataHandler;
    this.applicationConfigurationService = applicationConfigurationService;
    this.entitiesCacheService = entitiesCacheService;
    this.ontologyManager = ontologyManager;
    this.licenseHandler = licenseHandler;
    this.copyrightHandler = copyrightHandler;
    this.axiomsHandler = axiomsHandler;
    this.usageExtractor = usageExtractor;
    this.taxonomyExtractor = taxonomyExtractor;
    this.extractSubAndSuper = extractSubAndSuper;
    this.qnameHandler = qnameHandler;
    this.particularAnnotationPropertyHandler = particularAnnotationPropertyHandler;
    this.individualDataHelper = individualDataHelper;
    this.inheritedAxiomsHandler = inheritedAxiomsHandler;
    this.inferableRestrictionHandler = inferableRestrictionHandler;
  }

  public OwlListDetails handle(OWLClass owlClass) {
    var configurationData = applicationConfigurationService.getConfigurationData();

    var ontology = ontologyManager.getOntology();
    var classIri = owlClass.getIRI();
    var resultDetails = new OwlListDetails();

    try {
      resultDetails.setLabel(labelProvider.getLabelOrDefaultFragment(owlClass));

      OwlDetailsProperties<PropertyValue> axioms = axiomsHandler.handle(owlClass, ontology, true);
      List<PropertyValue> subclasses = extractSubAndSuper.getSubclasses(axioms);
      List<PropertyValue> superClasses = extractSubAndSuper.getSuperClasses(owlClass);
      List<PropertyValue> taxElements2 = taxonomyExtractor.extractTaxonomyElements(superClasses);

      OwlDetailsProperties<PropertyValue> directSubclasses = extractSubAndSuper.handleDirectSubclasses(owlClass);
      
      OwlDetailsProperties<PropertyValue> individuals = new OwlDetailsProperties<>();
      if (configurationData.getToolkitConfig().isIndividualsEnabled()) {
        individuals = individualDataHelper.handleInstances(ontology, owlClass); 
      }

      OwlDetailsProperties<PropertyValue> usage = new OwlDetailsProperties<>();
      if (configurationData.getToolkitConfig().isUsageEnabled()) {
        usage = usageExtractor.extractUsageForClasses(owlClass, ontology);
      }

      OwlDetailsProperties<PropertyValue> inheritedAxioms = inheritedAxiomsHandler.handle(ontology, owlClass);

      OntologyGraph ontologyGraph = new OntologyGraph(0);
      if (configurationData.getToolkitConfig().isOntologyGraphEnabled()) {
        // 'Nothing' has all restrictions, we don't want to display that.
        if (!owlClass.getIRI().equals(OWLRDFVocabulary.OWL_NOTHING.getIRI())) {
          ontologyGraph = graphDataHandler.handleGraph(owlClass, ontology, 0, 0);
        }
      }
      subclasses = extractSubAndSuper.filterSubclasses(subclasses);

      OwlTaxonomyImpl taxonomy = taxonomyExtractor.extractTaxonomy(
          taxElements2,
          owlClass.getIRI(),
          ontology,
          AXIOM_CLASS);
      taxonomy.sort();

      OwlDetailsProperties<PropertyValue> annotations =
          particularAnnotationPropertyHandler.handleAnnotations(
              owlClass.getIRI(),
              ontology,
              resultDetails);

      for (PropertyValue subclass : subclasses) {
        axioms.addProperty(StringIdentifier.subClassOfIriString, subclass);
      }
      resultDetails.setTaxonomy(taxonomy);
      resultDetails.addAllProperties(axioms);
      resultDetails.addAllProperties(annotations);
      resultDetails.addAllProperties(directSubclasses);
      resultDetails.addAllProperties(individuals);
      resultDetails.addAllProperties(inheritedAxioms);
      resultDetails.addAllProperties(usage);
      resultDetails.addAllProperties(copyrightHandler.getCopyright(classIri));
      resultDetails.addAllProperties(licenseHandler.getLicense(classIri));
      resultDetails.setqName(qnameHandler.getQName(classIri));

      inferableRestrictionHandler.markInferableRestrictions(resultDetails.getProperties());

      if (ontologyGraph.isEmpty()) {
        resultDetails.setGraph(null);
      } else {
        VisGraph vgj = new ViewerGraphFactory().convertToVisGraph(ontologyGraph);
        resultDetails.setGraph(vgj);
      }
    } catch (Exception ex) {
      LOG.warn("Unable to handle class {}. Details: {}", classIri, ex.getMessage(), ex);
    }
    return resultDetails;
  }


  public OwlListDetails handle(IRI classIri) {
    var resultDetails = new OwlListDetails();
    var entityEntry = entitiesCacheService.getEntityEntry(classIri, OwlType.CLASS);

    try { 
      if (entityEntry != null && entityEntry.isPresent()) {
        var owlClass = entityEntry.getEntityAs(OWLClass.class);
        resultDetails = handle(owlClass);
      } else {
        LOG.warn("Entity with IRI '{}' not found (is NULL or not present: {}).", classIri, entityEntry);
      }
    } catch (OntoViewerException ex) {
      LOG.warn("Unable to handle class {}. Details: {}", classIri, ex.getMessage(), ex);
    }
    return resultDetails;
  }

  
  public OwlListDetails handle(OWLAnonymousIndividual anonymousIndividual) {

    var resultDetails = new OwlListDetails();

    resultDetails = handleAnonymousIndividual(anonymousIndividual);
 
    return resultDetails;
  }


  public OwlListDetails handleAnonymousIndividual(OWLAnonymousIndividual owlAnonymousIndividual) {
    var configurationData = applicationConfigurationService.getConfigurationData();

    var ontology = ontologyManager.getOntology();
   
    var resultDetails = new OwlListDetails();

    resultDetails.setLabel(labelProvider.getLabelOrDefaultFragment(owlAnonymousIndividual));

    OwlDetailsProperties<PropertyValue> axioms = axiomsHandler.handle(owlAnonymousIndividual, ontology, true);
    
    List<PropertyValue> subclasses = extractSubAndSuper.getSubclasses(axioms);

    OwlDetailsProperties<PropertyValue> directSubclasses = extractSubAndSuper.handleDirectSubclasses(owlAnonymousIndividual);
    
    OntologyGraph ontologyGraph = new OntologyGraph(0);
    if (configurationData.getToolkitConfig().isOntologyGraphEnabled()) {
      ontologyGraph = graphDataHandler.handleGraph(owlAnonymousIndividual, ontology, 0, 0);
    }

    subclasses = extractSubAndSuper.filterSubclasses(subclasses);

    for (PropertyValue subclass : subclasses) {
      axioms.addProperty(StringIdentifier.subClassOfIriString, subclass);
    }

    OwlDetailsProperties<PropertyValue> annotations =
            particularAnnotationPropertyHandler.handleAnnotations(
                    owlAnonymousIndividual,
                    ontology,
                    resultDetails);
    
    resultDetails.addAllProperties(axioms);
    resultDetails.addAllProperties(annotations);
    resultDetails.addAllProperties(directSubclasses);

    IRI classIri = getIri(resultDetails);
    if (classIri != null) {
      resultDetails.addAllProperties(copyrightHandler.getCopyright(classIri));
      resultDetails.addAllProperties(licenseHandler.getLicense(classIri));
      resultDetails.setqName(qnameHandler.getQName(classIri));
    }
    
    if (ontologyGraph.isEmpty()) {
      resultDetails.setGraph(null);
    } else {
      VisGraph vgj = new ViewerGraphFactory().convertToVisGraph(ontologyGraph);
      resultDetails.setGraph(vgj);
    }

    return resultDetails;
  }

  private static IRI getIri(OwlListDetails resultDetails) {
    String iri = ((Pair) resultDetails.getAllProperties().getProperties().get("@viewer.function.direct_types").get(0).getValue()).getIri();
    if (iri != null) {
      return IRI.create(iri);
    } else {
      return null;
    }
  }
}
