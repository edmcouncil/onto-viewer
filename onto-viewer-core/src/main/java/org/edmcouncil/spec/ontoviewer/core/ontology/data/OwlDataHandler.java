package org.edmcouncil.spec.ontoviewer.core.ontology.data;

import static org.edmcouncil.spec.ontoviewer.core.model.OwlType.AXIOM_ANNOTATION_PROPERTY;
import static org.edmcouncil.spec.ontoviewer.core.model.OwlType.AXIOM_CLASS;
import static org.edmcouncil.spec.ontoviewer.core.model.OwlType.AXIOM_DATA_PROPERTY;
import static org.edmcouncil.spec.ontoviewer.core.model.OwlType.AXIOM_OBJECT_PROPERTY;
import static org.edmcouncil.spec.ontoviewer.core.model.OwlType.TAXONOMY;
import static org.semanticweb.owlapi.model.parameters.Imports.INCLUDED;

import com.github.jsonldjava.shaded.com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.Pair;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.exception.OntoViewerException;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlListDetails;
import org.edmcouncil.spec.ontoviewer.core.model.graph.OntologyGraph;
import org.edmcouncil.spec.ontoviewer.core.model.graph.viewer.ViewerGraphFactory;
import org.edmcouncil.spec.ontoviewer.core.model.graph.vis.VisGraph;
import org.edmcouncil.spec.ontoviewer.core.model.module.OntologyModule;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyEntity;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAxiomPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDirectedSubClassesProperty;
import org.edmcouncil.spec.ontoviewer.core.model.taxonomy.OwlTaxonomyElementImpl;
import org.edmcouncil.spec.ontoviewer.core.model.taxonomy.OwlTaxonomyImpl;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.AnnotationsDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.DataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.IndividualDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.ModuleHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevel;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.visitor.ContainsVisitors;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.edmcouncil.spec.ontoviewer.core.ontology.scope.ScopeIriOntology;
import org.edmcouncil.spec.ontoviewer.core.service.EntitiesCacheService;
import org.edmcouncil.spec.ontoviewer.core.utils.OwlUtils;
import org.edmcouncil.spec.ontoviewer.core.utils.StringUtils;
import org.edmcouncil.spec.ontoviewer.core.utils.UrlChecker;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
@Component
public class OwlDataHandler {

  private static final Logger LOG = LoggerFactory.getLogger(OwlDataHandler.class);

  private final OWLObjectRenderer rendering = new ManchesterOWLSyntaxOWLObjectRendererImpl();
  @Autowired
  private DataHandler dataHandler;
  @Autowired
  private ModuleHandler moduleHandler;
  @Autowired
  private AnnotationsDataHandler annotationsDataHandler;
  @Autowired
  private IndividualDataHandler individualDataHandler;
  @Autowired
  private LabelProvider labelProvider;
  @Autowired
  private RestrictionGraphDataHandler graphDataHandler;
  @Autowired
  private OwlUtils owlUtils;
  @Autowired
  private ApplicationConfigurationService applicationConfigurationService;
  @Autowired
  private ScopeIriOntology scopeIriOntology;
  @Autowired
  private ContainsVisitors containsVisitors;
  @Autowired
  private EntitiesCacheService entitiesCacheService;
  @Autowired
  private OntologyManager ontologyManager;

  private final Set<String> unwantedEndOfLeafIri = new HashSet<>();
  private final Set<String> unwantedTypes = new HashSet<>();
  private final Set<String> SUBJECTS_TO_HIDE = ImmutableSet.of("SubClassOf", "Domain", "Range", "SubPropertyOf:",
      "Range:", "Functional:", "Transitive:", "Symmetric:", "Asymmetric", "Reflexive", "Irreflexive");
  private final String INVERSE_OF_SUBJECT = "InverseOf";

  private final String subClassOfIriString = ViewerIdentifierFactory
      .createId(ViewerIdentifierFactory.Type.axiom, AxiomType.SUBCLASS_OF.getName());
  private final String subObjectPropertyOfIriString = ViewerIdentifierFactory
      .createId(ViewerIdentifierFactory.Type.axiom, AxiomType.SUB_OBJECT_PROPERTY.getName());

  {
    /*static block*/
    unwantedEndOfLeafIri.add("http://www.w3.org/2002/07/owl#Thing");
    unwantedEndOfLeafIri.add("http://www.w3.org/2002/07/owl#topObjectProperty");
    unwantedEndOfLeafIri.add("http://www.w3.org/2002/07/owl#topDataProperty");

    unwantedTypes.add("^^anyURI");
    unwantedTypes.add("^^dateTime");
  }

  public OwlListDetails handleParticularClass(OWLClass owlClass) {
    var configurationData = applicationConfigurationService.getConfigurationData();

    var ontology = ontologyManager.getOntology();
    var classIri = owlClass.getIRI();
    var resultDetails = new OwlListDetails();

    try {
      resultDetails.setLabel(labelProvider.getLabelOrDefaultFragment(owlClass));

      OwlDetailsProperties<PropertyValue> axioms = handleAxioms(owlClass, ontology);
      List<PropertyValue> subclasses = getSubclasses(axioms);
      List<PropertyValue> subclasses2 = getSubclasses(owlClass);
      List<PropertyValue> taxElements2 = extractTaxonomyElements(subclasses2);

      OwlDetailsProperties<PropertyValue> directSubclasses = handleDirectSubclasses(owlClass);
      OwlDetailsProperties<PropertyValue> individuals = new OwlDetailsProperties<>();
      if (configurationData.getToolkitConfig().isIndividualsEnabled()) {
        individuals = handleInstances(ontology, owlClass);
      }

      OwlDetailsProperties<PropertyValue> usage = new OwlDetailsProperties<>();
      if (configurationData.getToolkitConfig().isUsageEnabled()) {
        usage = extractUsageForClasses(owlClass, ontology);
      }

      OwlDetailsProperties<PropertyValue> inheritedAxioms =
          handleInheritedAxioms(ontology, owlClass);

      OntologyGraph ontologyGraph = new OntologyGraph();
      if (configurationData.getToolkitConfig().isOntologyGraphEnabled()) {
        // 'Nothing' has all restrictions, we don't want to display that.
        if (!owlClass.getIRI().equals(OWLRDFVocabulary.OWL_NOTHING.getIRI())) {
          ontologyGraph = graphDataHandler.handleGraph(owlClass, ontology);
        }
      }

      subclasses = filterSubclasses(subclasses);

      OwlTaxonomyImpl taxonomy = extractTaxonomy(taxElements2, owlClass.getIRI(), ontology,
          AXIOM_CLASS);
      taxonomy.sort();

      OwlDetailsProperties<PropertyValue> annotations =
          handleAnnotations(owlClass.getIRI(), ontology, resultDetails);

      setResultValues(resultDetails, taxonomy, axioms, annotations, directSubclasses, individuals,
          inheritedAxioms, usage, ontologyGraph, subclasses);
    } catch (Exception ex) {
      LOG.warn("Unable to handle class {}. Details: {}", classIri, ex.getMessage(), ex);
    }

    return resultDetails;
  }

