package org.edmcouncil.spec.ontoviewer.toolkit.handlers;

import static org.edmcouncil.spec.ontoviewer.core.mapping.OntoViewerEntityType.CLASS;
import static org.edmcouncil.spec.ontoviewer.core.mapping.OntoViewerEntityType.DATATYPE;
import static org.edmcouncil.spec.ontoviewer.core.mapping.OntoViewerEntityType.DATA_PROPERTY;
import static org.edmcouncil.spec.ontoviewer.core.mapping.OntoViewerEntityType.INDIVIDUAL;
import static org.edmcouncil.spec.ontoviewer.core.mapping.OntoViewerEntityType.OBJECT_PROPERTY;
import static org.edmcouncil.spec.ontoviewer.core.ontology.generator.DescriptionGenerator.INHERITED_DESCRIPTIONS_LABEL;
import static org.edmcouncil.spec.ontoviewer.core.ontology.generator.DescriptionGenerator.OWN_DESCRIPTIONS_LABEL;
import static org.semanticweb.owlapi.model.parameters.Imports.INCLUDED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.GroupsPropertyKey;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.exception.NotFoundElementInOntologyException;
import org.edmcouncil.spec.ontoviewer.core.mapping.OntoViewerEntityType;
import org.edmcouncil.spec.ontoviewer.core.mapping.model.EntityData;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlDetails;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlGroupedDetails;
import org.edmcouncil.spec.ontoviewer.core.ontology.DetailsManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.toolkit.exception.OntoViewerToolkitRuntimeException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OntologyTableDataExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(OntologyTableDataExtractor.class);
  private static final String EQUIVALENT_TO = "EquivalentTo";
  private static final String ONTOLOGY_IRI_GROUP_NAME = "ontologyIri";
  private static final Pattern ONTOLOGY_IRI_PATTERN = Pattern.compile("(?<ontologyIri>.*\\/)[^/]+$");
  private static final String UNKNOWN_LABEL = "UNKNOWN";

  private final ApplicationConfigurationService applicationConfigurationService;
  private final DetailsManager detailsManager;
  private final LabelProvider labelProvider;

  private Map<String, List<String>> extractDataColumns;

  public OntologyTableDataExtractor(ApplicationConfigurationService applicationConfigurationService,
      DetailsManager detailsManager,
      LabelProvider labelProvider) {
    this.applicationConfigurationService = applicationConfigurationService;
    this.detailsManager = detailsManager;
    this.labelProvider = labelProvider;
  }

  public List<EntityData> extractEntityData() {
    this.extractDataColumns =
        applicationConfigurationService.getConfigurationData().getToolkitConfig().getExtractDataColumns();

    var result = new ArrayList<EntityData>();
    var ontology = detailsManager.getOntology();

    var executorService = Executors.newFixedThreadPool(5);

    List<Callable<List<EntityData>>> tasks = new ArrayList<>();
    tasks.add(() -> getEntities(ontology.classesInSignature(INCLUDED), CLASS));
    tasks.add(() -> getEntities(ontology.individualsInSignature(INCLUDED), INDIVIDUAL));
    tasks.add(() -> getEntities(ontology.objectPropertiesInSignature(INCLUDED), OBJECT_PROPERTY));
    tasks.add(() -> getEntities(ontology.dataPropertiesInSignature(INCLUDED), DATA_PROPERTY));
    tasks.add(() -> getEntities(ontology.datatypesInSignature(INCLUDED), DATATYPE));

    try {
      var futures = executorService.invokeAll(tasks);
      executorService.shutdown();
      var succeeded = executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
      if (!succeeded) {
        throw new IllegalStateException(
            "Executor service was not able to gather all entities in the given time. Aborting...");
      }

      for (Future<List<EntityData>> future : futures) {
        result.addAll(future.get());
      }
    } catch (Exception ex) {
      LOGGER.error("Error occurred while getting all entities. Details: " + ex.getMessage(), ex);
    }

    LOGGER.debug("Found {} entities.", result.size());

    sortExtractedEntities(result);

    return result;
  }

  private List<EntityData> getEntities(Stream<? extends OWLEntity> entities, OntoViewerEntityType type) {
    var filterPattern = applicationConfigurationService.getConfigurationData()
        .getToolkitConfig()
        .getFilterPattern();
    if (filterPattern == null) {
      filterPattern = "";
    }
    var filterPatternRegex = Pattern.compile(filterPattern);

    var notFoundEntitiesCounter = new AtomicInteger(0);

    var entityData = entities
        .parallel()
        .filter(owlEntity -> {
          var matcher = filterPatternRegex.matcher(owlEntity.getIRI().toString());
          return matcher.find();
        })
        .map(owlEntity -> {
          try {
            return detailsManager.getEntityDetails(owlEntity);
          } catch (NotFoundElementInOntologyException e) {
            LOGGER.warn("OWL Entity '{}' not found.", owlEntity.getIRI());
            notFoundEntitiesCounter.getAndIncrement();
            return null;
          }
        })
        .filter(Objects::nonNull)
        .map(owlDetails -> mapToEntityData(owlDetails, type))
        .collect(Collectors.toList());

    LOGGER.info("Found {} entities of type {} and was not able to found {} entities.",
        entityData.size(), type, notFoundEntitiesCounter.get());

    return entityData;
  }

  private EntityData mapToEntityData(OwlDetails owlDetails, OntoViewerEntityType entityType)
      throws OntoViewerToolkitRuntimeException {
    if (owlDetails instanceof OwlGroupedDetails) {
      var groupedOwlDetails = (OwlGroupedDetails) owlDetails;
      var properties = groupedOwlDetails.getToolkitProperties();

      var entityData = new EntityData();
      entityData.setIri(owlDetails.getIri());
      entityData.setTermLabel(cleanString(groupedOwlDetails.getLabel()));
      entityData.setTypeLabel(cleanString(entityType.getDisplayName()));
      entityData.setOntology(cleanString(getOntologyName(groupedOwlDetails.getIri())));
      entityData.setSynonyms(cleanString(getProperty(properties, GroupsPropertyKey.SYNONYM)));
      entityData.setDefinition(cleanString(getProperty(properties, GroupsPropertyKey.DEFINITION)));
      entityData.setGeneratedDefinition(
          cleanString(
              cleanGeneratedDescription(
                  entityData.getTermLabel(),
                  getPropertyGeneratedDescription(groupedOwlDetails.getProperties()))));
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

  private String getPropertyGeneratedDescription(Map<String, Map<String, List<PropertyValue>>> properties) {
    var glossary = properties.getOrDefault(GroupsPropertyKey.GLOSSARY.getKey(), new HashMap<>());
    var generatedDescriptionValues =
        glossary.getOrDefault(GroupsPropertyKey.GENERATED_DESCRIPTION.getKey(), Collections.emptyList());
    if (!generatedDescriptionValues.isEmpty()) {
      return generatedDescriptionValues.stream()
          .map(propertyValue -> propertyValue.getValue().toString())
          .collect(Collectors.joining(" "));
    }
    return "";
  }

  private String getProperty(Map<String, Map<String, List<PropertyValue>>> properties, GroupsPropertyKey propertyKey) {
    if (properties == null) {
      return "";
    }

    List<PropertyValue> propertyValues = new ArrayList<>();

    var propertyIris = extractDataColumns.getOrDefault(propertyKey.getKey(), Collections.emptyList());
    for (String propertyIri : propertyIris) {
      var propertyValuesForIri = properties
          .getOrDefault(GroupsPropertyKey.GLOSSARY.getKey(), Collections.emptyMap())
          .get(propertyIri);
      if (propertyValuesForIri != null) {
        propertyValues.addAll(propertyValuesForIri);
      }
    }

    if (propertyValues.isEmpty()) {
      return "";
    } else {
      return propertyValues.stream()
          .filter(Objects::nonNull)
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
      if (term.isBlank()) {
        return cleanedText;
      }
      var startingLabel = term.substring(0, 1).toUpperCase() + term.substring(1);
      cleanedText = cleanedText
          .replaceAll(startingLabel, "It")
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