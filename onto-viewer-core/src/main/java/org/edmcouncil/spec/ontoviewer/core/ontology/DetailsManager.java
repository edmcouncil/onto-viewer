package org.edmcouncil.spec.ontoviewer.core.ontology;

import static org.semanticweb.owlapi.model.parameters.Imports.INCLUDED;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigKeys;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.CoreConfiguration;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.impl.element.GroupsItem;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.exception.NotFoundElementInOntologyException;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlDetails;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlGroupedDetails;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlListDetails;
import org.edmcouncil.spec.ontoviewer.core.model.module.FiboModule;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.OwlDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.generator.DescriptionGenerator;
import org.edmcouncil.spec.ontoviewer.core.service.ChangerIriToLabelService;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Michał Daniel (michal.daniel@makolab.com)
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
@Component
public class DetailsManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(DetailsManager.class);
  private static final String DEFAULT_GROUP_NAME = "other";

  private final OntologyManager ontologyManager;
  private final OwlDataHandler dataHandler;
  private final ConfigurationService configurationService;
  private final ChangerIriToLabelService changerIriToLabelService;
  private final DescriptionGenerator descriptionGenerator;

  public DetailsManager(OntologyManager ontologyManager, OwlDataHandler dataHandler,
      ConfigurationService configurationService, ChangerIriToLabelService changerIriToLabelService,
      DescriptionGenerator descriptionGenerator) {
    this.ontologyManager = ontologyManager;
    this.dataHandler = dataHandler;
    this.configurationService = configurationService;
    this.changerIriToLabelService = changerIriToLabelService;
    this.descriptionGenerator = descriptionGenerator;
  }

  public OWLOntology getOntology() {
    return ontologyManager.getOntology();
  }

  public OwlDetails getEntityDetails(OWLEntity owlEntity)
      throws NotFoundElementInOntologyException {
    var entityIri = owlEntity.getIRI();
    EntityType<?> entityType = owlEntity.getEntityType();
    OwlListDetails result = null;

    if (entityType == EntityType.CLASS) {
      result = dataHandler.handleParticularClass(owlEntity.asOWLClass());
    } else if (entityType == EntityType.NAMED_INDIVIDUAL) {
      result = dataHandler.handleParticularIndividual(owlEntity.asOWLNamedIndividual());
    } else if (entityType == EntityType.OBJECT_PROPERTY) {
      result = dataHandler.handleParticularObjectProperty(owlEntity.asOWLObjectProperty());
    } else if (entityType == EntityType.DATA_PROPERTY) {
      result = dataHandler.handleParticularDataProperty(owlEntity.asOWLDataProperty());
    } else if (entityType == EntityType.DATATYPE) {
      result = dataHandler.handleParticularDatatype(owlEntity.asOWLDatatype());
    }

    if (result == null) {
      throw new NotFoundElementInOntologyException(
          "Not found element in ontology with IRI: " + entityIri.toString());
    }

    result.setIri(entityIri.toString());

    // Path to element in modules
    if (getSetting(ConfigKeys.LOCATION_IN_MODULES_ENABLED)) {
      result.setLocationInModules(
          dataHandler.getElementLocationInModules(
              entityIri.getIRIString(),
              ontologyManager.getOntology()));
    }

    if (configurationService.getCoreConfiguration().isNotEmpty()) {
      CoreConfiguration coreConfiguration = configurationService.getCoreConfiguration();
      if (coreConfiguration.isGrouped()) {
        OwlGroupedDetails newResult = groupDetails(result, coreConfiguration);
        addGeneratedDescription(newResult);
        return newResult;
      } else {
        sortResults(result);
      }
    }

    return result;
  }

  public OwlDetails getDetailsByIri(String iriString) throws NotFoundElementInOntologyException {
    IRI iri = IRI.create(iriString);
    OwlListDetails result = null;

    // If '/' is at the end of the URL, we extract the ontology metadata
    if (iriString.endsWith("/")) {
      result = dataHandler.handleOntologyMetadata(iri, getOntology());
    } else {
      if (ontologyManager.getOntology().containsClassInSignature(iri, INCLUDED)) {
        result = dataHandler.handleParticularClass(iri);
      } else if (ontologyManager.getOntology().containsDataPropertyInSignature(iri, INCLUDED)) {
        result = dataHandler.handleParticularDataProperty(iri);
      } else if (ontologyManager.getOntology().containsObjectPropertyInSignature(iri, INCLUDED)) {
        result = dataHandler.handleParticularObjectProperty(iri);
      } else if (ontologyManager.getOntology().containsIndividualInSignature(iri, INCLUDED)) {
        result = dataHandler.handleParticularIndividual(iri);
      } else if (ontologyManager.getOntology().containsDatatypeInSignature(iri, INCLUDED)) {
        result = dataHandler.handleParticularDatatype(iri);
      } else if (ontologyManager.getOntology()
          .containsAnnotationPropertyInSignature(iri, INCLUDED)) {
        result = dataHandler.handleParticularAnnotationProperty(iri, getOntology());
      }

      if (result != null) {
        result.setMaturityLevel(dataHandler.getMaturityLevel(iriString));
      }
    }

    if (result == null) {
      throw new NotFoundElementInOntologyException(
          "Not found element in ontology with IRI: " + iriString);
    }

    result.setIri(iriString);

    // Path to element in modules
    if (getSetting(ConfigKeys.LOCATION_IN_MODULES_ENABLED)) {
      result.setLocationInModules(
          dataHandler.getElementLocationInModules(
              iriString,
              ontologyManager.getOntology()));
    }

    if (configurationService.getCoreConfiguration().isNotEmpty()) {
      CoreConfiguration coreConfiguration = configurationService.getCoreConfiguration();
      if (coreConfiguration.isGrouped()) {
        OwlGroupedDetails newResult = groupDetails(result, coreConfiguration);
        addGeneratedDescription(newResult);
        return newResult;
      } else {
        sortResults(result);
      }
    }

    return result;
  }

  public List<FiboModule> getAllModulesData() {
    return dataHandler.getAllModules();
  }

  private OwlGroupedDetails groupDetails(OwlListDetails owlDetails,
      CoreConfiguration configuration) {
    var groupedDetails = new OwlGroupedDetails();
    var groups = configuration.getConfiguration().get(ConfigKeys.GROUPS);

    for (Map.Entry<String, List<PropertyValue>> entry : owlDetails.getProperties().entrySet()) {
      String propertyKey = entry.getKey();

      String groupName = getGroupName(groups, propertyKey);
      groupName = groupName == null ? DEFAULT_GROUP_NAME : groupName;
      for (PropertyValue property : entry.getValue()) {
        groupedDetails.addProperty(groupName, propertyKey, property);
      }
    }
    groupedDetails.setTaxonomy(owlDetails.getTaxonomy());
    groupedDetails.setLabel(owlDetails.getLabel());
    groupedDetails.setIri(owlDetails.getIri());
    groupedDetails.setLocationInModules(owlDetails.getLocationInModules());
    groupedDetails.setGraph(owlDetails.getGraph());
    groupedDetails.setqName(owlDetails.getqName());
    groupedDetails.setMaturityLevel(owlDetails.getMaturityLevel());
    groupedDetails.sortProperties(groups, configuration);

    // first must be sorted next we need to change keys
    groupedDetails = changerIriToLabelService.changeIriKeysInGroupedDetails(groupedDetails);

    // owlDetails.release(); // TODO: Remove
    return groupedDetails;
  }

  private String getGroupName(Set<ConfigItem> groups, String propertyKey) {
    String result = null;
    if (propertyKey == null || propertyKey.isEmpty()) {
      return result;
    }
    for (ConfigItem g : groups) {
      GroupsItem group = (GroupsItem) g;
      if (group.getElements() != null && group.getElements().size() > 0) {
        if (group.contains(propertyKey)) {
          return group.getName();
        }
      }
    }
    return result;
  }

  private void sortResults(OwlListDetails result) {
    var set = configurationService.getCoreConfiguration().getValue(ConfigKeys.PRIORITY_LIST);
    if (set == null) {
      return;
    }
    List prioritySortList = new LinkedList();
    result.sortProperties(prioritySortList);
  }

  private void addGeneratedDescription(OwlGroupedDetails groupedDetails) {
    Optional<List<OwlAnnotationPropertyValue>> description
        = descriptionGenerator.prepareDescriptionString(groupedDetails);

    description.ifPresent(descriptionValueList
        -> descriptionValueList.forEach(descriptionValue
        -> groupedDetails.addProperty(
        "Glossary",
        "generated description",
        descriptionValue)));
  }

  private boolean getSetting(String key) {
    return (boolean) configurationService.getCoreConfiguration().getOntologyHandling().get(key);
  }
}
