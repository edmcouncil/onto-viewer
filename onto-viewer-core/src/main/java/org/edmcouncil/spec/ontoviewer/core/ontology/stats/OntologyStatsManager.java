package org.edmcouncil.spec.ontoviewer.core.ontology.stats;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlListElementIndividualProperty;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.individual.IndividualDataHelper;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.module.ModuleHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Michal Daniel (michal.daniel@makolab.com)
 */
@Component
public class OntologyStatsManager {

  private static final String INSTANCE_KEY = ViewerIdentifierFactory.createId(
      ViewerIdentifierFactory.Type.function,
      OwlType.INSTANCES.name().toLowerCase());
  private static final Logger LOG = LoggerFactory.getLogger(OntologyStatsManager.class);

  private final ModuleHandler moduleHandler;
  private final IndividualDataHelper individualDataHandler;
  private final LabelProvider labelProvider;
  private final ApplicationConfigurationService applicationConfigurationService;

  private String moduleClassIri;
  private OntologyStatsMapped ontologyStatsMapped;

  public OntologyStatsManager(ModuleHandler moduleHandler,
      IndividualDataHelper individualDataHandler,
      LabelProvider labelProvider,
      ApplicationConfigurationService applicationConfigurationService) {
    this.moduleHandler = moduleHandler;
    this.individualDataHandler = individualDataHandler;
    this.labelProvider = labelProvider;
    this.applicationConfigurationService = applicationConfigurationService;
  }

  public OntologyStatsMapped getOntologyStats() {
    return ontologyStatsMapped;
  }

  public void clear() {
    ontologyStatsMapped = new OntologyStatsMapped();
  }

  public void generateStats(OWLOntology ontology) {
    Optional<OWLClass> clazzOpt = getModuleClazz(ontology);

    Map<String, Number> stats = new LinkedHashMap<>();
    Map<String, String> labels = new LinkedHashMap<>();

    if (clazzOpt.isPresent()) {
      OwlDetailsProperties<PropertyValue> indi = individualDataHandler.handleClassIndividuals(
          ontology, clazzOpt.get());
      if (indi.getProperties().isEmpty()) {
        //--noDomain
        String id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfDomain");
        stats.put(id, 0);
        labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
        //--numberOfModule
        id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfModule");
        stats.put(id, 0);
        labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
      }

      Set<String> modulesIriSet = new HashSet<>();
      try {
        indi.getProperties().get(INSTANCE_KEY).stream()
            .map((propertyValue) -> (OwlListElementIndividualProperty) propertyValue)
            .map((individualProperty) -> (String) individualProperty.getValue().getIri())
            .forEachOrdered((elIri) -> {
              modulesIriSet.add(elIri);
            });
      } catch (NullPointerException e) {
        LOG.info("The ontology has not modules defined");
      }

      List<String> rootModulesIris = moduleHandler.getRootModulesIris(modulesIriSet, ontology);
      //--numberOfDomain
      String id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats,
          "numberOfDomain");
      stats.put(id, rootModulesIris.size());
      labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
      //--numberOfModule
      id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfModule");
      stats.put(id, modulesIriSet.size() - rootModulesIris.size());
      labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
    } else {
      //--numberOfDomain
      String id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfDomain");
      stats.put(id, 0);
      labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
      //--numberOfModule
      id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfModule");
      stats.put(id, 0);
      labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
    }

    //--numberOfClass
    String id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats,
        "numberOfClass");
    stats.put(id, ontology.classesInSignature(Imports.INCLUDED).count());
    labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
    //--numberOfObjectProperty
    id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats,
        "numberOfObjectProperty");
    stats.put(id, ontology.objectPropertiesInSignature(Imports.INCLUDED).count());
    labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
    //--numberOfDataPropertyy
    id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats,
        "numberOfDataProperty");
    stats.put(id, ontology.dataPropertiesInSignature(Imports.INCLUDED).count());
    labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
    //--numberOfOAnnotationProperty
    id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats,
        "numberOfAnnotationProperty");
    stats.put(id, ontology.annotationPropertiesInSignature(Imports.INCLUDED).count());
    labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
    //--numberOfIndividuals
    id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats,
        "numberOfIndividuals");
    stats.put(id, ontology.individualsInSignature(Imports.INCLUDED).count());
    labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
    //--numberOfAxiom
    id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfAxiom");
    stats.put(id, ontology.axioms(Imports.INCLUDED).count());
    labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));
    //--numberOfDatatype
    id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfDatatype");
    stats.put(id, ontology.datatypesInSignature(Imports.INCLUDED).count());
    labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));

    OWLOntologyManager manager = ontology.getOWLOntologyManager();
    var ontologies = manager.ontologies().collect(Collectors.toSet());

    //--numberOfOntologies
    id = ViewerIdentifierFactory.createId(ViewerIdentifierFactory.Type.stats, "numberOfOntologies");
    stats.put(id, ontologies.size());
    labels.put(id, labelProvider.getLabelOrDefaultFragment(IRI.create(id)));

    LOG.debug(stats.toString());
    OntologyStatsMapped osm = new OntologyStatsMapped();
    osm.setLabels(labels);
    osm.setStats(stats);

    ontologyStatsMapped = osm;
  }

  private Optional<OWLClass> getModuleClazz(OWLOntology ontology) {
    IRI moduleIri = IRI.create(getModuleClassIri());
    return ontology
        .classesInSignature(Imports.INCLUDED)
        .filter(c -> c.getIRI().equals(moduleIri))
        .findFirst();
  }

  private String getModuleClassIri() {
    if (moduleClassIri == null) {
      this.moduleClassIri = applicationConfigurationService
          .getConfigurationData()
          .getOntologiesConfig()
          .getModuleClassIri();
    }
    return moduleClassIri;
  }
}
