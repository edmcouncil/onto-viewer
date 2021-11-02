package org.edmcouncil.spec.ontoviewer.toolkit.handlers;

import static org.edmcouncil.spec.ontoviewer.core.ontology.generator.DescriptionGenerator.INHERITED_DESCRIPTIONS_LABEL;
import static org.edmcouncil.spec.ontoviewer.core.ontology.generator.DescriptionGenerator.OWN_DESCRIPTIONS_LABEL;
import static org.edmcouncil.spec.ontoviewer.toolkit.model.EntityType.CLASS;
import static org.edmcouncil.spec.ontoviewer.toolkit.model.EntityType.DATATYPE;
import static org.edmcouncil.spec.ontoviewer.toolkit.model.EntityType.DATA_PROPERTY;
import static org.edmcouncil.spec.ontoviewer.toolkit.model.EntityType.INDIVIDUAL;
import static org.edmcouncil.spec.ontoviewer.toolkit.model.EntityType.OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.parameters.Imports.INCLUDED;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.toolkit.exception.OntoViewerToolkitRuntimeException;
import org.edmcouncil.spec.ontoviewer.toolkit.model.EntityData;
import org.edmcouncil.spec.ontoviewer.toolkit.model.EntityType;
import org.edmcouncil.spec.ontoviewer.toolkit.options.OptionDefinition;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OntologyTableDataExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(OntologyTableDataExtractor.class);
  private static final String EQUIVALENT_TO = "EquivalentTo";
  private static final String ONTOLOGY_IRI_GROUP_NAME = "ontologyIri";
  private static final Pattern ONTOLOGY_IRI_PATTERN =
      Pattern.compile("(?<ontologyIri>.*\\/)[^/]+$");
  private static final String UNKNOWN_LABEL = "UNKNOWN";

  private final ConfigurationService configurationService;
  private final DetailsManager detailsManager;
  private final LabelProvider labelProvider;

  public OntologyTableDataExtractor(ConfigurationService configurationService,
      DetailsManager detailsManager,
      LabelProvider labelProvider) {
    this.configurationService = configurationService;
    this.detailsManager = detailsManager;
    this.labelProvider = labelProvider;
  }

  public List<EntityData> extractEntityData() {
    var result = new ArrayList<EntityData>();

    var ontology = detailsManager.getOntology();
    result.addAll(getEntities(ontology.classesInSignature(INCLUDED), CLASS));
    result.addAll(getEntities(ontology.individualsInSignature(INCLUDED), INDIVIDUAL));
    result.addAll(getEntities(ontology.objectPropertiesInSignature(INCLUDED), OBJECT_PROPERTY));
    result.addAll(getEntities(ontology.dataPropertiesInSignature(INCLUDED), DATA_PROPERTY));
    result.addAll(getEntities(ontology.datatypesInSignature(INCLUDED), DATATYPE));

    LOGGER.debug("Found {} entities.", result.size());

    sortExtractedEntities(result);

    return result;
  }

  private Collection<? extends EntityData> getEntities(
      Stream<? extends OWLNamedObject> entities,
      EntityType type) {
    var filterPattern = configurationService.getCoreConfiguration()
        .getSingleStringValue(OptionDefinition.FILTER_PATTERN.argName())
        .orElse("");

    return entities
//        .parallel()
        .filter(owlEntity -> owlEntity.getIRI().toString().contains(filterPattern))
        .map(owlEntity -> {
          try {
            return detailsManager.getDetailsByIri(owlEntity.getIRI().toString());
          } catch (NotFoundElementInOntologyException e) {
            LOGGER.warn("OWL Entity '{}' not found.", owlEntity.getIRI());
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
      entityData.setTermLabel(cleanString(groupedOwlDetails.getLabel()));
      entityData.setTypeLabel(cleanString(entityType.getDisplayName()));
      entityData.setOntology(cleanString(getOntologyName(groupedOwlDetails.getIri())));
      entityData.setSynonyms(cleanString(getProperty(properties, GroupsPropertyKey.SYNONYM)));
      entityData.setDefinition(cleanString(getProperty(properties, GroupsPropertyKey.DEFINITION)));
      entityData.setGeneratedDefinition(
          cleanString(
              cleanGeneratedDescription(
                  entityData.getTermLabel(),
                  getProperty(properties, GroupsPropertyKey.GENERATED_DESCRIPTION))));
      entityData.setExamples(cleanString(getProperty(properties, GroupsPropertyKey.EXAMPLE)));
      entityData.setExplanations(
          cleanString(getProperty(properties, GroupsPropertyKey.EXPLANATORY_NOTE)));
      entityData.setMaturity(cleanString(getMaturityLevel(groupedOwlDetails)));
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

  private String getMaturityLevel(OwlGroupedDetails owlDetails) {
    var maturityLevel = owlDetails.getMaturityLevel();
    if (maturityLevel != null && maturityLevel.getLabel() != null) {
      return maturityLevel.getLabel();
    }

    return UNKNOWN_LABEL;
  }

  private String cleanString(String text) {
    if (text == null) {
      return UNKNOWN_LABEL;
    }
    return text.replaceAll("\\s+", " ");
  }

  private String cleanGeneratedDescription(String term, String text) {
    var cleanedText = text.replace("- ", " ")
        .replace(INHERITED_DESCRIPTIONS_LABEL, " ")
        .replace(OWN_DESCRIPTIONS_LABEL, " ")
        .replace(EQUIVALENT_TO, "")
        .trim();
    // We replace term's label with 'It' but not for the first occurrence
    if (cleanedText.toLowerCase().startsWith(term.toLowerCase())) {
      var startingLabel = term.substring(0, 1).toUpperCase() + term.substring(1);
      cleanedText = cleanedText.replaceAll(startingLabel, "It")
          .replaceFirst("It", startingLabel);
    }
    return cleanedText;
  }

  private String getOntologyName(String iri) {
    var matcher = ONTOLOGY_IRI_PATTERN.matcher(iri);

    if (matcher.matches()) {
      var ontologyIri = matcher.group(ONTOLOGY_IRI_GROUP_NAME);
      return labelProvider.getLabelOrDefaultFragment(IRI.create(ontologyIri));
    }

    return "<" + iri + ">";
  }

  private void sortExtractedEntities(List<EntityData> result) {
    result.sort(Comparator.comparing(EntityData::getTermLabel));
  }
}