  public OwlListDetails handleParticularClass(IRI classIri) {
    var resultDetails = new OwlListDetails();

    var entityEntry = entitiesCacheService.getEntityEntry(classIri, OwlType.CLASS);

    try {
      if (entityEntry != null && entityEntry.isPresent()) {
        var owlClass = entityEntry.getEntityAs(OWLClass.class);

        resultDetails = handleParticularClass(owlClass);
      } else {
        LOG.warn("Entity with IRI '{}' not found (is NULL or not present: {}).",
            classIri, entityEntry);
      }
    } catch (OntoViewerException ex) {
      LOG.warn("Unable to handle class {}. Details: {}", classIri, ex.getMessage(), ex);
    }

    return resultDetails;
  }

  private List<PropertyValue> filterSubclasses(List<PropertyValue> subclasses) {
    List<PropertyValue> result = subclasses.stream()
        .filter((pv) -> (!pv.getType().equals(OwlType.TAXONOMY)))
        .collect(Collectors.toList());
    return result;
  }

  private List<PropertyValue> getSubclasses(OwlDetailsProperties<PropertyValue> axioms) {
    return axioms
        .getProperties()
        .getOrDefault(subClassOfIriString, new ArrayList<>(0));
  }

  private List<PropertyValue> getSubclasses(OWLClass clazz) {
    List<OWLClassExpression> subClasses = EntitySearcher
        .getSuperClasses(clazz, ontologyManager.getOntologyWithImports())
        .collect(Collectors.toList());

    List<PropertyValue> result = new LinkedList<>();
    for (OWLClassExpression subClassExpression : subClasses) {
      Optional<OWLEntity> entityOptional = subClassExpression.signature().findFirst();

      if (subClassExpression.getClassExpressionType() != ClassExpressionType.OWL_CLASS ||
          entityOptional.isEmpty() ||
          entityOptional.get().getIRI().equals(clazz.getIRI())) {
        continue;
      }

      var entityIri = entityOptional.get().getIRI();

      String key = StringUtils.getIdentifier(entityIri);

      OwlAxiomPropertyValue axiomPropertyValue = new OwlAxiomPropertyValue();
      axiomPropertyValue.setType(OwlType.TAXONOMY);
      axiomPropertyValue.setValue(key);

      OwlAxiomPropertyEntity propertyEntity = new OwlAxiomPropertyEntity(
          entityIri.toString(),
          labelProvider.getLabelOrDefaultFragment(entityIri));
      axiomPropertyValue.addEntityValues(key, propertyEntity);
      result.add(axiomPropertyValue);
    }

    return result.stream().distinct().collect(Collectors.toList());
  }

  private List<PropertyValue> extractTaxonomyElements(List<PropertyValue> subclasses) {
    return subclasses
        .stream()
        .filter(pv -> (pv.getType().equals(OwlType.TAXONOMY)))
        .distinct()
        .collect(Collectors.toList());
  }

  private void setResultValues(OwlListDetails resultDetails,
      OwlTaxonomyImpl taxonomy,
      OwlDetailsProperties<PropertyValue> axioms,
      OwlDetailsProperties<PropertyValue> annotations,
      OwlDetailsProperties<PropertyValue> directSubclasses,
      OwlDetailsProperties<PropertyValue> individuals,
      OwlDetailsProperties<PropertyValue> inheritedAxioms,
      OwlDetailsProperties<PropertyValue> usage,
      OntologyGraph ontologyGraph,
      List<PropertyValue> subclasses) {
    for (PropertyValue subclass : subclasses) {
      axioms.addProperty(subClassOfIriString, subclass);
    }

    resultDetails.setTaxonomy(taxonomy);
    resultDetails.addAllProperties(axioms);
    resultDetails.addAllProperties(annotations);
    resultDetails.addAllProperties(directSubclasses);
    resultDetails.addAllProperties(individuals);
    resultDetails.addAllProperties(inheritedAxioms);
    resultDetails.addAllProperties(usage);
    if (ontologyGraph.isEmpty()) {
      resultDetails.setGraph(null);
    } else {
      VisGraph vgj = new ViewerGraphFactory().convertToVisGraph(ontologyGraph);
      resultDetails.setGraph(vgj);
    }
  }

  public OwlListDetails handleParticularIndividual(OWLNamedIndividual individual) {
    var ontology = ontologyManager.getOntology();
    var iri = individual.getIRI();

    var resultDetails = new OwlListDetails();

    try {
      resultDetails.setLabel(labelProvider.getLabelOrDefaultFragment(individual.getIRI()));

      OwlDetailsProperties<PropertyValue> axioms = handleAxioms(individual, ontology);

      OwlDetailsProperties<PropertyValue> annotations =
          handleAnnotations(individual.getIRI(), ontology, resultDetails);
      OntologyGraph ontologyGraph = graphDataHandler.handleGraph(individual, ontology);
      if (ontologyGraph.isEmpty()) {
        resultDetails.setGraph(null);
      } else {
        VisGraph vgj = new ViewerGraphFactory().convertToVisGraph(ontologyGraph);
        resultDetails.setGraph(vgj);
      }
      resultDetails.addAllProperties(axioms);
      resultDetails.addAllProperties(annotations);
    } catch (Exception ex) {
      LOG.warn("Unable to handle individual " + iri + ". Details: " + ex.getMessage(), ex);
    }

    return resultDetails;
  }

  public OwlListDetails handleParticularIndividual(IRI iri) {
    OwlListDetails resultDetails = new OwlListDetails();

    var entityEntry = entitiesCacheService.getEntityEntry(iri, OwlType.INDIVIDUAL);

    try {
      if (entityEntry.isPresent()) {
        var individual = entityEntry.getEntityAs(OWLNamedIndividual.class);

        resultDetails = handleParticularIndividual(individual);
      }
    } catch (OntoViewerException ex) {
      LOG.warn("Unable to handle individual {}. Details: {}", iri, ex.getMessage(), ex);
    }

    return resultDetails;
  }

  private OwlDetailsProperties<PropertyValue> handleAnnotations(IRI iri, OWLOntology ontology,
      OwlListDetails details) {
    return annotationsDataHandler.handleAnnotations(iri, ontology, details);
  }

  private OwlDetailsProperties<PropertyValue> handleAxioms(
      OWLNamedIndividual obj,
      OWLOntology ontology) {

    Iterator<OWLIndividualAxiom> axiomsIterator = ontology.axioms(obj, INCLUDED).iterator();
    return handleAxioms(axiomsIterator, obj.getIRI());
  }

  private OwlDetailsProperties<PropertyValue> handleAxioms(
      OWLObjectProperty obj,
      OWLOntology ontology) {

    Iterator<OWLObjectPropertyAxiom> axiomsIterator = ontology.axioms(obj, INCLUDED).iterator();
    return handleAxioms(axiomsIterator, obj.getIRI());
  }

  private OwlDetailsProperties<PropertyValue> handleAxioms(
      OWLDataProperty obj,
      OWLOntology ontology) {
    Iterator<OWLDataPropertyAxiom> axiomsIterator = ontology.axioms(obj, INCLUDED).iterator();
    return handleAxioms(axiomsIterator, obj.getIRI());
  }

  private OwlDetailsProperties<PropertyValue> handleAxioms(OWLClass obj, OWLOntology ontology) {
    Iterator<OWLClassAxiom> axiomsIterator = ontology.axioms(obj, INCLUDED).iterator();
    return handleAxioms(axiomsIterator, obj.getIRI());
  }

