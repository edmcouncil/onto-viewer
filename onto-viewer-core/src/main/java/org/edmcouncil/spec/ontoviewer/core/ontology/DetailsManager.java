package org.edmcouncil.spec.ontoviewer.core.ontology;

import static org.semanticweb.owlapi.model.parameters.Imports.INCLUDED;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.exception.NotFoundElementInOntologyException;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlDetails;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlGroupedDetails;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlListDetails;
import org.edmcouncil.spec.ontoviewer.core.model.module.OntologyModule;
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
  private static final String NOT_FOUND_ENTITY_MESSAGE = "Not found element with IRI: %s";

  private final ApplicationConfigurationService applicationConfigurationService;
  private final OntologyManager ontologyManager;
  private final OwlDataHandler dataHandler;
  private final ChangerIriToLabelService changerIriToLabelService;
  private final DescriptionGenerator descriptionGenerator;

  public DetailsManager(ApplicationConfigurationService applicationConfigurationService,
      OntologyManager ontologyManager,
      OwlDataHandler dataHandler,
      ChangerIriToLabelService changerIriToLabelService,
      DescriptionGenerator descriptionGenerator) {
    this.applicationConfigurationService = applicationConfigurationService;
    this.ontologyManager = ontologyManager;
    this.dataHandler = dataHandler;
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
      throw new NotFoundElementInOntologyException(String.format(NOT_FOUND_ENTITY_MESSAGE, entityIri.toString()));
    }

    result.setIri(entityIri.toString());

    // Path to element in modules
    var locationInModulesEnabled = applicationConfigurationService.getConfigurationData()
        .getToolkitConfig()
        .isLocationInModulesEnabled();
    if (locationInModulesEnabled) {
      result.setLocationInModules(
          dataHandler.getElementLocationInModules(
              entityIri.getIRIString()));
    }

    ConfigurationData coreConfiguration = applicationConfigurationService.getConfigurationData();
    if (applicationConfigurationService.hasConfiguredGroups()) {
      OwlGroupedDetails newResult = groupDetails(result, coreConfiguration);
      addGeneratedDescription(newResult);
      return newResult;
    } else {
      sortResults(result);
    }

    return result;
  }

  public OwlDetails getDetailsByIri(String iriString) throws NotFoundElementInOntologyException {
    IRI iri = IRI.create(iriString);
    OwlListDetails result;

    // If '/' is at the end of the URL, we extract the ontology metadata
    if (iriString.endsWith("/")) {
      result = dataHandler.handleOntologyMetadata(iri);
    } else {
      result = getEntityListDetails(iri);
    }

    if (result == null) {
      throw new NotFoundElementInOntologyException(String.format(NOT_FOUND_ENTITY_MESSAGE, iriString));
    }

    return setGroupedDetailsIfEnabled(iriString, result);
  }

  public List<OntologyModule> getAllModulesData() {
    return dataHandler.getAllModules();
  }

  public OwlDetails getEntityDetailsByIri(String iriString) throws NotFoundElementInOntologyException {
    IRI iri = IRI.create(iriString);
    OwlListDetails resultDetails = dataHandler.handleOntologyMetadata(iri);
    if (resultDetails == null) {
      resultDetails = getEntityListDetails(iri);

      if (resultDetails == null) {
        throw new NotFoundElementInOntologyException(String.format(NOT_FOUND_ENTITY_MESSAGE, iri));
      }
    }

    return setGroupedDetailsIfEnabled(iriString, resultDetails);
  }

  private OwlDetails setGroupedDetailsIfEnabled(String iriString, OwlListDetails result) {
    var locationInModulesEnabled = applicationConfigurationService.getConfigurationData()
        .getToolkitConfig()
        .isLocationInModulesEnabled();
    if (locationInModulesEnabled) {
      result.setLocationInModules(dataHandler.getElementLocationInModules(iriString));
    }

    ConfigurationData configurationData = applicationConfigurationService.getConfigurationData();
    if (applicationConfigurationService.hasConfiguredGroups()) {
      OwlGroupedDetails newResult = groupDetails(result, configurationData);
      addGeneratedDescription(newResult);
      return newResult;
    } else {
      sortResults(result);
    }

    return result;
  }

  private OwlListDetails getEntityListDetails(IRI iri) {
    OwlListDetails result = null;

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
    } else if (ontologyManager.getOntology().containsAnnotationPropertyInSignature(iri, INCLUDED)) {
      result = dataHandler.handleParticularAnnotationProperty(iri, getOntology());
    }

    if (result != null) {
      result.setIri(iri.toString());
      result.setMaturityLevel(dataHandler.getMaturityLevel(iri));
    }

    return result;
  }

  private OwlGroupedDetails groupDetails(OwlListDetails owlDetails, ConfigurationData configurationData) {
    var groupedDetails = new OwlGroupedDetails();
    var groups = configurationData.getGroupsConfig().getGroups();

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
    groupedDetails.sortProperties(groups);

    // first must be sorted next we need to change keys
    groupedDetails = changerIriToLabelService.changeIriKeysInGroupedDetails(groupedDetails);

    // owlDetails.release(); // TODO: Remove
    return groupedDetails;
  }

  private String getGroupName(Map<String, List<String>> groups, String propertyKey) {
    // TODO: groups as set of strings?
    if (propertyKey == null || propertyKey.isEmpty()) {
      return null;
    }

    for (Entry<String, List<String>> groupEntry : groups.entrySet()) {
      if (groupEntry.getValue() != null && groupEntry.getValue().contains(propertyKey)) {
        return groupEntry.getKey();
      }
    }

    return null;
  }

  private void sortResults(OwlListDetails result) {
    var priorityList =
        applicationConfigurationService.getConfigurationData().getGroupsConfig().getPriorityList();
    if (priorityList == null) {
      return;
    }
    List<String> prioritySortList = new LinkedList<>();
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
}
