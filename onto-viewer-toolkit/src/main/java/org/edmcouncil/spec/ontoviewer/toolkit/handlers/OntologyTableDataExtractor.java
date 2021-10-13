package org.edmcouncil.spec.ontoviewer.toolkit.handlers;

import static org.edmcouncil.spec.ontoviewer.toolkit.model.EntityType.CLASS;
import static org.edmcouncil.spec.ontoviewer.toolkit.model.EntityType.DATA_PROPERTY;
import static org.edmcouncil.spec.ontoviewer.toolkit.model.EntityType.INDIVIDUAL;
import static org.edmcouncil.spec.ontoviewer.toolkit.model.EntityType.OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.parameters.Imports.INCLUDED;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.GroupsPropertyKey;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.exception.NotFoundElementInOntologyException;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlDetails;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlGroupedDetails;
import org.edmcouncil.spec.ontoviewer.core.ontology.DetailsManager;
import org.edmcouncil.spec.ontoviewer.toolkit.exception.OntoViewerToolkitRuntimeException;
import org.edmcouncil.spec.ontoviewer.toolkit.model.EntityData;
import org.edmcouncil.spec.ontoviewer.toolkit.model.EntityType;
import org.edmcouncil.spec.ontoviewer.toolkit.options.OptionDefinition;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OntologyTableDataExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(OntologyTableDataExtractor.class);
  private static final String ONTOLOGY_IRI_GROUP_NAME = "ontologyIri";
  private static final Pattern ONTOLOGY_IRI_PATTERN =
      Pattern.compile(".*\\/(?<ontologyIri>\\w+)\\/\\w+$");
  private static final String UNKNOWN_ONTOLOGY = "UNKNOWN";

  private final ConfigurationService configurationService;
  private final DetailsManager detailsManager;

  public OntologyTableDataExtractor(ConfigurationService configurationService,
      DetailsManager detailsManager) {
    this.configurationService = configurationService;
    this.detailsManager = detailsManager;
  }

  public List<EntityData> extractEntityData() {
    var result = new ArrayList<EntityData>();

    var ontology = detailsManager.getOntology();
    result.addAll(getEntities(ontology.classesInSignature(INCLUDED), CLASS));
    result.addAll(getEntities(ontology.individualsInSignature(INCLUDED), INDIVIDUAL));
    result.addAll(getEntities(ontology.objectPropertiesInSignature(INCLUDED), OBJECT_PROPERTY));
    result.addAll(getEntities(ontology.dataPropertiesInSignature(INCLUDED), DATA_PROPERTY));

    return result;
  }

  private Collection<? extends EntityData> getEntities(
      Stream<? extends OWLNamedObject> entities,
      EntityType type) {
    var filterPattern = configurationService.getCoreConfiguration()
        .getSingleStringValue(OptionDefinition.FILTER_PATTERN.argName())
        .orElse("");

    return entities
        .filter(owlClass -> owlClass.getIRI().toString().contains(filterPattern))
        .map(owlClass -> {
          try {
            return detailsManager.getDetailsByIri(owlClass.getIRI().toString());
          } catch (NotFoundElementInOntologyException e) {
            LOGGER.warn("OWL Class '{}' not found.", owlClass.getIRI());
            return null;
          }
        })
        .filter(Objects::nonNull)
        .map(owlDetails -> mapToEntityData(owlDetails, type))
        .collect(Collectors.toList());
  }

  private EntityData mapToEntityData(OwlDetails owlDetails, EntityType entityType)
      throws OntoViewerToolkitRuntimeException {
    if (owlDetails instanceof OwlGroupedDetails) {
      var groupedOwlDetails = (OwlGroupedDetails) owlDetails;
      var properties = groupedOwlDetails.getProperties();

      var entityData = new EntityData();
      entityData.setTermLabel(groupedOwlDetails.getLabel());
      entityData.setTypeLabel(entityType.getDisplayName());
      entityData.setOntology(getOntologyName(owlDetails.getIri()));
      entityData.setSynonyms(getProperty(properties, GroupsPropertyKey.SYNONYM));
      entityData.setDefinition(getProperty(properties, GroupsPropertyKey.DEFINITION));
      entityData.setGeneratedDefinition(
          getProperty(properties, GroupsPropertyKey.GENERATED_DESCRIPTION));
      entityData.setExamples(getProperty(properties, GroupsPropertyKey.EXAMPLE));
      entityData.setExplanations(getProperty(properties, GroupsPropertyKey.EXPLANATORY_NOTE));
      entityData.setMaturity(owlDetails.getMaturityLevel().getLabel());
      return entityData;
    } else {
      throw new OntoViewerToolkitRuntimeException(
          "Configuration item 'GROUPS' is not set which makes it impossible to extract most of the"
              + "data from ontologies.");
    }
  }

  private String getProperty(Map<String, Map<String, List<PropertyValue>>> properties,
      GroupsPropertyKey propertyKey) {
    var propertyValues =
        properties
            .getOrDefault(GroupsPropertyKey.GLOSSARY.getKey(), Collections.emptyMap())
            .get(propertyKey.getKey());
    if (propertyValues == null || propertyValues.isEmpty()) {
      return "";
    } else {
      return propertyValues.stream()
          .map(propertyValue -> propertyValue.getValue().toString())
          .collect(Collectors.joining(" "));
    }
  }

  private String getOntologyName(String iri) {
    var matcher = ONTOLOGY_IRI_PATTERN.matcher(iri);
    if (matcher.matches()) {
      return matcher.group(ONTOLOGY_IRI_GROUP_NAME);
    } else {
      return UNKNOWN_ONTOLOGY;
    }
  }
}