  private OwlDetailsProperties<PropertyValue> handleAxioms(
      OWLAnnotationProperty obj,
      OWLOntology ontology) {
    Iterator<OWLAnnotationAxiom> axiomsIterator = ontology.axioms(obj, INCLUDED).iterator();
    return handleAxioms(axiomsIterator, obj.getIRI());
  }

  private OwlTaxonomyImpl extractTaxonomy(List<PropertyValue> subElements, IRI objIri,
      OWLOntology ontology, OwlType type) {
    return extractTaxonomy(subElements, objIri, ontology, type, 0);
  }

  private OwlTaxonomyImpl extractTaxonomy(List<PropertyValue> subElements, IRI objIri,
      OWLOntology ontology, OwlType type, int depth) {
    OwlTaxonomyImpl taxonomy = new OwlTaxonomyImpl();
    if (!subElements.isEmpty()) {
      for (PropertyValue property : subElements) {
        // TODO: Replace this hack with proper handling of circular taxonomies etc.
        if (depth > 40) {
          LOG.debug("Depth > 40 for extracting taxonomy for objIri {} (type: {}) "
              + "and current taxonomy: {}", objIri, type, taxonomy);
          continue;
        }

        if (property.getType().equals(OwlType.TAXONOMY)) {
          OwlAxiomPropertyValue axiomProperty = (OwlAxiomPropertyValue) property;
          LOG.debug("Axiom Property {}", axiomProperty);
          IRI subElementIri = extractSubElementIri(axiomProperty, objIri);

          OWLEntity entity = createEntity(ontology, subElementIri, type);

          List<PropertyValue> subTax = getSuperElements(entity, ontology, type);

          OwlTaxonomyImpl subCLassTax =
              extractTaxonomy(subTax, entity.getIRI(), ontology, type, depth++);

          String label = labelProvider.getLabelOrDefaultFragment(objIri);

          OwlTaxonomyElementImpl taxEl = new OwlTaxonomyElementImpl(objIri.getIRIString(), label);

          if (subCLassTax.getValue().isEmpty()) {
            List<OwlTaxonomyElementImpl> currentTax = new LinkedList<>();
            currentTax.add(taxEl);
            taxonomy.addTaxonomy(currentTax);
          } else {
            taxonomy.addTaxonomy(subCLassTax, taxEl);
          }
        }
      }
    } else {
      LOG.trace("\t\tEnd leaf on {}", StringUtils.getIdentifier(objIri));
      String label = labelProvider.getLabelOrDefaultFragment(objIri);

      OwlTaxonomyElementImpl taxEl = new OwlTaxonomyElementImpl(objIri.getIRIString(), label);
      List<OwlTaxonomyElementImpl> currentTax = new LinkedList<>();

      if (!unwantedEndOfLeafIri.contains(objIri.toString())) {
        switch (type) {
          case AXIOM_CLASS:
            label = labelProvider.getLabelOrDefaultFragment(
                IRI.create("http://www.w3.org/2002/07/owl#Thing"));
            currentTax.add(
                new OwlTaxonomyElementImpl("http://www.w3.org/2002/07/owl#Thing", label));
            break;
          case AXIOM_OBJECT_PROPERTY:
            label = labelProvider.getLabelOrDefaultFragment(
                IRI.create("http://www.w3.org/2002/07/owl#topObjectProperty"));
            currentTax.add(
                new OwlTaxonomyElementImpl("http://www.w3.org/2002/07/owl#topObjectProperty",
                    label));
            break;
          case AXIOM_DATA_PROPERTY:
            label = labelProvider.getLabelOrDefaultFragment(
                IRI.create("http://www.w3.org/2002/07/owl#topDataProperty"));
            currentTax.add(
                new OwlTaxonomyElementImpl("http://www.w3.org/2002/07/owl#topDataProperty", label));
            break;
          case AXIOM_ANNOTATION_PROPERTY:
            label = labelProvider.getLabelOrDefaultFragment(
                IRI.create("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property"));
            currentTax.add(
                new OwlTaxonomyElementImpl("http://www.w3.org/1999/02/22-rdf-syntax-ns#Property",
                    label));
            break;
          case AXIOM_NAMED_INDIVIDUAL:
            break;
          default:
            label = labelProvider.getLabelOrDefaultFragment(
                IRI.create("http://www.w3.org/2002/07/owl#Thing"));
            currentTax.add(
                new OwlTaxonomyElementImpl("http://www.w3.org/2002/07/owl#Thing", label));
            break;
        }
      }
      currentTax.add(taxEl);

      taxonomy.addTaxonomy(currentTax);
    }

    return taxonomy;
  }

  private OWLEntity createEntity(OWLOntology ontology, IRI sci, OwlType type) {
    switch (type) {
      case AXIOM_CLASS:
        return ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(sci);
      case AXIOM_DATA_PROPERTY:
        return ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(sci);
      case AXIOM_OBJECT_PROPERTY:
        return ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(sci);
      case AXIOM_DATATYPE:
        return ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDatatype(sci);
      case AXIOM_ANNOTATION_PROPERTY:
        return ontology.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty(sci);
    }

    return null;
  }

  private IRI extractSubElementIri(OwlAxiomPropertyValue axiomProperty, IRI objIri) {
    LOG.debug("Axiom Property SubElementIri {}", axiomProperty.toString());
    LOG.debug("extractSubElementIri -> obj {}", objIri);
    for (Map.Entry<String, OwlAxiomPropertyEntity> entry : axiomProperty.getEntityMaping()
        .entrySet()) {
      LOG.debug("Axiom Property entry element {}", entry.toString());
      if (!entry.getValue().getIri().equals(objIri.getIRIString())) {
        return IRI.create(entry.getValue().getIri());
      }
    }
    return null;
  }

  private <T extends OWLAxiom> OwlDetailsProperties<PropertyValue> handleAxioms(
      Iterator<T> axiomsIterator,
      IRI elementIri) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();
    String iriFragment = elementIri.getFragment();
    String splitFragment = StringUtils.getIdentifier(elementIri);
    Boolean fixRenderedIri = !iriFragment.equals(splitFragment);

    int start = 0;

    while (axiomsIterator.hasNext()) {
      T axiom = axiomsIterator.next();

      String key = axiom.getAxiomType().getName();
      key = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.axiom, key);

      OwlAxiomPropertyValue opv = prepareAxiomPropertyValue(axiom, iriFragment, splitFragment,
          fixRenderedIri, key, start, true);

