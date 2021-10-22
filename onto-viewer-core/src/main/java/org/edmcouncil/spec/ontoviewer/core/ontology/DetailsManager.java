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
import org.edmcouncil.spec.ontoviewer.core.changer.ChangerIriToLabel;
import org.edmcouncil.spec.ontoviewer.core.exception.NotFoundElementInOntologyException;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlDetails;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlGroupedDetails;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlListDetails;
import org.edmcouncil.spec.ontoviewer.core.model.module.FiboModule;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlAnnotationPropertyValue;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.OwlDataHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.generator.DescriptionGenerator;
import org.semanticweb.owlapi.model.IRI;
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

  private static final Logger LOG = LoggerFactory.getLogger(DetailsManager.class);
  private static final String DEFAULT_GROUP_NAME = "other";

  private final OntologyManager ontologyManager;
  private final OwlDataHandler dataHandler;
  private final ConfigurationService config;
  private final ChangerIriToLabel changerIriToLabel;
  private final DescriptionGenerator descriptionGenerator;

  public DetailsManager(OntologyManager ontologyManager, OwlDataHandler dataHandler,
      ConfigurationService config, ChangerIriToLabel changerIriToLabel,
      DescriptionGenerator descriptionGenerator) {
    this.ontologyManager = ontologyManager;
    this.dataHandler = dataHandler;
    this.config = config;
    this.changerIriToLabel = changerIriToLabel;
    this.descriptionGenerator = descriptionGenerator;
  }

  public OWLOntology getOntology() {
    return ontologyManager.getOntology();
  }

  public OwlDetails getDetailsByIri(String iriString) throws NotFoundElementInOntologyException {
    IRI iri = IRI.create(iriString);
    OwlListDetails result = null;

    // If '/' is at the end of the URL, we extract the ontology metadata
    if (iriString.endsWith("/")) {
      result = dataHandler.handleOntologyMetadata(iri, getOntology());
    } else {
      if (ontologyManager.getOntology().containsClassInSignature(iri, INCLUDED)) {
        result = dataHandler.handleParticularClass(iri, getOntology());
      } else if (ontologyManager.getOntology().containsDataPropertyInSignature(iri, INCLUDED)) {
        result = dataHandler.handleParticularDataProperty(iri, getOntology());
      } else if (ontologyManager.getOntology().containsObjectPropertyInSignature(iri, INCLUDED)) {
        result = dataHandler.handleParticularObjectProperty(iri, getOntology());
      } else if (ontologyManager.getOntology().containsIndividualInSignature(iri, INCLUDED)) {
        result = dataHandler.handleParticularIndividual(iri, getOntology());
      } else if (ontologyManager.getOntology().containsDatatypeInSignature(iri, INCLUDED)) {
        result = dataHandler.handleParticularDatatype(iri, getOntology());
      } else if (ontologyManager.getOntology()
          .containsAnnotationPropertyInSignature(iri, INCLUDED)) {
        result = dataHandler.handleParticularAnnotationProperty(iri, getOntology());
      }

      if (result != null) {
        result.setMaturityLevel(dataHandler.getMaturityLevel(iriString, getOntology()));
      }
    }

    if (result == null) {
      throw new NotFoundElementInOntologyException(
          "Not found element in ontology with IRI: " + iriString);
    }

    result.setIri(iriString);

    // Path to element in modules
    result.setLocationInModules(
        dataHandler.getElementLocationInModules(
            iriString,
            ontologyManager.getOntology()));

    if (config.getCoreConfiguration().isNotEmpty()) {
      CoreConfiguration coreConfiguration = config.getCoreConfiguration();
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
    return dataHandler.getAllModulesData(ontologyManager.getOntology());
  }

  private OwlGroupedDetails groupDetails(OwlListDetails owlDetails, CoreConfiguration cfg) {
    OwlGroupedDetails groupedDetails = new OwlGroupedDetails();
    Set<ConfigItem> groups = cfg.getConfiguration().get(ConfigKeys.GROUPS);

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

    groupedDetails.sortProperties(groups, cfg);

    //first must be sorted next we need to change keys
    groupedDetails = changerIriToLabel.changeIriKeysInGroupedDetails(groupedDetails);

    for (Map.Entry<String, Map<String, List<PropertyValue>>> entry : groupedDetails.getProperties()
        .entrySet()) {
      LOG.debug(entry.toString());
    }

    owlDetails.release();

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
    Set set = config.getCoreConfiguration()
        .getValue(ConfigKeys.PRIORITY_LIST);
    if (set == null) {
      return;
    }
    List prioritySortList = new LinkedList();
    result.sortProperties(prioritySortList);
  }

  private void addGeneratedDescription(OwlGroupedDetails groupedDetails) {
    Optional<List<OwlAnnotationPropertyValue>> description =
        descriptionGenerator.prepareDescriptionString(groupedDetails);

    description.ifPresent(descriptionValueList ->
        descriptionValueList.forEach(descriptionValue ->
            groupedDetails.addProperty(
                "Glossary",
                "generated description",
                descriptionValue)));
  }
}
