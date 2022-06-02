package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
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
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;
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

  private static final Logger LOG = LoggerFactory.getLogger(DataHandler.class);

  private final AnnotationsDataHandler annotationsDataHandler;
  private final CustomDataFactory customDataFactory;
  private final ModuleHandler moduleHandler;
  private final OntologyManager ontologyManager;

  private String resourceInternal;
  private String resourceExternal;

  private Map<String, OntologyResources> resources = null;

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

          OntologyResources ontologyResources = getOntologyResources(currentOntologyIri.toString(), ontology);
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

  public List<OntologyModule> getAllModules() {
    return moduleHandler.getModules();
  }

  public List<String> getRootModulesIris(Set<String> modulesIriSet, OWLOntology ontology) {
    Map<String, Integer> referenceCount = new LinkedHashMap<>();
    modulesIriSet.forEach((mIri) -> {
      Set<String> hasPartModules = getHasPartElements(IRI.create(mIri), ontology);
      referenceCount.putIfAbsent(mIri, 0);
      hasPartModules.forEach((partModule) -> {
        Integer c = referenceCount.getOrDefault(partModule, 0);
        c++;
        referenceCount.put(partModule, c);
      });
    });
    return referenceCount.entrySet()
        .stream()
        .filter(r -> r.getValue() == 0)
        .map(Entry::getKey)
        .collect(Collectors.toList());
  }

  public OntologyResources getOntologyResources(String iri, OWLOntology ontology) {
    if (resources == null) {
      loadAllOntologyResources(ontology);
    }

    return resources.get(iri);
  }

  public Set<String> getHasPartElements(IRI iri, OWLOntology ontology) {
    OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
    Optional<OWLNamedIndividual> individual = ontology
        .individualsInSignature(Imports.INCLUDED)
        .filter(c -> c.getIRI().equals(iri))
        .findFirst();
    if (individual.isEmpty()) {
      return new HashSet<>(0);
    }
    Iterator<OWLAnnotation> iteratorAnnotation = EntitySearcher
        .getAnnotations(
            individual.get(),
            ontology.importsClosure(),
            dataFactory.getOWLAnnotationProperty(IRI.create("http://purl.org/dc/terms/hasPart")))
        .iterator();

    Set<String> result = new LinkedHashSet<>();
    while (iteratorAnnotation.hasNext()) {
      OWLAnnotation annotation = iteratorAnnotation.next();
      String s = annotation.annotationValue().toString();
      result.add(s);
    }

    return result;
  }

  public void populateOntologyResources(OWLOntology ontology) {
    // TODO: Make loadAllOntologyResources and setOntologyResources private and use this method
    //       instead
    this.resources = loadAllOntologyResources(ontology);
    this.moduleHandler.updateModules();
  }

  private Map<String, OntologyResources> loadAllOntologyResources(OWLOntology ontology) {
    OWLOntologyManager manager = ontology.getOWLOntologyManager();
    Map<String, OntologyResources> allResources = new HashMap<>();

    completeResourceKeys();

    manager.ontologies().collect(Collectors.toSet()).forEach((owlOntology) -> {
      OntologyResources ontoResources = extractOntologyResources(owlOntology);

      if (ontoResources != null) {
        String ontIri = owlOntology.getOntologyID().getOntologyIRI().get().toString();
        if (!ontIri.equals("https://spec.edmcouncil.org/fibo/ontology")) {
          allResources.put(ontIri, ontoResources);
        }
      }
    });
    return allResources;
  }

  private OntologyResources extractOntologyResources(OWLOntology selectedOntology) {
    OntologyResources ontoResources = new OntologyResources();
    Optional<IRI> opt = selectedOntology.getOntologyID().getOntologyIRI();
    IRI ontologyIri;
    if (opt.isPresent()) {
      ontologyIri = opt.get();
    } else {
      opt = selectedOntology.getOntologyID().getDefaultDocumentIRI();
      if (opt.isPresent()) {
        ontologyIri = opt.get();
        LOG.debug("IRI for this ontology doesn't exist, use Default Document IRI {}", ontologyIri);
      } else {
        LOG.debug("Ontology doesn't have any iri to present... Ontology ID: {}",
            selectedOntology.getOntologyID().toString());
        return null;
      }
    }

    selectedOntology.annotationPropertiesInSignature()
        .map(c -> {
          String istring = c.getIRI().toString();
          OwlAnnotationIri pv = customDataFactory.createAnnotationIri(istring);
          return pv;
        })
        .forEachOrdered(c -> ontoResources
            .addElement(selectResourceIriString(c, ontologyIri,
                ViewerIdentifierFactory.Element.annotationProperty), c));

    selectedOntology.classesInSignature()
        .map(c -> {
          String istring = c.getIRI().toString();
          OwlAnnotationIri pv = customDataFactory.createAnnotationIri(istring);
          return pv;
        })
        .forEachOrdered(c -> ontoResources
            .addElement(
                selectResourceIriString(c, ontologyIri, ViewerIdentifierFactory.Element.clazz), c)
        );

    selectedOntology.dataPropertiesInSignature()
        .map(c -> {
          String istring = c.getIRI().toString();
          OwlAnnotationIri pv = customDataFactory.createAnnotationIri(istring);
          return pv;
        })
        .forEachOrdered(c -> ontoResources
            .addElement(selectResourceIriString(c, ontologyIri,
                ViewerIdentifierFactory.Element.dataProperty), c));

    selectedOntology.objectPropertiesInSignature()
        .map(c -> {
          String istring = c.getIRI().toString();
          OwlAnnotationIri pv = customDataFactory.createAnnotationIri(istring);
          return pv;
        })
        .forEachOrdered(c -> ontoResources
            .addElement(selectResourceIriString(c, ontologyIri,
                ViewerIdentifierFactory.Element.objectProperty), c));

    selectedOntology.individualsInSignature()
        .map(individual -> customDataFactory.createAnnotationIri(individual.getIRI().toString()))
        .forEachOrdered(individual
            -> ontoResources.addElement(
            selectResourceIriString(
                individual,
                ontologyIri,
                ViewerIdentifierFactory.Element.instance),
            individual
        ));

    selectedOntology.datatypesInSignature()
        .map(c -> {
          String istring = c.getIRI().toString();
          OwlAnnotationIri pv = customDataFactory.createAnnotationIri(istring);
          return pv;
        })
        .forEachOrdered(c -> ontoResources
            .addElement(selectResourceIriString(c, ontologyIri,
                ViewerIdentifierFactory.Element.dataType), c));

    ontoResources.sortInAlphabeticalOrder();

    return ontoResources;
  }

  private void completeResourceKeys() {

    resourceInternal = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.internal,
        ViewerIdentifierFactory.Element.empty);
    LOG.debug("Internal resource iri: {}", resourceInternal);

    resourceExternal = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.external,
        ViewerIdentifierFactory.Element.empty);
    LOG.debug("External resource iri: {}", resourceExternal);

  }

  private String selectResourceIriString(OwlAnnotationIri c, IRI ontologyIri,
      ViewerIdentifierFactory.Element element) {
    String annotationIri = c.getValue().getIri();

    return annotationIri.contains(ontologyIri)
        ? ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.internal, element)
        : ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.external, element);
  }

  public List<String> getElementLocationInModules(String elementIri) {
    OWLOntology ontology = ontologyManager.getOntology();
    List<String> result = new LinkedList<>();
    if (resources == null) {
      loadAllOntologyResources(ontology);
    }
    var allModules = getAllModules();

    if (allModules.isEmpty()) {
      return result;
    }

    String ontologyIri = findElementInOntology(elementIri);

    ontologyIri = ontologyIri == null ? elementIri : ontologyIri;

    LOG.debug("Element found in ontology {}", ontologyIri);
    if (ontologyIri != null) {
      for (OntologyModule module : allModules) {
        if (trackingThePath(module, ontologyIri, result, elementIri)) {
          LOG.debug("[FIBO Data Handler] Location Path {}", Arrays.toString(result.toArray()));
          return result;
        }
      }
    }

    return result;
  }

  /**
   * @return ontology iri where the element is present
   */
  private String findElementInOntology(String elementIri) {
    // https://spec.edmcouncil.org/fibo/ontology
    String ontologyIri = null;
    for (Map.Entry<String, OntologyResources> entry : resources.entrySet()) {
      for (Map.Entry<String, List<PropertyValue>> entryResource : entry.getValue().getResources()
          .entrySet()) {
        if (entryResource.getKey().contains(resourceInternal)) {
          for (PropertyValue propertyValue : entryResource.getValue()) {
            OwlAnnotationIri annotation = (OwlAnnotationIri) propertyValue;
            if (annotation.getValue().getIri().equals(elementIri)) {
              if (elementIri.contains(entry.getKey())) {
                ontologyIri = entry.getKey();
                break;
              }
            }
          }
        }

      }
    }
    return ontologyIri;
  }

  private Boolean trackingThePath(OntologyModule node, String ontologyIri, List<String> track,
      String elementIri) {

    if (node == null) {
      return false;
    }

    if (node.getIri().equals(elementIri)) {
      track.add(node.getIri());
      return true;
    }

    if (node.getIri().equals(ontologyIri)) {
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