      if (opv == null) {
        continue;
      }
      start = opv.getLastId();
      if (!key.equals(subClassOfIriString) || !opv.getType().equals(TAXONOMY)) {
        result.addProperty(key, opv);
      }
    }
    result.sortPropertiesInAlphabeticalOrder();
    return result;
  }

  private <T extends OWLAxiom> OwlAxiomPropertyValue prepareAxiomPropertyValue(
      T axiom,
      String iriFragment,
      String splitFragment,
      Boolean fixRenderedIri,
      String key
  ) {
    return prepareAxiomPropertyValue(axiom, iriFragment, splitFragment, fixRenderedIri, key, 0, true);
  }

  private <T extends OWLAxiom> OwlAxiomPropertyValue prepareAxiomPropertyValue(
      T axiom,
      String iriFragment,
      String splitFragment,
      Boolean fixRenderedIri,
      String key,
      int startCountingArgs,
      boolean bypassClass
  ) {
    String value = rendering.render(axiom);
    LOG.debug("Rendered default value: {}", value);
    for (String unwantedType : unwantedTypes) {
      value = value.replaceAll(unwantedType, "");
    }

    if (bypassClass) {
      value = fixRenderedValue(value, iriFragment, splitFragment, fixRenderedIri);
    }
    OwlAxiomPropertyValue axiomPropertyValue = new OwlAxiomPropertyValue();
    axiomPropertyValue.setValue(value);
    axiomPropertyValue.setType(OwlType.AXIOM);

    boolean isRestriction = owlUtils.isRestriction(axiom);
    if (!isRestriction && axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF)) {
      axiomPropertyValue.setType(OwlType.TAXONOMY);
    }

    processingAxioms(axiom, fixRenderedIri, iriFragment, splitFragment, axiomPropertyValue, value,
        startCountingArgs);

    return axiomPropertyValue;
  }

  //TODO: refactor this method
  private <T extends OWLAxiom> void processingAxioms(
      T axiom,
      Boolean fixRenderedIri,
      String iriFragment,
      String splitFragment,
      OwlAxiomPropertyValue opv,
      String renderedVal,
      int startCountingArgs) {
    String argPattern = "/arg%s/";
    String[] splitted = renderedVal.split(" ");
    String openingBrackets = "(";
    String closingBrackets = ")";
    String openingCurlyBrackets = "{";
    String closingCurlyBrackets = "}";
    String comma = ",";
    Iterator<OWLEntity> iterator = axiom.signature().iterator();
    LOG.trace("Rendered Val: {}", renderedVal);

    while (iterator.hasNext()) {
      OWLEntity next = iterator.next();
      String eSignature = rendering.render(next);
      eSignature = fixRenderedIri && iriFragment.equals(eSignature) ? splitFragment : eSignature;
      String key = null;
      LOG.debug("Processing Item: {}", next);
      LOG.trace("OWL Entity splitted: {}", Arrays.asList(splitted));

      for (int countingArg = startCountingArgs; countingArg < startCountingArgs + splitted.length; countingArg++) {
        int fixedIValue = countingArg - startCountingArgs;
        String string = splitted[fixedIValue].trim();
        LOG.trace("Splitted string i: '{}', str: '{}'", fixedIValue, string);
        //more than 1 because when it's 1, it's a number
        Boolean hasOpeningBrackets = string.length() > 1 ? string.contains("(") : false;
        int countOpeningBrackets = StringUtils.countLetter(string, '(');

        Boolean hasClosingBrackets =
            string.length() > 1 ? string.endsWith(closingBrackets) : false;
        int countClosingBrackets = StringUtils.countLetter(string, ')');

        Boolean hasOpeningCurlyBrackets = string.length() > 1 ? string.contains("{") : false;
        int countOpeningCurlyBrackets = StringUtils.countLetter(string, '{');

        Boolean hasClosingCurlyBrackets = string.length() > 1 ? string.contains("}") : false;
        int countClosingCurlyBrackets = StringUtils.countLetter(string, '}');

        Boolean hasComma = string.length() > 1 ? string.contains(",") : false;
        int countComma = StringUtils.countLetter(string, ',');

        if (hasOpeningBrackets) {
          String newString = string.substring(countOpeningBrackets);
          LOG.trace("Old string: '{}', new string '{}', count opening parenthesis '{}'", string,
              newString,
              countOpeningBrackets);
          string = newString;
        }
        if (hasClosingBrackets) {
          String newString = string.substring(0, string.length() - countClosingBrackets);
          LOG.trace("Old string: '{}', new string '{}', count closing parenthesis '{}'", string,
              newString,
              countClosingBrackets);

          string = newString;
        }
        if (hasOpeningCurlyBrackets) {
          String newString = string.substring(countOpeningCurlyBrackets);
          LOG.trace("Old string: '{}', new string '{}', count opening curly brackets '{}'", string,
              newString,
              countOpeningCurlyBrackets);
          string = newString;
        }
        if (hasClosingCurlyBrackets) {
          String newString = string.substring(0, string.length() - countClosingCurlyBrackets);
          LOG.trace("Old string: '{}', new string '{}', count closing curly brackets '{}'", string,
              newString,
              countClosingCurlyBrackets);

          string = newString;
        }
        if (hasComma) {
          String newString = string.substring(0, string.length() - countComma);
          LOG.trace("Old string: '{}', new string '{}', count comma '{}'", string,
              newString,
              countComma);

          string = newString;
        }
        if (string.equals(eSignature)) {
          LOG.trace("Find match for processing item {}", string);
          String generatedKey = String.format(argPattern, countingArg);
          key = generatedKey;
          String textToReplace = generatedKey;
          if (hasOpeningBrackets) {
            String prefix = String.join("",
                Collections.nCopies(countOpeningBrackets, openingBrackets));
            textToReplace = prefix + textToReplace;
          }
          if (hasClosingBrackets) {
            String postfix = String.join("",
                Collections.nCopies(countClosingBrackets, closingBrackets));
            textToReplace = textToReplace + postfix;
          }
          if (hasOpeningCurlyBrackets) {
            String prefix = String.join("",
                Collections.nCopies(countOpeningCurlyBrackets, openingCurlyBrackets));
            textToReplace = prefix + textToReplace;
          }
          if (hasClosingCurlyBrackets) {
            String postfix = String.join("",
                Collections.nCopies(countClosingCurlyBrackets, closingCurlyBrackets));
            textToReplace = textToReplace + postfix;
          }
          if (hasComma) {
            String postfix = String.join("", Collections.nCopies(countComma, comma));
            textToReplace = textToReplace + postfix;
          }
          LOG.trace("Prepared text: {} for: {}", textToReplace, splitted[fixedIValue]);
          splitted[fixedIValue] = textToReplace;
          String eIri = next.getIRI().toString();

          parseToIri(argPattern, opv, key, splitted, fixedIValue, generatedKey, eIri,
              countOpeningBrackets, countClosingBrackets, countComma);
        }
        opv.setLastId(countingArg);
      }
    }

    checkAndParseUriInLiteral(splitted, argPattern, opv);

    String value = String.join(" ", splitted).trim();

    LOG.debug("[Data Handler] Prepared value for axiom : {}", value);
    opv.setValue(value);
    String fullRenderedString = parseRenderedString(opv);
    opv.setFullRenderedString(fullRenderedString);
    LOG.debug("Full Rendered String: {}", fullRenderedString);
  }

  private void checkAndParseUriInLiteral(String[] splited, String argPattern,
      OwlAxiomPropertyValue opv) {
    for (int j = 0; j < splited.length; j++) {
      String str = splited[j].trim();
      String probablyUrl = splited[j].trim();
      if (str.startsWith("<") && str.endsWith(">")) {
        int length = str.length();
        probablyUrl = str.substring(1, length - 1);
      }
      if (UrlChecker.isUrl(probablyUrl)) {
        String generatedKey = String.format(argPattern, j);
        String key = generatedKey;

        if (scopeIriOntology.scopeIri(probablyUrl)) {
          //Brace checking is not needed here, so the arguments are 0.
          parseToIri(probablyUrl, opv, key, splited, j, generatedKey, str, 0, 0, 0);
        } else {
          parseUrl(probablyUrl, splited, j);
        }
      }
    }
  }

  private void parseUrl(String probablyUrl, String[] splited, int j) {
    String label = labelProvider.getLabelOrDefaultFragment(IRI.create(probablyUrl));
    splited[j] = label;
  }

  private void parseToIri(String probablyUrl, OwlAxiomPropertyValue opv, String key,
      String[] splited, int j, String generatedKey, String iriString, int countOpeningParenthesis,
      int countClosingParenthesis, int countComma) {
    OwlAxiomPropertyEntity axiomPropertyEntity = new OwlAxiomPropertyEntity();
    if (iriString.contains("<") && iriString.contains(">")) {
      iriString = iriString.toString().replace("<", "").replace(">", "");
    }
    axiomPropertyEntity.setIri(iriString);
    LOG.debug("Probably iriString {}", iriString);
    String label = labelProvider.getLabelOrDefaultFragment(IRI.create(iriString));
    axiomPropertyEntity.setLabel(label);
    opv.addEntityValues(key, axiomPropertyEntity);
    splited[j] = generatedKey;

    String textToReplace = generatedKey;

    if (countOpeningParenthesis > 0) {
      String prefix = String.join("", Collections.nCopies(countOpeningParenthesis, "("));
      textToReplace = prefix + textToReplace;
    }
    if (countClosingParenthesis > 0) {
      String postfix = String.join("", Collections.nCopies(countClosingParenthesis, ")"));
      textToReplace = textToReplace + postfix;
    }
    if (countComma > 0) {
      String postfix = String.join("", Collections.nCopies(countComma, ","));
      textToReplace = textToReplace + postfix;
    }
    splited[j] = textToReplace;
  }

  private String fixRenderedValue(String axiomString, String iriFragment, String splitFragment,
      Boolean fixRenderedIri) {
    String[] axiomParts = axiomString.split(" ");
    LOG.debug("Split fixRenderedValue: {}", Arrays.asList(axiomParts));
    Boolean axiomIsInverseOf = INVERSE_OF_SUBJECT.contains(axiomParts[1]);
    Boolean axiomSubject = axiomParts[0].contains(iriFragment);

    if (axiomIsInverseOf) {
      if (axiomSubject) {
        axiomParts[0] = "";
        axiomParts[1] = "";
      } else {
        axiomParts[1] = "";
        axiomParts[2] = "";
      }
    }
    if (SUBJECTS_TO_HIDE.contains(axiomParts[1])) {
      axiomParts[0] = "";
      axiomParts[1] = "";
    }
    if (fixRenderedIri) {
      int iriFragmentIndex = -1;
      for (int i = 0; i < axiomParts.length; i++) {
        String sString = axiomParts[i];
        if (iriFragment.contains(sString)) {
          iriFragmentIndex = i;
          break;
        }
      }
      if (iriFragmentIndex != -1) {
        axiomParts[iriFragmentIndex] = splitFragment;
      }
    }
    axiomString = String.join(" ", axiomParts);
    return axiomString;
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

      OwlDetailsProperties<PropertyValue> axioms = handleAxioms(dataProperty, ontology);
      OwlDetailsProperties<PropertyValue> directSubDataProperty = handleDirectSubDataProperty(
          ontology, dataProperty);

      List<PropertyValue> subElements =
          getSuperElements(dataProperty, ontology, AXIOM_DATA_PROPERTY);
      OwlTaxonomyImpl taxonomy =
          extractTaxonomy(subElements, iri, ontology, AXIOM_DATA_PROPERTY);
      taxonomy.sort();

      OwlDetailsProperties<PropertyValue> annotations =
          handleAnnotations(dataProperty.getIRI(), ontology, resultDetails);

      resultDetails.addAllProperties(axioms);
      resultDetails.addAllProperties(annotations);
      resultDetails.addAllProperties(directSubDataProperty);
      resultDetails.setTaxonomy(taxonomy);
    } catch (Exception ex) {
      LOG.warn("Unable to handle data property {}. Details: {}", iri, ex.getMessage());
    }

    return resultDetails;
  }

  public OwlListDetails handleParticularObjectProperty(IRI iri) {
    OwlListDetails resultDetails = new OwlListDetails();

    var entityEntry = entitiesCacheService.getEntityEntry(iri, OwlType.OBJECT_PROPERTY);

    try {
      if (entityEntry.isPresent()) {
        var objectProperty = entityEntry.getEntityAs(OWLObjectProperty.class);

        resultDetails = handleParticularObjectProperty(objectProperty);
      }
    } catch (OntoViewerException ex) {
      LOG.warn("Unable to handle object property {}. Details: {}", iri, ex.getMessage());
    }

    return resultDetails;
  }

  public OwlListDetails handleParticularObjectProperty(OWLObjectProperty objectProperty) {
    var ontology = ontologyManager.getOntology();
    var iri = objectProperty.getIRI();
    var resultDetails = new OwlListDetails();

    try {
      resultDetails.setLabel(labelProvider.getLabelOrDefaultFragment(objectProperty.getIRI()));

      OwlDetailsProperties<PropertyValue> axioms = handleAxioms(objectProperty, ontology);
      OwlDetailsProperties<PropertyValue> directSubObjectProperty =
          handleDirectSubObjectProperty(ontology, objectProperty);

      List<PropertyValue> subElements =
          getSuperElements(objectProperty, ontology, AXIOM_OBJECT_PROPERTY);
      OwlTaxonomyImpl taxonomy =
          extractTaxonomy(subElements, iri, ontology, AXIOM_OBJECT_PROPERTY);
      taxonomy.sort();

      subElements = subElements.stream()
          .filter(pv -> (!pv.getType().equals(OwlType.TAXONOMY)))
          .collect(Collectors.toList());

      OwlDetailsProperties<PropertyValue> annotations =
          handleAnnotations(objectProperty.getIRI(), ontology, resultDetails);

      for (PropertyValue subElement : subElements) {
        axioms.addProperty(subObjectPropertyOfIriString, subElement);
      }
      resultDetails.addAllProperties(axioms);
      resultDetails.addAllProperties(annotations);
      resultDetails.addAllProperties(directSubObjectProperty);
      resultDetails.setTaxonomy(taxonomy);
    } catch (Exception ex) {
      LOG.warn("Unable to handle object property " + iri + ". Details: " + ex.getMessage());
    }

    return resultDetails;
  }

  private List<PropertyValue> getSuperElements(OWLEntity entity, OWLOntology ontology,
      OwlType type) {
    Stream<OWLProperty> propertyStream = null;
    OWLProperty prop = null;
    switch (type) {
      case AXIOM_CLASS:
        return getSubclasses(entity.asOWLClass());
      case AXIOM_DATA_PROPERTY:
        prop = entity.asOWLDataProperty();
        propertyStream = EntitySearcher.getSuperProperties(prop, ontology.importsClosure());
        break;
      case AXIOM_OBJECT_PROPERTY:
        prop = entity.asOWLObjectProperty();
        propertyStream = EntitySearcher.getSuperProperties(prop, ontology.importsClosure());
        break;
      case AXIOM_ANNOTATION_PROPERTY:
        prop = entity.asOWLAnnotationProperty();
        propertyStream = EntitySearcher.getSuperProperties(prop, ontology.importsClosure());
        break;

    }
    List<PropertyValue> resultProperties = new LinkedList<>();

    for (OWLProperty owlProperty : propertyStream.collect(Collectors.toSet())) {
      LOG.trace("{} Sub Property Of {}", StringUtils.getIdentifier(entity.getIRI()),
          StringUtils.getIdentifier(owlProperty.getIRI()));
      IRI subClazzIri = entity.getIRI();
      IRI superClazzIri = owlProperty.getIRI();

      OwlAxiomPropertyValue pv = new OwlAxiomPropertyValue();
      OwlAxiomPropertyEntity entitySubClass = new OwlAxiomPropertyEntity();
      OwlAxiomPropertyEntity entitySuperClass = new OwlAxiomPropertyEntity();
      entitySubClass.setIri(subClazzIri.getIRIString());
      entitySubClass.setLabel(labelProvider.getLabelOrDefaultFragment(subClazzIri));
      entitySuperClass.setIri(superClazzIri.getIRIString());
      entitySuperClass.setLabel(labelProvider.getLabelOrDefaultFragment(superClazzIri));

      pv.setType(OwlType.TAXONOMY);
      pv.addEntityValues(labelProvider.getLabelOrDefaultFragment(subClazzIri), entitySubClass);
      pv.addEntityValues(labelProvider.getLabelOrDefaultFragment(superClazzIri), entitySuperClass);
      resultProperties.add(pv);

    }

    return resultProperties;
  }

  /**
   * This method is used to display SubClassOf
   *
   * @param clazz Clazz are all properties of direct Subclasses.
   * @return Properties of direct Subclasses.
   */
  private OwlDetailsProperties<PropertyValue> handleDirectSubclasses(OWLClass clazz) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();

    var subClasses = EntitySearcher
        .getSubClasses(clazz, ontologyManager.getOntologyWithImports())
        .collect(Collectors.toSet());

    for (OWLClassExpression subClass : subClasses) {
      if (subClass.isOWLClass()) {
        IRI iri = subClass.asOWLClass().getIRI();
        OwlDirectedSubClassesProperty subClassProperty = new OwlDirectedSubClassesProperty();
        subClassProperty.setType(OwlType.DIRECT_SUBCLASSES);
        subClassProperty.setValue(
            new Pair(labelProvider.getLabelOrDefaultFragment(iri), iri.toString()));
        String key = ViewerIdentifierFactory.createId(
            ViewerIdentifierFactory.Type.function,
            OwlType.DIRECT_SUBCLASSES.name().toLowerCase());
        result.addProperty(key, subClassProperty);
      }
    }

    result.sortPropertiesInAlphabeticalOrder();
    return result;
  }

  /**
   * This method is used to display sub-object property
   *
   * @param ontology This is a loaded ontology.
   * @param obj Obj are all properties of direct subObjectProperty.
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

  /**
   * This method is used to display sub-annotation property
   *
   * @param ontology This is a loaded ontology.
   * @param annotationProperty AnnotationProperty are all properties of direct subAnnotationProperty.
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

  /**
   * This method is used to display sub-data property
   *
   * @param ontology This is a loaded ontology.
   * @param odj Odj are all properties of direct subDataProperty.
   * @return Properties of direct subDataProperty.
   */
  public OwlDetailsProperties<PropertyValue> handleDirectSubDataProperty(OWLOntology ontology,
      OWLDataProperty odj) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();

    // Iterator<OWLSubDataPropertyOfAxiom> iterator = ontology.dataSubPropertyAxiomsForSuperProperty(odj).iterator();
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

  /**
   * This method is used to display Particular Individual
   *
   * @param ontology This is a loaded ontology.
   * @param clazz Clazz are all Instances.
   * @return All instances of a given class;
   */
  private OwlDetailsProperties<PropertyValue> handleInstances(OWLOntology ontology,
      OWLClass clazz) {
    OwlDetailsProperties<PropertyValue> result =
        individualDataHandler.handleClassIndividuals(ontology, clazz);
    result.sortPropertiesInAlphabeticalOrder();
    return result;
  }

  //  /**
