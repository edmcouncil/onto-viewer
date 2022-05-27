package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity;

import static org.edmcouncil.spec.ontoviewer.core.FiboVocabulary.HAS_MATURITY_LEVEL;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.core.model.module.OntologyModule;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLOntology;
import org.springframework.stereotype.Service;

/**
 * @author Patrycja Miazek (patrycja.miazek@makolab.com)
 */
@Service
public class OntologyHandler {

  private static final String ONTOLOGY_IRI_GROUP_NAME = "ontologyIri";
  private static final Pattern ONTOLOGY_IRI_PATTERN = Pattern.compile("(?<ontologyIri>.*\\/)[^/]+$");
  private static final String PROD = "prod";
  private static final String PROD_DEV_MIXED = "prod_and_dev_mixed";
  private static final String DEV = "dev";

  private final OntologyManager ontologyManager;

  // Cache for ontologies' maturity level
  private final Map<IRI, MaturityLevel> maturityLevelsCache = new ConcurrentHashMap<>();
  private List<OntologyModule> modules = Collections.emptyList();

  public OntologyHandler(OntologyManager ontologyManager) {
    this.ontologyManager = ontologyManager;
  }

  /**
   * Find the ontology containing the resources with given iri and extract their level of maturity. When don't find
   * resource in ontologies or ontology doesn't have maturity level method return empty fibo maturity level.
   *
   * @param entityIri IRI of element
   * @return extracted fibo maturity level
   */
  public MaturityLevel getMaturityLevelForElement(String entityIri) {
    String ontologyIri = getOntologyIri(entityIri);
    if (ontologyIri != null) {
      MaturityLevel ontoMaturityLevel = getMaturityLevelForOntology(IRI.create(ontologyIri));
      if (ontoMaturityLevel.getIri().isEmpty() && ontoMaturityLevel.getLabel().isEmpty()) {
        ontoMaturityLevel = generateMaturityLevelForModules(entityIri);
      }
      return ontoMaturityLevel;
    }
    return MaturityLevelFactory.notSet();
  }

  public void setModulesTree(List<OntologyModule> modules) {
    this.modules = modules;
  }

  public MaturityLevel getMaturityLevelForOntology(IRI ontologyIri) {
    if (!maturityLevelsCache.containsKey(ontologyIri)) {
      var ontologies = ontologyManager.getOntologyWithImports().collect(Collectors.toSet());

      for (OWLOntology ontology : ontologies) {
        var maturityLevelOptional = getMaturityLevelForParticularOntology(ontology, ontologyIri);
        if (maturityLevelOptional.isPresent()) {
          maturityLevelsCache.put(ontologyIri, maturityLevelOptional.get());
          break;
        }
      }
    }

    return maturityLevelsCache.computeIfAbsent(
        ontologyIri,
        iri -> MaturityLevelFactory.notSet());
  }

  private Optional<MaturityLevel> getMaturityLevelForParticularOntology(
      OWLOntology ontology,
      IRI ontologyIri) {
    var currentOntologyIri = ontology.getOntologyID().getOntologyIRI();
    if (currentOntologyIri.isPresent() && currentOntologyIri.get().equals(ontologyIri)) {
      for (OWLAnnotation annotation : ontology.annotationsAsList()) {
        var annotationValue = annotation.annotationValue();

        if (annotation.getProperty().getIRI().equals(HAS_MATURITY_LEVEL.getIri())
            && annotationValue.isIRI()
            && annotationValue.asIRI().isPresent()) {
          String annotationIri = annotationValue.asIRI().get().toString();
          return MaturityLevelFactory.getByIri(annotationIri);
        }
      }
    }

    return Optional.empty();
  }

  private String getOntologyIri(String elementIri) {
    var matcher = ONTOLOGY_IRI_PATTERN.matcher(elementIri);

    if (matcher.matches()) {
      return matcher.group(ONTOLOGY_IRI_GROUP_NAME);
    }

    return elementIri;
  }

  public MaturityLevel generateMaturityLevelForModules(String iri) {
    MaturityLevel result = null;
    for (OntologyModule ontologyModule : modules) {
      result = generateMaturityLevelForModules(ontologyModule, iri);
      if (result != null) {
        break;
      }
    }
    return result != null ? result : MaturityLevelFactory.notSet();
  }

  private MaturityLevel generateMaturityLevelForModules(OntologyModule ontologyModule, String iri) {
    MaturityLevel result = null;
    if (ontologyModule.getIri().equals(iri)) {
      return generateModuleMaturity(ontologyModule);
    }
    for (OntologyModule subModule : ontologyModule.getSubModule()) {
      result = generateMaturityLevelForModules(subModule, iri);
      if (result != null) {
        break;
      }
    }
    return result;
  }

  private MaturityLevel generateModuleMaturity(OntologyModule ontologyModule) {
    switch (ontologyModule.getMaturityLevel().getLabel()) {
      case PROD:
        return MaturityLevelFactory.get(MaturityLevelDefinition.PROVISIONAL);
      case PROD_DEV_MIXED:
        return MaturityLevelFactory.get(MaturityLevelDefinition.MIXED);
      case DEV:
        return MaturityLevelFactory.get(MaturityLevelDefinition.INFORMATIVE);
      default:
        return MaturityLevelFactory.notSet();
    }
  }
}
