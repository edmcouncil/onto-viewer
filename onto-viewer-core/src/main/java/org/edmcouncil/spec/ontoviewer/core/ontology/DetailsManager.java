package org.edmcouncil.spec.ontoviewer.core.ontology;

import static org.semanticweb.owlapi.model.parameters.Imports.INCLUDED;
;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.ConfigurationData;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.exception.NotFoundElementInOntologyException;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlDetails;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlGroupedDetails;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlListDetails;
import org.edmcouncil.spec.ontoviewer.core.model.module.OntologyModule;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.DeprecatedHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.VersionIriHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.data.AnnotationsDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.classes.ClassHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.module.ModuleHelper;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.metadata.MetadataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.data.DataPropertyHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.data.DataTypeHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.individual.IndividualHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.data.ObjectDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.generator.DescriptionGenerator;
import org.edmcouncil.spec.ontoviewer.core.service.ChangerIriToLabelService;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.NodeID;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
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
  private static final String NOT_FOUND_BNODE_MESSAGE = "Not found element with nodeID: %s";

  private final ApplicationConfigurationService applicationConfigurationService;
  private final OntologyManager ontologyManager;
  private final ClassHandler particularClassHandler;
  private final ChangerIriToLabelService changerIriToLabelService;
  private final DescriptionGenerator descriptionGenerator;
  private final ModuleHelper moduleHelper;
  private final AnnotationsDataHandler annotationsDataHandler;
  private final DataTypeHandler particularDataTypeHandler;
  private final ObjectDataHandler particularObjectPropertyHandler;
  private final DataPropertyHandler particularDataPropertyHandler;
  private final IndividualHandler particularIndividualHandler;
  private final MetadataHandler metadataHandler;
  private final DeprecatedHandler deprecatedHandler;
  private final VersionIriHandler versionIriHandler;

  public DetailsManager(ApplicationConfigurationService applicationConfigurationService,
      OntologyManager ontologyManager,
      ClassHandler particularClassHandler,
      ChangerIriToLabelService changerIriToLabelService,
      DescriptionGenerator descriptionGenerator,
      ModuleHelper moduleHelper,
      AnnotationsDataHandler annotationsDataHandler,
      DataTypeHandler particularDataTypeHandler,
      ObjectDataHandler particularObjectPropertyHandler,
      DataPropertyHandler particularDataPropertyHandler,
      IndividualHandler particularIndividualHandler,
      MetadataHandler metadataHandler,
      DeprecatedHandler deprecatedHandler,
      VersionIriHandler versionIriHandler) {
    this.applicationConfigurationService = applicationConfigurationService;
    this.ontologyManager = ontologyManager;
    this.particularClassHandler = particularClassHandler;
    this.changerIriToLabelService = changerIriToLabelService;
    this.descriptionGenerator = descriptionGenerator;
    this.moduleHelper = moduleHelper;
    this.annotationsDataHandler = annotationsDataHandler;
    this.particularDataTypeHandler = particularDataTypeHandler;
    this.particularObjectPropertyHandler = particularObjectPropertyHandler;
    this.particularDataPropertyHandler = particularDataPropertyHandler;
    this.particularIndividualHandler = particularIndividualHandler;
    this.metadataHandler = metadataHandler;
    this.deprecatedHandler = deprecatedHandler;
    this.versionIriHandler = versionIriHandler;
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
      result = particularClassHandler.handle(owlEntity.asOWLClass());
    } else if (entityType == EntityType.NAMED_INDIVIDUAL) {
      result = particularIndividualHandler.handle(owlEntity.asOWLNamedIndividual());
    } else if (entityType == EntityType.OBJECT_PROPERTY) {
      result = particularObjectPropertyHandler.handle(owlEntity.asOWLObjectProperty());
    } else if (entityType == EntityType.DATA_PROPERTY) {
      result = particularDataPropertyHandler.handleParticularDataProperty(owlEntity.asOWLDataProperty());
    } else if (entityType == EntityType.DATATYPE) {
      result = particularDataTypeHandler.handle(owlEntity.asOWLDatatype());
    }

    if (result == null) {
      throw new NotFoundElementInOntologyException(
          String.format(NOT_FOUND_ENTITY_MESSAGE, entityIri.toString()));
    }

    result.setIri(entityIri.toString());

    // Path to element in modules
    var locationInModulesEnabled = applicationConfigurationService.getConfigurationData()
        .getToolkitConfig()
        .isLocationInModulesEnabled();
    if (locationInModulesEnabled) {
      result.setLocationInModules(moduleHelper.getElementLocationInModules(entityIri));
    }
    result.setVersionIri(versionIriHandler.getVersionIri(entityIri));
    result.setMaturityLevel(moduleHelper.getMaturityLevel(entityIri));
    result.setDeprecated(deprecatedHandler.getDeprecatedForEntity(entityIri));

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
      result = metadataHandler.handle(iri);
    } else {
      result = getEntityListDetails(iri);
    }

    if (result == null) {
      throw new NotFoundElementInOntologyException(
          String.format(NOT_FOUND_ENTITY_MESSAGE, iriString));
    }
    result.setDeprecated(deprecatedHandler.getDeprecatedForEntity(iri));
    result.setVersionIri(versionIriHandler.getVersionIri(iri));
    return setGroupedDetailsIfEnabled(iri, result);
  }

  public List<OntologyModule> getAllModulesData() {
    return moduleHelper.getAllModules();
  }

  public OwlDetails getEntityDetailsByIri(String iriString) throws NotFoundElementInOntologyException {
    OwlListDetails resultDetails;
    IRI iri;
    NodeID nodeID;
    
    if (isIri(iriString)){
      iri = IRI.create(iriString);
      resultDetails = metadataHandler.handle(iri);

      if (resultDetails == null) {
        resultDetails = getEntityListDetails(iri); 
        
        if (resultDetails == null) {
          throw new NotFoundElementInOntologyException(String.format(NOT_FOUND_ENTITY_MESSAGE, iri));
        }
      }
      resultDetails.setDeprecated(deprecatedHandler.getDeprecatedForEntity(iri));
      resultDetails.setVersionIri(versionIriHandler.getVersionIri(iri));
      OwlDetails owlDetails = setGroupedDetailsIfEnabled(iri, resultDetails);
      return owlDetails;

    } else if (isBlankNode(iriString)) { 
      nodeID = NodeID.getNodeID(iriString);
      resultDetails = getBlankNodeListDetails(nodeID);

      if (resultDetails == null) {
        throw new NotFoundElementInOntologyException(String.format(NOT_FOUND_BNODE_MESSAGE, nodeID));
      }
      
      resultDetails.setDeprecated(deprecatedHandler.getDeprecatedForEntity(IRI.create(iriString)));
      resultDetails.setVersionIri(versionIriHandler.getVersionIri(IRI.create(iriString)));
      OwlDetails owlDetails = setGroupedDetailsIfEnabled(IRI.create(iriString), resultDetails);
      return owlDetails;
    }
    return null;
  }

  
  
  private boolean isBlankNode(String blankNodeId) {
    return blankNodeId.startsWith("_:genid");
  }

  private boolean isIri(String iriString) {
    return iriString.startsWith("http://") || iriString.startsWith("https://");
  }

  private OwlDetails setGroupedDetailsIfEnabled(IRI iri, OwlListDetails result) {
    var locationInModulesEnabled = applicationConfigurationService.getConfigurationData()
        .getToolkitConfig()
        .isLocationInModulesEnabled();
    if (locationInModulesEnabled) {
      result.setLocationInModules(moduleHelper.getElementLocationInModules(iri));
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
      result = particularClassHandler.handle(iri);
      if (ontologyManager.getOntology().containsIndividualInSignature(iri, INCLUDED)) {
        var individualResult = particularIndividualHandler.handle(iri);
        mergeProperties(result, individualResult);
      }
    } else if (ontologyManager.getOntology().containsDataPropertyInSignature(iri, INCLUDED)) {
      result = particularDataPropertyHandler.handleParticularDataProperty(iri);
    } else if (ontologyManager.getOntology().containsObjectPropertyInSignature(iri, INCLUDED)) {
      result = particularObjectPropertyHandler.handle(iri);
    } else if (ontologyManager.getOntology().containsIndividualInSignature(iri, INCLUDED)) {
      result = particularIndividualHandler.handle(iri);
    } else if (ontologyManager.getOntology().containsDatatypeInSignature(iri, INCLUDED)) {
      result = particularDataTypeHandler.handle(iri);
    } else if (ontologyManager.getOntology().containsAnnotationPropertyInSignature(iri, INCLUDED)) {
      result = annotationsDataHandler.handleParticularAnnotationProperty(iri, getOntology());
    }

    if (result != null) {
      result.setIri(iri.toString());
      result.setMaturityLevel(moduleHelper.getMaturityLevel(iri));
    }

    return result;
  }


  private OwlListDetails getBlankNodeListDetails(NodeID nodeID) {
    OwlListDetails result = null;
    Optional<OWLAnonymousIndividual> specificBNode = getSpecificBNode(ontologyManager.getOntology(), nodeID);
    
    if (!specificBNode.isPresent()){
      return result;
    }

    OWLAnonymousIndividual owlAnonymousIndividual = specificBNode.get(); 
    
    result = particularClassHandler.handle(owlAnonymousIndividual);

    if (result != null) {
      result.setIri(nodeID.getID());
      result.setMaturityLevel(moduleHelper.getMaturityLevel(owlAnonymousIndividual));
    }
    
    return result;
  }
  
  private Optional<OWLAnonymousIndividual> getSpecificBNode(OWLOntology ontology, NodeID nodeID) {
    for (var currentOntology : ontology.importsClosure().collect(Collectors.toSet())) {
      Set<OWLAnonymousIndividual> matchedIndividuals = currentOntology.referencedAnonymousIndividuals()
              .filter(individual -> individual.toStringID().equals(nodeID.getID()))
              .collect(Collectors.toSet());

      if (!matchedIndividuals.isEmpty()) {
        return matchedIndividuals.stream().findFirst();
      }
    }
    return Optional.empty();
  }
  
  private OwlGroupedDetails groupDetails(OwlListDetails owlDetails,
      ConfigurationData configurationData) {
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
    groupedDetails.setDeprecated(owlDetails.getDeprecated());
    groupedDetails.setVersionIri(owlDetails.getVersionIri());
    groupedDetails.sortProperties(groups);

    if (configurationData.getToolkitConfig().isRunningToolkit()) {
      groupedDetails.setToolkitProperties(new HashMap<>(groupedDetails.getProperties()));
    }

    // first must be sorted next we need to change keys
    groupedDetails = changerIriToLabelService.changeIriKeysInGroupedDetails(groupedDetails);

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

    description.ifPresent(descriptionValueList -> descriptionValueList.forEach(
        descriptionValue -> groupedDetails.addProperty(
            "Glossary",
            "generated description",
            descriptionValue)));
  }

  private void mergeProperties(OwlListDetails result, OwlListDetails otherResult) {
    result.addAllProperties(otherResult.getAllProperties());
  }
}
