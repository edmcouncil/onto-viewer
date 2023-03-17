package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevel;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevelFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.springframework.stereotype.Service;

@Service
public class MaturityLevelHandler {

  private final ApplicationConfigurationService applicationConfigurationService;
  private final OntologyManager ontologyManager;
  private final MaturityLevelFactory maturityLevelFactory;

  private OWLAnnotationProperty maturityLevelAnnotationProperty;

  public MaturityLevelHandler(ApplicationConfigurationService applicationConfigurationService,
      OntologyManager ontologyManager,
      MaturityLevelFactory maturityLevelFactory) {
    this.applicationConfigurationService = applicationConfigurationService;
    this.ontologyManager = ontologyManager;
    this.maturityLevelFactory = maturityLevelFactory;
  }

  public MaturityLevel getMaturityLevelForElement(IRI entityIri) {
    Optional<OWLEntity> entityOptional = ontologyManager.getOntology()
        .signature(Imports.INCLUDED)
        .filter(c -> c.getIRI().equals(entityIri))
        .findFirst();

    // Check if the entityOptional has its own level of maturity
    if (entityOptional.isEmpty()) {
      return maturityLevelFactory.notSet();
    }

    OWLEntity entity = entityOptional.get();

    var maturityLevelOptional = getMaturityLevelForEntity(entity);
    if (maturityLevelOptional.isPresent() && !maturityLevelOptional.get().equals(maturityLevelFactory.notSet())) {
      return maturityLevelOptional.get();
    }

    maturityLevelOptional = getMaturityLevelForEntityFromOntology(entity);
    if (maturityLevelOptional.isPresent() && !maturityLevelOptional.get().equals(maturityLevelFactory.notSet())) {
      return maturityLevelOptional.get();
    }

    return maturityLevelFactory.notSet();
  }

  public Optional<MaturityLevel> getMaturityLevelForParticularOntology(OWLOntology ontology) {
    var levelString = maturityLevelFactory.getMaturityLevels()
        .stream()
        .map(MaturityLevel::getIri)
        .collect(Collectors.toSet());
    for (OWLAnnotation annotation : ontology.annotationsAsList()) {
      var annotationValue = annotation.annotationValue();

      if (annotationValue.isIRI()
          && annotationValue.asIRI().isPresent()
          && levelString.contains(annotationValue.asIRI().get().getIRIString())) {
        String annotationIri = annotationValue.asIRI().get().toString();
        return maturityLevelFactory.getByIri(annotationIri);
      }
    }

    return Optional.empty();
  }

  private Optional<MaturityLevel> getMaturityLevelForEntity(OWLEntity entity) {
    var maturityLevelProperties = EntitySearcher.getAnnotations(
            entity,
            ontologyManager.getOntologyWithImports(),
            maturityLevelAnnotationProperty)
        .collect(Collectors.toList());

    if (maturityLevelProperties.isEmpty()) {
      return Optional.empty();
    }

    MaturityLevel maturityLevel = null;

    for (OWLAnnotation maturityLevelProperty : maturityLevelProperties) {
      if (maturityLevelProperty.getValue().isIRI()) {
        var maturityLevelPropertyValueIri = maturityLevelProperty.getValue().asIRI().get();
        var maturityLevelOptional = maturityLevelFactory.getMaturityLevels()
            .stream()
            .filter(maturityLevelCandidate ->
                maturityLevelCandidate.getIri().equals(maturityLevelPropertyValueIri.getIRIString()))
            .findFirst();
        if (maturityLevelOptional.isPresent()) {
          maturityLevel = maturityLevelOptional.get();
          break;
        }
      }
    }

    if (maturityLevel != null) {
      return Optional.of(maturityLevel);
    }

    return Optional.empty();
  }

  private Optional<MaturityLevel> getMaturityLevelForEntityFromOntology(OWLEntity entity) {
    List<OWLOntology> ontologies = ontologyManager.getOntologyWithImports().collect(Collectors.toList());
    for (OWLOntology ontology : ontologies) {
      boolean entityInOntologyPresent = ontology.entitiesInSignature(entity.getIRI(), Imports.EXCLUDED)
          .findAny()
          .isPresent();
      if (entityInOntologyPresent) {
        var maturityLevelProperties = ontology
            .annotations(getMaturityLevelProperty())
            .collect(Collectors.toList());

        if (maturityLevelProperties.isEmpty()) {
          break;
        } else {
          for (OWLAnnotation maturityLevelProperty : maturityLevelProperties) {
            if (maturityLevelProperty.getValue().isIRI() && maturityLevelProperty.getValue().asIRI().isPresent()) {
              var maturityLevelPropertyValueIri = maturityLevelProperty.getValue().asIRI().get();
              var maturityLevelOptional =
                  maturityLevelFactory.getMaturityLevel(maturityLevelPropertyValueIri.getIRIString());
              if (maturityLevelOptional.isPresent()) {
                return maturityLevelOptional;
              }
            }
          }
        }
      }
    }

    return Optional.empty();
  }

  private OWLAnnotationProperty getMaturityLevelProperty() {
    if (maturityLevelAnnotationProperty == null) {
      String maturityLevelPropertyIriString =
          applicationConfigurationService.getConfigurationData().getOntologiesConfig().getMaturityLevelProperty();
      maturityLevelAnnotationProperty =
          OWLManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(maturityLevelPropertyIriString));
    }
    return maturityLevelAnnotationProperty;
  }
}