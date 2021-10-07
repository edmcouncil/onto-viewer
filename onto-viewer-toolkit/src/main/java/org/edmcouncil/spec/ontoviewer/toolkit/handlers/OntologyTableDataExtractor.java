package org.edmcouncil.spec.ontoviewer.toolkit.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.exception.NotFoundElementInOntologyException;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.GroupsPropertyKey;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlDetails;
import org.edmcouncil.spec.ontoviewer.core.model.details.OwlGroupedDetails;
import org.edmcouncil.spec.ontoviewer.core.ontology.DetailsManager;
import org.edmcouncil.spec.ontoviewer.toolkit.exception.OntoViewerToolkitRuntimeException;
import org.edmcouncil.spec.ontoviewer.toolkit.model.EntityData;
import org.edmcouncil.spec.ontoviewer.toolkit.model.EntityType;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OntologyTableDataExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(OntologyTableDataExtractor.class);

  private final DetailsManager detailsManager;

  public OntologyTableDataExtractor(DetailsManager detailsManager) {
    this.detailsManager = detailsManager;
  }

  public List<EntityData> extractEntityData() {
    var result = new ArrayList<EntityData>();

    var ontology = detailsManager.getOntology();
    result.addAll(
        ontology.classesInSignature(Imports.INCLUDED)
            .filter(owlClass -> owlClass.getIRI().toString().contains("edmcouncil"))
            .map(owlClass -> {
              try {
                return detailsManager.getDetailsByIri(owlClass.getIRI().toString());
              } catch (NotFoundElementInOntologyException e) {
                LOGGER.warn("OWL Class '{}' not found.", owlClass.getIRI());
                return null;
              }
            })
            .filter(Objects::nonNull)
            .map(owlDetails -> mapToEntityData(owlDetails, EntityType.CLASS))
            .collect(Collectors.toList()));

    return result;
  }

  private EntityData mapToEntityData(OwlDetails owlDetails, EntityType entityType)
      throws OntoViewerToolkitRuntimeException {
    if (owlDetails instanceof OwlGroupedDetails) {
      var groupedOwlDetails = (OwlGroupedDetails) owlDetails;
      var properties = groupedOwlDetails.getProperties();

      var entityData = new EntityData();
      entityData.setTermLabel(groupedOwlDetails.getLabel());
      entityData.setTypeLabel(entityType.getDisplayName());
      entityData.setOntology("TODO"); // TODO
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
      return propertyValues.get(0).getValue().toString();
    }
  }
}