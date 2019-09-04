package org.edmcouncil.spec.fibo.weasel.ontology.data;

import java.util.ArrayList;
import org.edmcouncil.spec.fibo.weasel.model.OwlDetails;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlAnnotationPropertyValue;
import org.edmcouncil.spec.fibo.weasel.model.WeaselOwlType;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.edmcouncil.spec.fibo.config.configuration.model.AppConfiguration;
import org.edmcouncil.spec.fibo.config.configuration.model.PairImpl;
import org.edmcouncil.spec.fibo.weasel.model.PropertyValue;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlAxiomPropertyValue;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlDirectedSubClassesProperty;
import org.edmcouncil.spec.fibo.weasel.model.property.OwlListElementIndividualProperty;
import org.edmcouncil.spec.fibo.weasel.model.taxonomy.OwlTaxonomyElementImpl;
import org.edmcouncil.spec.fibo.weasel.model.taxonomy.OwlTaxonomyImpl;
import org.edmcouncil.spec.fibo.weasel.model.taxonomy.OwlTaxonomyValue;
import org.edmcouncil.spec.fibo.weasel.ontology.visitor.WeaselOntologyVisitors;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.InferenceDepth;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 */
@Component
public class OwlDataHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(OwlDataHandler.class);

  private final OWLObjectRenderer rendering = new ManchesterOWLSyntaxOWLObjectRendererImpl();
  @Autowired
  private OwlDataExtractor dataExtractor;
  @Autowired
  private AppConfiguration appConfiguration;

  public OwlDetails handleParticularClass(IRI iri, OWLOntology ontology) {
    OwlDetails resultDetails = new OwlDetails();
    Iterator<OWLClass> classesIterator = ontology.classesInSignature().iterator();

    while (classesIterator.hasNext()) {
      OWLClass clazz = classesIterator.next();

      if (clazz.getIRI().equals(iri)) {
        LOGGER.debug("Find owl class wih iri: {}", iri.toString());

        handleParticularSubClassOf(ontology, clazz);

        resultDetails.setLabel(clazz.getIRI().getShortForm());

        OwlDetailsProperties<PropertyValue> axioms = handleAxioms(clazz, ontology);

        List<PropertyValue> subclasses = axioms.getProperties().getOrDefault(AxiomType.SUBCLASS_OF.getName(), new ArrayList<>(0));
        List<PropertyValue> taxElements = subclasses.stream().filter((pv) -> (pv.getType().equals(WeaselOwlType.TAXONOMY))).collect(Collectors.toList());
        OwlDetailsProperties<PropertyValue> handleSubClassOf = handleParticularSubClassOf(ontology, clazz);
        OwlDetailsProperties<PropertyValue> individuals = handleParticularIndividual(ontology, clazz);
        OwlDetailsProperties<PropertyValue> inheritedAxioms = handleInheritedAxioms(ontology, clazz);

        subclasses = subclasses.stream().filter((pv) -> (!pv.getType().equals(WeaselOwlType.TAXONOMY))).collect(Collectors.toList());
        axioms.getProperties().put(AxiomType.SUBCLASS_OF.getName(), subclasses);
        OwlTaxonomyImpl tax = extractTaxonomy(taxElements, iri, ontology, WeaselOwlType.AXIOM_CLASS);
        tax.sort();

        resultDetails.setTaxonomy(tax);
        OwlDetailsProperties<PropertyValue> annotations
            = handleAnnotations(clazz.getIRI(), ontology);

        resultDetails.addAllProperties(axioms);
        resultDetails.addAllProperties(annotations);
        resultDetails.addAllProperties(handleSubClassOf);
        resultDetails.addAllProperties(individuals);
        resultDetails.addAllProperties(inheritedAxioms);

      }
    }
    return resultDetails;
  }

  public OwlDetails handleParticularIndividual(IRI iri, OWLOntology ontology) {
    OwlDetails resultDetails = new OwlDetails();
    Iterator<OWLNamedIndividual> individualIterator = ontology.individualsInSignature().iterator();

    while (individualIterator.hasNext()) {
      OWLNamedIndividual individual = individualIterator.next();

      if (individual.getIRI().equals(iri)) {
        LOGGER.debug("Find owl named individual wih iri: {}", iri.toString());

        resultDetails.setLabel(individual.getIRI().getShortForm());

        OwlDetailsProperties<PropertyValue> axioms = handleAxioms(individual, ontology);

        OwlDetailsProperties<PropertyValue> annotations
            = handleAnnotations(individual.getIRI(), ontology);

        resultDetails.addAllProperties(axioms);
        resultDetails.addAllProperties(annotations);
      }
    }
    return resultDetails;
  }

  private OwlDetailsProperties<PropertyValue> handleAnnotations(IRI iri, OWLOntology ontology) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();

    Iterator<OWLAnnotationAssertionAxiom> annotationAssertionAxiom
        = ontology.annotationAssertionAxioms(iri).iterator();
    while (annotationAssertionAxiom.hasNext()) {
      OWLAnnotationAssertionAxiom next = annotationAssertionAxiom.next();
      String property = rendering.render(next.getProperty());
      String value = next.getValue().toString();

      LOGGER.debug("Find annotation, value: \"{}\", property: \"{}\" ", value, property);

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

  private OwlDetailsProperties<PropertyValue> handleAxioms(
      OWLNamedIndividual obj,
      OWLOntology ontology) {

    Iterator<OWLIndividualAxiom> axiomsIterator = ontology.axioms(obj).iterator();
    return handleAxioms(axiomsIterator);
  }

  private OwlDetailsProperties<PropertyValue> handleAxioms(
      OWLObjectProperty obj,
      OWLOntology ontology) {

    Iterator<OWLObjectPropertyAxiom> axiomsIterator = ontology.axioms(obj).iterator();
    return handleAxioms(axiomsIterator);
  }

  private OwlDetailsProperties<PropertyValue> handleAxioms(
      OWLDataProperty obj,
      OWLOntology ontology) {

    Iterator<OWLDataPropertyAxiom> axiomsIterator = ontology.axioms(obj).iterator();
    return handleAxioms(axiomsIterator);
  }

  private OwlDetailsProperties<PropertyValue> handleAxioms(
      OWLClass obj,
      OWLOntology ontology) {

    Iterator<OWLClassAxiom> axiomsIterator = ontology.axioms(obj).iterator();
    return handleAxioms(axiomsIterator);
  }

  private OwlTaxonomyImpl extractTaxonomy(List<PropertyValue> subElements, IRI objIri, OWLOntology ontology, WeaselOwlType type) {
    OwlTaxonomyImpl taxonomy = new OwlTaxonomyImpl();
    if (subElements.size() > 0) {

      for (PropertyValue property : subElements) {
        if (property.getType().equals(WeaselOwlType.TAXONOMY)) {
          OwlAxiomPropertyValue axiomProperty = (OwlAxiomPropertyValue) property;
          IRI sci = extractSubElementIri(axiomProperty, objIri);
          OWLEntity entity = createEntity(ontology, sci, type);
          LOGGER.debug(">> {} Sub Element Of {}", objIri.getShortForm(), entity.getIRI().getShortForm());
          List<PropertyValue> subTax = getSubElements(entity, ontology, type);

          OwlTaxonomyImpl subCLassTax = extractTaxonomy(subTax, entity.getIRI(), ontology, type);

          OwlTaxonomyValue val1 = new OwlTaxonomyValue(WeaselOwlType.STRING, objIri.getFragment());
          OwlTaxonomyValue val2 = new OwlTaxonomyValue(WeaselOwlType.IRI, objIri.getIRIString());
          OwlTaxonomyElementImpl taxEl = new OwlTaxonomyElementImpl(val1, val2);

          if (subCLassTax.getValue().size() > 0) {
            taxonomy.addTaxonomy(subCLassTax, taxEl);
          } else {
            List<OwlTaxonomyElementImpl> currentTax = new LinkedList<>();
            currentTax.add(taxEl);
            taxonomy.addTaxonomy(currentTax);
          }
        }
      }

    } else {

      LOGGER.info(">>> End leaf on {}", objIri.getFragment());
      OwlTaxonomyValue val1 = new OwlTaxonomyValue(WeaselOwlType.STRING, objIri.getFragment());
      OwlTaxonomyValue val2 = new OwlTaxonomyValue(WeaselOwlType.IRI, objIri.getIRIString());
      OwlTaxonomyElementImpl taxEl = new OwlTaxonomyElementImpl(val1, val2);
      List<OwlTaxonomyElementImpl> currentTax = new LinkedList<>();
      currentTax.add(taxEl);
      taxonomy.addTaxonomy(currentTax);
    }

    return taxonomy;
  }

  private OWLEntity createEntity(OWLOntology ontology, IRI sci, WeaselOwlType type) {

    switch (type) {
      case AXIOM_CLASS:
        return ontology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(sci);
      case AXIOM_DATA_PROPERTY:
        return ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDataProperty(sci);
      case AXIOM_OBJECT_PROPERTY:
        return ontology.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(sci);
      case AXIOM_DATATYPE:
        return ontology.getOWLOntologyManager().getOWLDataFactory().getOWLDatatype(sci);
    }

    return null;
  }

  private IRI extractSubElementIri(OwlAxiomPropertyValue axiomProperty, IRI objIri) {
    for (Map.Entry<String, String> entry : axiomProperty.getEntityMaping().entrySet()) {
      if (!entry.getValue().equals(objIri.getIRIString())) {
        return IRI.create(entry.getValue());
      }
    }
    return null;
  }

  private <T extends OWLAxiom> OwlDetailsProperties<PropertyValue> handleAxioms(
      Iterator<T> axiomsIterator) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<>();

    while (axiomsIterator.hasNext()) {
      T axiom = axiomsIterator.next();
      String value = rendering.render(axiom);
      String key = axiom.getAxiomType().getName();
      OwlAxiomPropertyValue opv = new OwlAxiomPropertyValue();
      opv.setValue(value);

      opv.setType(WeaselOwlType.AXIOM);
      LOGGER.debug("Find Axiom \"{}\" with type \"{}\"", value, key);
      Boolean isRestriction = isRestriction(axiom);

      if (!isRestriction && axiom.getAxiomType().equals(AxiomType.SUBCLASS_OF)) {
        LOGGER.debug("Find non restriction SubClassOf");
        opv.setType(WeaselOwlType.TAXONOMY);
      }

      Iterator<OWLEntity> iterator = axiom.signature().iterator();
      while (iterator.hasNext()) {
        OWLEntity next = iterator.next();
        String eSignature = rendering.render(next);
        String eIri = next.getIRI().toString();
        opv.addEntityValues(eSignature, eIri);
      }
      result.addProperty(key, opv);
    }
    return result;
  }

  private static <T extends OWLAxiom> Boolean isRestriction(T axiom) {
    Boolean isRestriction = axiom.accept(WeaselOntologyVisitors.isRestrictionVisitor);
    if (isRestriction == null) {
      isRestriction = Boolean.FALSE;
    }
    return isRestriction;
  }

  public OwlDetails handleParticularDataProperty(IRI iri, OWLOntology ontology) {
    OwlDetails resultDetails = new OwlDetails();
    Iterator<OWLDataProperty> dataPropertyIt = ontology.dataPropertiesInSignature().iterator();

    while (dataPropertyIt.hasNext()) {
      OWLDataProperty dataProperty = dataPropertyIt.next();

      if (dataProperty.getIRI().equals(iri)) {
        LOGGER.debug("Find owl data property wih iri: {}", iri.toString());

        resultDetails.setLabel(dataProperty.getIRI().getShortForm());

        OwlDetailsProperties<PropertyValue> axioms = handleAxioms(dataProperty, ontology);

        List<PropertyValue> subElements = getSubElements(dataProperty, ontology, WeaselOwlType.AXIOM_DATA_PROPERTY);
        OwlTaxonomyImpl taxonomy = extractTaxonomy(subElements, iri, ontology, WeaselOwlType.AXIOM_DATA_PROPERTY);
        taxonomy.sort();

        OwlDetailsProperties<PropertyValue> annotations
            = handleAnnotations(dataProperty.getIRI(), ontology);

        resultDetails.addAllProperties(axioms);
        resultDetails.addAllProperties(annotations);
        resultDetails.setTaxonomy(taxonomy);
      }
    }
    return resultDetails;

  }

  public OwlDetails handleParticularObjectProperty(IRI iri, OWLOntology ontology) {
    OwlDetails resultDetails = new OwlDetails();
    Iterator<OWLObjectProperty> dataPropertyIt = ontology.objectPropertiesInSignature().iterator();

    while (dataPropertyIt.hasNext()) {
      OWLObjectProperty dataProperty = dataPropertyIt.next();

      if (dataProperty.getIRI().equals(iri)) {
        LOGGER.debug("Find owl object property wih iri: {}", iri.toString());

        resultDetails.setLabel(dataProperty.getIRI().getShortForm());

        OwlDetailsProperties<PropertyValue> axioms = handleAxioms(dataProperty, ontology);

        List<PropertyValue> subElements = getSubElements(dataProperty, ontology, WeaselOwlType.AXIOM_OBJECT_PROPERTY);
        OwlTaxonomyImpl taxonomy = extractTaxonomy(subElements, iri, ontology, WeaselOwlType.AXIOM_OBJECT_PROPERTY);
        taxonomy.sort();

        OwlDetailsProperties<PropertyValue> annotations
            = handleAnnotations(dataProperty.getIRI(), ontology);

        resultDetails.addAllProperties(axioms);
        resultDetails.addAllProperties(annotations);
        resultDetails.setTaxonomy(taxonomy);
      }
    }
    return resultDetails;
  }

  private List<PropertyValue> getSubElements(OWLEntity entity, OWLOntology ontology, WeaselOwlType type) {
    Stream<OWLProperty> propertyStream = null;
    OWLProperty prop = null;
    switch (type) {
      case AXIOM_CLASS:
        return getSubclasses(ontology, AxiomType.SUBCLASS_OF, entity);
      case AXIOM_DATA_PROPERTY:
        prop = entity.asOWLDataProperty();
        propertyStream = EntitySearcher.getSuperProperties(prop, ontology);
        break;
      case AXIOM_OBJECT_PROPERTY:
        prop = entity.asOWLObjectProperty();
        propertyStream = EntitySearcher.getSuperProperties(prop, ontology);
        break;
    }
    List<PropertyValue> resultProperties = new LinkedList<>();

    for (OWLProperty owlProperty : propertyStream.collect(Collectors.toSet())) {
      LOGGER.debug("{} Sub Property Of {}", entity.getIRI().getFragment(), owlProperty.getIRI().getFragment());
      IRI subClazzIri = entity.getIRI();
      IRI superClazzIri = owlProperty.getIRI();

      OwlAxiomPropertyValue pv = new OwlAxiomPropertyValue();
      pv.setType(WeaselOwlType.TAXONOMY);
      pv.addEntityValues(subClazzIri.getFragment(), subClazzIri.getIRIString());
      pv.addEntityValues(superClazzIri.getFragment(), superClazzIri.getIRIString());
      resultProperties.add(pv);

    }

    return resultProperties;
  }

  private List<PropertyValue> getSubclasses(OWLOntology ontology, AxiomType<OWLSubClassOfAxiom> subType, OWLEntity entity) {
    List<PropertyValue> result = new LinkedList<>();
    ontology.axioms(subType)
        .collect(Collectors.toList())
        .stream()
        .filter((subClasse)
            -> (subClasse.getSuperClass() instanceof OWLClass
        && subClasse.getSubClass() instanceof OWLClass))
        .forEachOrdered((subClasse) -> {

          OWLClass superClazz = (OWLClass) subClasse.getSuperClass();
          OWLClass subClazz = (OWLClass) subClasse.getSubClass();
          if (subClazz.getIRI().equals(entity.getIRI())) {
            IRI subClazzIri = subClazz.getIRI();
            IRI superClazzIri = superClazz.getIRI();

            OwlAxiomPropertyValue pv = new OwlAxiomPropertyValue();
            pv.setType(WeaselOwlType.TAXONOMY);
            pv.addEntityValues(subClazzIri.getFragment(), subClazzIri.getIRIString());
            pv.addEntityValues(superClazzIri.getFragment(), superClazzIri.getIRIString());
            pv.setValue(rendering.render(subClasse));
            result.add(pv);
          }
        });
    return result;
  }

  public OwlDetailsProperties<PropertyValue> handleParticularSubClassOf(OWLOntology ontology, OWLClass clazz) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<PropertyValue>();

    Iterator<OWLSubClassOfAxiom> iterator = ontology.subClassAxiomsForSuperClass(clazz).iterator();

    while (iterator.hasNext()) {
      OWLSubClassOfAxiom next = iterator.next();
      IRI iri = next.getSubClass().asOWLClass().getIRI();
      String fragment = iri.getFragment();
      // LOGGER.debug(next.toString());
      OWLClassExpression superClass = next.getSuperClass();
      OwlDirectedSubClassesProperty r = new OwlDirectedSubClassesProperty();
      r.setType(WeaselOwlType.DIRECT_SUBCLASSES);
      r.setValue(new PairImpl(fragment, iri.toString()));
      result.addProperty(WeaselOwlType.DIRECT_SUBCLASSES.name(), r);
    }
    return result;
  }

  private OwlDetailsProperties<PropertyValue> handleParticularIndividual(OWLOntology ontology, OWLClass clazz) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<PropertyValue>();
    OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
    OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);
    NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(clazz, true);

    for (OWLNamedIndividual namedIndividual : instances.entities().collect(Collectors.toSet())) {
      String fragment = namedIndividual.getIRI().getFragment();
      //LOGGER.debug(namedIndividual.getIRI().toString());
      OwlListElementIndividualProperty s = new OwlListElementIndividualProperty();
      s.setType(WeaselOwlType.INSTANCES);
      s.setValue(new PairImpl(fragment, namedIndividual.getIRI().toString()));
      result.addProperty(WeaselOwlType.INSTANCES.name(), s);
      namedIndividual.getEntityType();
    }
    return result;
  }

  private OwlDetailsProperties<PropertyValue> handleInheritedAxioms(OWLOntology ontology, OWLClass clazz) {
    OwlDetailsProperties<PropertyValue> result = new OwlDetailsProperties<PropertyValue>();
    OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
    OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);

    NodeSet<OWLClass> rset = reasoner.getSuperClasses(clazz, InferenceDepth.ALL);
    for (OWLClass c : rset.entities().collect(Collectors.toSet())) {
      //LOGGER.debug(c.getIRI().getFragment());
      OwlDetailsProperties<PropertyValue> handleAxioms = handleAxioms(c, ontology);

      for (Map.Entry<String, List<PropertyValue>> entry : handleAxioms.getProperties().entrySet()) {
        for (PropertyValue propertyValue : entry.getValue()) {
          result.addProperty(WeaselOwlType.ANONYMOUS_ANCESTOR.name(), propertyValue);
        }
      }
    }
    return result;

  }
}