//   * This method is used to handle Inherited Axioms
//   *
//   * @param ontology Paramter which loaded ontology.
//   * @param clazz Clazz are all properties of Inherited Axioms.
//   * @return Class and properties of Inherited Axioms.
//   */
  private OwlDetailsProperties<PropertyValue> handleInheritedAxioms(OWLOntology ontology,
      OWLClass clazz) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();
    String subClassOfKey = ViewerIdentifierFactory.createId(
        ViewerIdentifierFactory.Type.axiom,
        "SubClassOf");
    String equivalentClassKey = ViewerIdentifierFactory.createId(
        ViewerIdentifierFactory.Type.axiom,
        "EquivalentClasses");
    String key = ViewerIdentifierFactory.createId(
        ViewerIdentifierFactory.Type.function,
        OwlType.ANONYMOUS_ANCESTOR.name().toLowerCase());

    Set<OWLClassExpression> alreadySeen = new HashSet<>();
    Set<OWLClass> rset = owlUtils.getSuperClasses(clazz, ontology, alreadySeen);
    Map<IRI, Set<OwlAxiomPropertyValue>> values = new HashMap<>();

    rset.stream()
        .forEachOrdered((c) -> {
          OwlDetailsProperties<PropertyValue> handleAxioms = handleAxioms(c, ontology);
          for (Map.Entry<String, List<PropertyValue>> entry : handleAxioms.getProperties()
              .entrySet()) {

            if (entry.getKey().equals(subClassOfKey) || entry.getKey().equals(equivalentClassKey)) {

              for (PropertyValue propertyValue : entry.getValue()) {
                if (propertyValue.getType() != OwlType.TAXONOMY) {

                  if (entry.getKey().equals(equivalentClassKey)) {
                    OwlAxiomPropertyValue opv = (OwlAxiomPropertyValue) propertyValue;
                    String val = opv.getValue();
                    String[] value = val.split(" ");
                    value[0] = value[1] = "";
                    val = String.join(" ", value);
                    opv.setValue(val);
                  }
                  OwlAxiomPropertyValue opv = (OwlAxiomPropertyValue) propertyValue;

                  Set<OwlAxiomPropertyValue> owlAxiomPropertyValues = values.getOrDefault(
                      c.getIRI(), new LinkedHashSet<>());

                  owlAxiomPropertyValues.add(opv);
                  values.put(c.getIRI(), owlAxiomPropertyValues);

                }

              }
            }
          }
        });

    StringBuilder sb = new StringBuilder();

    for (Map.Entry<IRI, Set<OwlAxiomPropertyValue>> entry : values.entrySet()) {
      OwlAxiomPropertyValue opv = new OwlAxiomPropertyValue();

      sb.append("%arg00%").append(" <br />");

      int i = 0;
      for (OwlAxiomPropertyValue owlAxiomPropertyValue : entry.getValue()) {
        i++;
        sb.append("- ").append(owlAxiomPropertyValue.getValue());
        if (i < entry.getValue().size()) {
          sb.append("<br />");
        }

        for (Map.Entry<String, OwlAxiomPropertyEntity> mapping : owlAxiomPropertyValue.getEntityMaping()
            .entrySet()) {
          opv.addEntityValues(mapping.getKey(), mapping.getValue());

        }
      }
      OwlAxiomPropertyEntity prop = new OwlAxiomPropertyEntity();
      prop.setIri(entry.getKey().toString());
      prop.setLabel(labelProvider.getLabelOrDefaultFragment(entry.getKey()));
      opv.addEntityValues("%arg00%", prop);

      opv.setValue(sb.toString());
      opv.setType(OwlType.AXIOM);

      sb = new StringBuilder();

      String fullRenderedString = parseRenderedString(opv);
      opv.setFullRenderedString(fullRenderedString);

      result.addProperty(key, opv);
    }

    result.sortPropertiesInAlphabeticalOrder();
    return result;
  }

  public OwlListDetails handleOntologyMetadata(IRI iri) {
    OwlListDetails ontologyDetails = new OwlListDetails();
    OwlDetailsProperties<PropertyValue> metadata = dataHandler.handleOntologyMetadata(iri, ontologyDetails);
    if (metadata != null && !metadata.getProperties().keySet().isEmpty()) {
      ontologyDetails.addAllProperties(metadata);
      ontologyDetails.setIri(iri.toString());
      ontologyDetails.setLabel(labelProvider.getLabelOrDefaultFragment(iri));
      ontologyDetails.setLocationInModules(dataHandler.getElementLocationInModules(iri.toString()));
      return ontologyDetails;
    }
    return null;
  }

  public List<OntologyModule> getAllModules() {
    return dataHandler.getAllModules();
  }

  public List<String> getElementLocationInModules(String iriString) {
    LOG.debug("[Data Handler] Handle location for element {}", iriString);
    return dataHandler.getElementLocationInModules(iriString);
  }

  public OwlListDetails handleParticularDatatype(IRI iri) {
    OwlListDetails resultDetails = new OwlListDetails();

    var entityEntry = entitiesCacheService.getEntityEntry(iri, OwlType.DATATYPE);

    try {
      if (entityEntry.isPresent()) {
        var datatype = entityEntry.getEntityAs(OWLDatatype.class);

        resultDetails = handleParticularDatatype(datatype);
      }
    } catch (OntoViewerException ex) {
      LOG.warn("Unable to handle datatype {}. Details: {}", iri, ex.getMessage());
    }

    return resultDetails;
  }

  public OwlListDetails handleParticularDatatype(OWLDatatype datatype) {
    var ontology = ontologyManager.getOntology();
    var resultDetails = new OwlListDetails();
    var iri = datatype.getIRI();

    try {
      resultDetails.setLabel(labelProvider.getLabelOrDefaultFragment(iri));
      resultDetails.addAllProperties(handleAnnotations(iri, ontology, resultDetails));
    } catch (Exception ex) {
      LOG.warn("Unable to handle datatype {}. Details: {}", iri, ex.getMessage());
    }

    return resultDetails;
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

          OwlDetailsProperties<PropertyValue> axioms = handleAxioms(annotationProperty, ontology);

          List<PropertyValue> subElements =
              getSuperElements(annotationProperty, ontology, AXIOM_ANNOTATION_PROPERTY);
          OwlTaxonomyImpl taxonomy =
              extractTaxonomy(subElements, iri, ontology, AXIOM_ANNOTATION_PROPERTY);
          taxonomy.sort();

          OwlDetailsProperties<PropertyValue> annotations =
              handleAnnotations(annotationProperty.getIRI(), ontology, resultDetails);

          resultDetails.addAllProperties(annotations);
          resultDetails.addAllProperties(directSubAnnotationProperty);
          resultDetails.addAllProperties(axioms);
          resultDetails.setTaxonomy(taxonomy);
        }
      }
    } catch (OntoViewerException ex) {
      LOG.warn("Unable to handle annotation property {}. Details: {}", iri, ex.getMessage());
    }

    return resultDetails;
  }

  public MaturityLevel getMaturityLevel(IRI iri) {
    return moduleHandler.getMaturityLevelForElement(iri);
  }

  private void parseUrlAxiom(String eIri, String[] splited, int i, Boolean hasOpeningParenthesis,
      int countOpeningParenthesis, Boolean hasClosingParenthesis, int countClosingParenthesis,
      Boolean hasComma, int countComma) {
    String label = labelProvider.getLabelOrDefaultFragment(IRI.create(eIri));

    String textToReplace = label;

    if (hasOpeningParenthesis) {
      String prefix = String.join("", Collections.nCopies(countOpeningParenthesis, "("));
      textToReplace = prefix + textToReplace;
    }
    if (hasClosingParenthesis) {
      String postfix = String.join("", Collections.nCopies(countClosingParenthesis, ")"));
      textToReplace = textToReplace + postfix;
    }
    if (hasComma) {
      String postfix = String.join("", Collections.nCopies(countComma, ","));
      textToReplace = textToReplace + postfix;
    }
    splited[i] = textToReplace;

  }

  private OwlDetailsProperties<PropertyValue> extractUsageForClasses(OWLClass clazz,
      OWLOntology ontology) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();
    String key = ViewerIdentifierFactory.createId(
        ViewerIdentifierFactory.Type.function,
        OwlType.USAGE_CLASSES.name().toLowerCase());
    //Usage OWLClass--------------------------------------------------------------------------

    Map<IRI, List<OwlAxiomPropertyValue>> values = new HashMap<>();
    Set<OWLSubClassOfAxiom> axioms = new HashSet<>();
    ontology.importsClosure().forEach(currentOntology -> {
      axioms.addAll(
          currentOntology.axioms(AxiomType.SUBCLASS_OF)
              .filter(el -> el.accept(containsVisitors.visitor(clazz.getIRI())))
              .collect(Collectors.toSet()));
    });

    int start = 0;
    for (OWLSubClassOfAxiom axiom : axioms) {
      LOG.debug("OwlDataHandler -> extractUsage {}", axiom.toString());
      LOG.debug("OwlDataHandler -> extractUsageAx {}", axiom.getSubClass());

      IRI iri = axiom.getSubClass().asOWLClass().getIRI();
      if (iri.equals(clazz.getIRI())) {
        continue;
      }

      String iriFragment = iri.getFragment();
      String splitFragment = StringUtils.getIdentifier(iri);
      Boolean fixRenderedIri = !iriFragment.equals(splitFragment);

      OwlAxiomPropertyValue opv = prepareAxiomPropertyValue(
          axiom,
          iriFragment,
          splitFragment,
          fixRenderedIri,
          key, start, false);
      start = opv.getLastId() + 1;
      List<OwlAxiomPropertyValue> ll = values.getOrDefault(iri, new LinkedList<>());
      ll.add(opv);

      values.put(iri, ll);
    }

    StringBuilder sb = new StringBuilder();

    for (Map.Entry<IRI, List<OwlAxiomPropertyValue>> entry : values.entrySet()) {
      OwlAxiomPropertyValue opv = new OwlAxiomPropertyValue();
      sb.append("%arg00%").append(" <br />"); //<br />
      int i = 0;
      for (OwlAxiomPropertyValue owlAxiomPropertyValue : entry.getValue()) {
        i++;
        sb.append("- ").append(owlAxiomPropertyValue.getValue());
        if (i < entry.getValue().size()) {
          sb.append("<br />");
        }
        for (Map.Entry<String, OwlAxiomPropertyEntity> mapping :
            owlAxiomPropertyValue.getEntityMaping().entrySet()) {
          opv.addEntityValues(mapping.getKey(), mapping.getValue());
        }
      }
      OwlAxiomPropertyEntity prop = new OwlAxiomPropertyEntity();
      prop.setIri(entry.getKey().toString());
      prop.setLabel(labelProvider.getLabelOrDefaultFragment(entry.getKey()));
      opv.addEntityValues("%arg00%", prop);

      opv.setValue(sb.toString());
      opv.setType(OwlType.AXIOM);

      LOG.debug("Generated big axiom: {}", sb);
      sb = new StringBuilder();

      String fullRenderedString = parseRenderedString(opv);
      opv.setFullRenderedString(fullRenderedString);

      result.addProperty(key, opv);
    }

    //Range of ObjectProperty--------------------------------
    Map<IRI, List<OwlAxiomPropertyValue>> valuesO = new HashMap<>();
    Set<OWLObjectPropertyRangeAxiom> ops = new HashSet<>();
    ontology.importsClosure().forEach(currentOntology -> {
      ops.addAll(currentOntology.axioms(AxiomType.OBJECT_PROPERTY_RANGE)
          .filter(el -> el.accept(containsVisitors.visitorObjectProperty(clazz.getIRI())))
          .collect(Collectors.toSet()));
    });

    int startR = 0;

    LOG.debug("How many range is found for x : {}", ops.size());

    for (OWLObjectPropertyRangeAxiom axiom : ops) {
      OWLEntity rangeEntity = axiom.signature()
          .filter(entity -> !entity.getIRI()
              .equals(clazz.getIRI()))
          .findFirst().get();
      LOG.debug("OwlDataHandler -> extractUsageRangeAxiom {}", rangeEntity.getIRI());

      String iriFragment = rangeEntity.getIRI().toString();
      String splitFragment = StringUtils.getIdentifier(rangeEntity.getIRI().toString());
      Boolean fixRenderedIri = !iriFragment.equals(splitFragment);

      OwlAxiomPropertyValue opv = prepareAxiomPropertyValue(axiom, iriFragment, splitFragment, fixRenderedIri, key,
          startR, false);
      startR = opv.getLastId() + 1;
      List<OwlAxiomPropertyValue> ll = valuesO.getOrDefault(rangeEntity, new LinkedList<>());
      ll.add(opv);

      valuesO.put(rangeEntity.getIRI(), ll);
    }

    StringBuilder sbo = new StringBuilder();

    for (Map.Entry<IRI, List<OwlAxiomPropertyValue>> entry : valuesO.entrySet()) {
      OwlAxiomPropertyValue opv = new OwlAxiomPropertyValue();
      sbo.append("%arg00%").append(" <br />"); //<br />
      int i = 0;
      for (OwlAxiomPropertyValue owlAxiomPropertyValue : entry.getValue()) {
        i++;
        sbo.append("- ").append(owlAxiomPropertyValue.getValue());
        if (i < entry.getValue().size()) {
          sbo.append("<br />");
        }
        for (Map.Entry<String, OwlAxiomPropertyEntity> maping : owlAxiomPropertyValue.getEntityMaping()
            .entrySet()) {
          opv.addEntityValues(maping.getKey(), maping.getValue());
        }
      }
      OwlAxiomPropertyEntity prop = new OwlAxiomPropertyEntity();
      prop.setIri(entry.getKey().toString());
      prop.setLabel(labelProvider.getLabelOrDefaultFragment(entry.getKey()));
      opv.addEntityValues("%arg00%", prop);

      opv.setValue(sbo.toString());
      opv.setType(OwlType.AXIOM);

      LOG.debug("Generated big axiom: {}", sbo.toString());
      sbo = new StringBuilder();

      String fullRenderedString = parseRenderedString(opv);
      opv.setFullRenderedString(fullRenderedString);

      result.addProperty(key, opv);
    }

    //Domain of ObjectProperty-----------------------------------------------------
    Map<IRI, List<OwlAxiomPropertyValue>> valuesD = new HashMap<>();
    Set<OWLObjectPropertyDomainAxiom> opd = new HashSet<>();
    ontology.importsClosure().forEach(currentOntology -> {
      opd.addAll(
          currentOntology.axioms(AxiomType.OBJECT_PROPERTY_DOMAIN)
              .filter(el -> el.accept(containsVisitors.visitorObjectProperty(clazz.getIRI())))
              .collect(Collectors.toSet()));
    });
    int startD = 0;

    for (OWLObjectPropertyDomainAxiom axiom : opd) {
      OWLEntity domainEntity
          = axiom.signature()
          .filter(e -> !e.getIRI().equals(clazz.getIRI()))
          .findFirst().get();
      LOG.debug("OwlDataHandler -> extractUsageObjectDomainAxiom {}", domainEntity.getIRI());

      String iriFragment = domainEntity.getIRI().toString();
      String splitFragment = StringUtils.getIdentifier(domainEntity.getIRI().toString());
      Boolean fixRenderedIri = !iriFragment.equals(splitFragment);

      OwlAxiomPropertyValue opv = prepareAxiomPropertyValue(axiom, iriFragment, splitFragment, fixRenderedIri, key,
          startD, false);
      startD = opv.getLastId() + 1;
      List<OwlAxiomPropertyValue> ll = valuesD.getOrDefault(domainEntity, new LinkedList());
      ll.add(opv);

      valuesD.put(domainEntity.getIRI(), ll);
    }

    StringBuilder sbd = new StringBuilder();

    for (Map.Entry<IRI, List<OwlAxiomPropertyValue>> entry : valuesD.entrySet()) {
      OwlAxiomPropertyValue opv = new OwlAxiomPropertyValue();
      sbd.append("%arg00%").append(" <br />");
      int i = 0;
      for (OwlAxiomPropertyValue owlAxiomPropertyValue : entry.getValue()) {
        i++;
        sbd.append("- ").append(owlAxiomPropertyValue.getValue());
        if (i < entry.getValue().size()) {
          sbd.append("<br />");
        }
        for (Map.Entry<String, OwlAxiomPropertyEntity> maping : owlAxiomPropertyValue.getEntityMaping()
            .entrySet()) {
          opv.addEntityValues(maping.getKey(), maping.getValue());
        }
      }
      OwlAxiomPropertyEntity prop = new OwlAxiomPropertyEntity();
      prop.setIri(entry.getKey().toString());
      prop.setLabel(labelProvider.getLabelOrDefaultFragment(entry.getKey()));
      opv.addEntityValues("%arg00%", prop);

      opv.setValue(sbd.toString());
      opv.setType(OwlType.AXIOM);

      LOG.debug("Generated big axiom: {}", sbd);
      sbd = new StringBuilder();
      String fullRenderedString = parseRenderedString(opv);
      opv.setFullRenderedString(fullRenderedString);

      result.addProperty(key, opv);
    }

    result.sortPropertiesInAlphabeticalOrder();

    return result;
  }

  private String parseRenderedString(OwlAxiomPropertyValue opv) {
    String result = opv.getValue();
    for (Map.Entry<String, OwlAxiomPropertyEntity> entry : opv.getEntityMaping().entrySet()) {
      LOG.debug("parseRenderedString: {}", entry.toString());
      String key = entry.getKey();
      if (!key.contains("arg")) {
        continue;
      }
      String replecment = entry.getValue().getLabel();
      LOG.debug("replecment: {}", replecment);
      result = result.replaceAll(key, replecment);
      LOG.debug("result: {}", result);
    }

    return result;
  }
}
