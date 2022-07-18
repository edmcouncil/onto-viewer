package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import com.google.common.base.Stopwatch;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlListDetails;
import org.edmcouncil.spec.ontoviewer.core.model.module.OntologyModule;
import org.edmcouncil.spec.ontoviewer.core.model.onto.OntologyResources;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationIri;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.CustomDataFactory;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This data handler working with FIBO ontology.
 *
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
@Component
public class DataHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataHandler.class);

  private final AnnotationsDataHandler annotationsDataHandler;
  private final CustomDataFactory customDataFactory;
  private final ModuleHandler moduleHandler;
  private final OntologyManager ontologyManager;

  private final Map<IRI, OntologyResources> resources = new HashMap<>();

  private Map<IRI, IRI> entityIriToOntologyIriMap;

  public DataHandler(AnnotationsDataHandler annotationsDataHandler,
      CustomDataFactory customDataFactory,
      ModuleHandler moduleHandler,
      OntologyManager ontologyManager) {
    this.annotationsDataHandler = annotationsDataHandler;
    this.customDataFactory = customDataFactory;
    this.moduleHandler = moduleHandler;
    this.ontologyManager = ontologyManager;
  }

  public OwlDetailsProperties<PropertyValue> handleOntologyMetadata(IRI iri, OwlListDetails details) {
    OWLOntology ontology = ontologyManager.getOntology();
    OWLOntologyManager manager = ontology.getOWLOntologyManager();
    OwlDetailsProperties<PropertyValue> annotations = null;

    for (OWLOntology currentOntology : manager.ontologies().collect(Collectors.toSet())) {
      var ontologyIriOptional = currentOntology.getOntologyID().getOntologyIRI();
      if (ontologyIriOptional.isPresent()) {
        var currentOntologyIri = ontologyIriOptional.get();
        if (currentOntologyIri.equals(iri)
            || currentOntologyIri.equals(
            IRI.create(iri.getIRIString().substring(0, iri.getIRIString().length() - 1)))) {
          annotations = annotationsDataHandler.handleOntologyAnnotations(currentOntology.annotations(), details);

          OntologyResources ontologyResources = getOntologyResources(currentOntologyIri);
          if (ontologyResources != null) {
            for (Map.Entry<String, List<PropertyValue>> entry : ontologyResources.getResources().entrySet()) {
              for (PropertyValue propertyValue : entry.getValue()) {
                annotations.addProperty(entry.getKey(), propertyValue);
              }
            }
          }
          details.setMaturityLevel(moduleHandler.getMaturityLevelForModule(iri));
          break;
        }
      }
    }

    return annotations;
  }

  public void populateOntologyResources() {
    LOGGER.info("Start populating ontology mapping of entity IRIs to ontology IRIs...");
    var stopwatch = Stopwatch.createStarted();

    this.entityIriToOntologyIriMap = populateEntityIriToOntologyIriMap();
    this.moduleHandler.updateModules();

    LOGGER.info("Finished populating mapping entity IRIs to ontology IRIs in {} seconds.",
        stopwatch.elapsed(TimeUnit.SECONDS));
  }

  public List<String> getElementLocationInModules(IRI elementIri) {
    List<String> result = new LinkedList<>();

    var allModules = moduleHandler.getModules();

    if (allModules.isEmpty()) {
      return result;
    }

    IRI ontologyIri = findElementInOntology(elementIri);
    ontologyIri = ontologyIri == null ? elementIri : ontologyIri;

    LOGGER.debug("Element found in ontology {}", ontologyIri);
    if (ontologyIri != null) {
      for (OntologyModule module : allModules) {
        if (trackingThePath(module, ontologyIri, result, elementIri)) {
          LOGGER.debug("[Data Handler] Location Path {}", Arrays.toString(result.toArray()));
          return result;
        }
      }
    }

    return result;
  }

  private OntologyResources getOntologyResources(IRI ontologyIri) {
    if (!resources.containsKey(ontologyIri)) {
      var owlOntologyOptional = ontologyManager.getOntologyWithImports()
          .filter(owlOntology -> {
            var owlOntologyIriOptional = owlOntology.getOntologyID().getOntologyIRI();
            return owlOntologyIriOptional.map(iri -> iri.equals(ontologyIri)).orElse(false);
          })
          .findFirst();

      if (owlOntologyOptional.isPresent()) {
        var ontologyResources = extractOntologyResources(owlOntologyOptional.get());
        resources.put(ontologyIri, ontologyResources);
      } else {
        resources.put(ontologyIri, new OntologyResources());
      }
    }
    return resources.get(ontologyIri);
  }

  private Map<IRI, IRI> populateEntityIriToOntologyIriMap() {
    Map<IRI, IRI> entityIriToOntologyIri = new HashMap<>();

    ontologyManager.getOntologyWithImports()
        .forEach(owlOntology -> {
          var ontologyIriOptional = owlOntology.getOntologyID().getOntologyIRI();

          if (ontologyIriOptional.isPresent()) {
            var ontologyIri = ontologyIriOptional.get();

            owlOntology.signature(Imports.EXCLUDED)
                .forEach(owlEntity -> {
                  var entityIri = owlEntity.getIRI();
                  entityIriToOntologyIri.put(entityIri, ontologyIri);
                });
          }
        });

    return entityIriToOntologyIri;
  }

  private OntologyResources extractOntologyResources(OWLOntology selectedOntology) {
    OntologyResources ontologyResources = new OntologyResources();
    Optional<IRI> ontologyIriOptional = selectedOntology.getOntologyID().getOntologyIRI();
    IRI ontologyIri;
    if (ontologyIriOptional.isPresent()) {
      ontologyIri = ontologyIriOptional.get();
    } else {
      ontologyIriOptional = selectedOntology.getOntologyID().getDefaultDocumentIRI();
      if (ontologyIriOptional.isPresent()) {
        ontologyIri = ontologyIriOptional.get();
        LOGGER.debug("IRI for this ontology doesn't exist, use Default Document IRI {}", ontologyIri);
      } else {
        LOGGER.debug("Ontology doesn't have any iri to present... Ontology ID: {}", selectedOntology.getOntologyID());
        return null;
      }
    }

    selectedOntology.annotationPropertiesInSignature()
        .map(annotationProperty -> customDataFactory.createAnnotationIri(annotationProperty.getIRI().toString()))
        .forEachOrdered(annotationIri ->
            ontologyResources.addElement(
                selectResourceIriString(annotationIri, ontologyIri, ViewerIdentifierFactory.Element.annotationProperty),
                annotationIri));

    selectedOntology.classesInSignature()
        .map(clazz -> customDataFactory.createAnnotationIri(clazz.getIRI().toString()))
        .forEachOrdered(clazzIri ->
            ontologyResources.addElement(
                selectResourceIriString(clazzIri, ontologyIri, ViewerIdentifierFactory.Element.clazz),
                clazzIri));

    selectedOntology.dataPropertiesInSignature()
        .map(dataProperty -> customDataFactory.createAnnotationIri(dataProperty.getIRI().toString()))
        .forEachOrdered(dataPropertyIri ->
            ontologyResources.addElement(
                selectResourceIriString(dataPropertyIri, ontologyIri, ViewerIdentifierFactory.Element.dataProperty),
                dataPropertyIri));

    selectedOntology.objectPropertiesInSignature()
        .map(objectProperty -> customDataFactory.createAnnotationIri(objectProperty.getIRI().toString()))
        .forEachOrdered(objectPropertyIri ->
            ontologyResources.addElement(
                selectResourceIriString(objectPropertyIri, ontologyIri, ViewerIdentifierFactory.Element.objectProperty),
                objectPropertyIri));

    selectedOntology.individualsInSignature()
        .map(individual -> customDataFactory.createAnnotationIri(individual.getIRI().toString()))
        .forEachOrdered(individual ->
            ontologyResources.addElement(
                selectResourceIriString(individual, ontologyIri, ViewerIdentifierFactory.Element.instance),
                individual));

    selectedOntology.datatypesInSignature()
        .map(datatype -> customDataFactory.createAnnotationIri(datatype.getIRI().toString()))
        .forEachOrdered(datatypeIri ->
            ontologyResources.addElement(
                selectResourceIriString(datatypeIri, ontologyIri, ViewerIdentifierFactory.Element.dataType),
                datatypeIri));

    ontologyResources.sortInAlphabeticalOrder();

    return ontologyResources;
  }

  private String selectResourceIriString(OwlAnnotationIri c, IRI ontologyIri,
      ViewerIdentifierFactory.Element element) {
    String annotationIri = c.getValue().getIri();

    return annotationIri.contains(ontologyIri)
        ? ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.internal, element)
        : ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.external, element);
  }

  private IRI findElementInOntology(IRI elementIri) {
    return entityIriToOntologyIriMap.get(elementIri);
  }

  private boolean trackingThePath(OntologyModule node, IRI ontologyIri, List<String> track, IRI elementIri) {
    if (node == null) {
      return false;
    }

    if (IRI.create(node.getIri()).equals(elementIri)) {
      track.add(node.getIri());
      return true;
    }

    if (node.getIri().equals(ontologyIri.toString())) {
      track.add(node.getIri());
      return true;
    }

    for (OntologyModule child : node.getSubModule()) {
      if (trackingThePath(child, ontologyIri, track, elementIri)) {
        track.add(0, node.getIri());
        return true;
      }
    }

    return false;
  }
}
