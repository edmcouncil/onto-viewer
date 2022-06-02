package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import static org.edmcouncil.spec.ontoviewer.core.FiboVocabulary.HAS_MATURITY_LEVEL;
import static org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevelDefinition.INFORMATIVE;
import static org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevelDefinition.NOT_SET;
import static org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevelDefinition.PROVISIONAL;
import static org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevelDefinition.RELEASE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.module.OntologyModule;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlListElementIndividualProperty;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevel;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevelDefinition;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevelFactory;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.edmcouncil.spec.ontoviewer.core.utils.PathUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ModuleHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModuleHandler.class);
  private static final String ONTOLOGY_IRI_GROUP_NAME = "ontologyIri";
  private static final Pattern ONTOLOGY_IRI_PATTERN = Pattern.compile("(?<ontologyIri>.*\\/)[^/]+$");
  private static final String HAS_PART_IRI = "http://purl.org/dc/terms/hasPart";
  private static final String MODULE_IRI = "http://www.omg.org/techprocess/ab/SpecificationMetadata/Module";
  private static final String INSTANCE_KEY = ViewerIdentifierFactory.createId(
      ViewerIdentifierFactory.Type.function,
      OwlType.INSTANCES.name().toLowerCase());
  private static final Pattern URL_PATTERN
      = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
  private final boolean automaticCreationOfModules;
  private final OntologyManager ontologyManager;
  private final IndividualDataHandler individualDataHandler;
  private final LabelProvider labelProvider;
  private final Set<IRI> ontologiesToIgnoreWhenGeneratingModules;
  private final Set<Pattern> ontologyModuleIgnorePatterns;
  private final OWLAnnotationProperty hasPartAnnotation;
  // Cache for ontologies' maturity level
  private final Map<IRI, MaturityLevel> maturityLevelsCache = new ConcurrentHashMap<>();

  private List<OntologyModule> modules;
  private Map<IRI, OntologyModule> modulesMap;

  public ModuleHandler(OntologyManager ontologyManager,
      IndividualDataHandler individualDataHandler,
      LabelProvider labelProvider,
      ApplicationConfigurationService applicationConfigurationService) {
    this.ontologyManager = ontologyManager;
    this.individualDataHandler = individualDataHandler;
    this.labelProvider = labelProvider;
    this.automaticCreationOfModules = applicationConfigurationService.getConfigurationData()
        .getOntologiesConfig()
        .getAutomaticCreationOfModules();

    this.ontologiesToIgnoreWhenGeneratingModules = applicationConfigurationService.getConfigurationData()
        .getOntologiesConfig()
        .getModuleToIgnore()
        .stream()
        .map(IRI::create)
        .collect(Collectors.toSet());

    this.ontologyModuleIgnorePatterns
        = applicationConfigurationService.getConfigurationData()
        .getOntologiesConfig()
        .getModuleIgnorePatterns()
        .stream()
        .map(Pattern::compile)
        .collect(Collectors.toSet());

    this.hasPartAnnotation = OWLManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(HAS_PART_IRI));
  }

  public List<OntologyModule> getModules() {
    if (modules == null) {
      var ontology = ontologyManager.getOntology();

      modules = new ArrayList<>();

      IRI moduleIri = IRI.create(MODULE_IRI);
      Optional<OWLClass> moduleClassOptional = ontology
          .classesInSignature(Imports.INCLUDED)
          .filter(c -> c.getIRI().equals(moduleIri))
          .findFirst();

      if (moduleClassOptional.isPresent()) {
        OwlDetailsProperties<PropertyValue> moduleClass
            = individualDataHandler.handleClassIndividuals(ontology, moduleClassOptional.get());

        if (!moduleClass.getProperties().isEmpty()) {
          Set<String> modulesIris = moduleClass.getProperties().get(INSTANCE_KEY)
              .stream()
              .map(OwlListElementIndividualProperty.class::cast)
              .map(individualProperty -> individualProperty.getValue().getIri())
              .collect(Collectors.toSet());

          List<String> rootModulesIris = getRootModulesIris(modulesIris);

          this.modules = prepareModuleObjects(rootModulesIris);
          this.modules.forEach(this::checkAndCompleteMaturityLevel);
        }
      }
      if (automaticCreationOfModules) {
        addMissingModules(modules);
      }

      modulesMap = prepareModulesMap(modules);
    }
    return modules;
  }

  public List<String> getRootModulesIris(Set<String> modulesIris) {
    Map<String, Integer> referenceCount = new LinkedHashMap<>();

    modulesIris.forEach(moduleIri -> {
      Set<String> subModules = getSubModules(IRI.create(moduleIri));
      referenceCount.putIfAbsent(moduleIri, 0);

      subModules.forEach(partModule -> {
        int counter = referenceCount.getOrDefault(partModule, 0);
        counter++;
        referenceCount.put(partModule, counter);
      });
    });

    return referenceCount.entrySet()
        .stream()
        .filter(moduleEntry -> moduleEntry.getValue() == 0)
        .map(Entry::getKey)
        .collect(Collectors.toList());
  }

  private List<OntologyModule> getSubModules(String moduleIri) {
    Set<String> hasPartModules = getSubModules(IRI.create(moduleIri));

    return prepareModuleObjects(hasPartModules);
  }

  private List<OntologyModule> prepareModuleObjects(Collection<String> modulesIris) {
    return modulesIris.stream()
        .map(moduleIri -> {
          OntologyModule module = new OntologyModule();
          module.setIri(moduleIri);
          module.setLabel(labelProvider.getLabelOrDefaultFragment(IRI.create(moduleIri)));
          module.setMaturityLevel(getMaturityLevelForOntology(IRI.create(moduleIri)));
          module.setSubModule(getSubModules(moduleIri));
          return module;
        })
        .sorted()
        .collect(Collectors.toList());
  }

  public Set<String> getSubModules(IRI moduleIri) {
    var ontology = ontologyManager.getOntology();

    Optional<OWLNamedIndividual> individual = ontology
        .individualsInSignature(Imports.INCLUDED)
        .filter(c -> c.getIRI().equals(moduleIri))
        .findFirst();

    if (individual.isEmpty()) {
      return new HashSet<>(0);
    }

    return EntitySearcher
        .getAnnotations(
            individual.get(),
            ontology.importsClosure(),
            hasPartAnnotation)
        .map(owlAnnotation -> owlAnnotation.annotationValue().toString())
        .collect(Collectors.toSet());
  }

  public MaturityLevel getMaturityLevelForModule(IRI moduleIri) {
    OntologyModule ontologyModule = modulesMap.get(moduleIri);
    if (ontologyModule != null) {
      return ontologyModule.getMaturityLevel();
    }
    return MaturityLevelFactory.notSet();
  }

  public MaturityLevel getMaturityLevelForElement(IRI entityIri) {
    OntologyModule ontologyModule = modulesMap.get(entityIri);
    if (ontologyModule != null) {
      return ontologyModule.getMaturityLevel();
    }
    IRI ontologyIri = getOntologyIri(entityIri);
    if (ontologyIri != null) {
      return getMaturityLevelForModule(ontologyIri);
    }
    return MaturityLevelFactory.notSet();
  }

  void updateModules() {
    this.modules = null;
    // If modules is empty is auto generated again while get
    getModules();
  }

  private MaturityLevel getMaturityLevelForOntology(IRI ontologyIri) {
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

  private IRI getOntologyIri(IRI elementIri) {
    var matcher = ONTOLOGY_IRI_PATTERN.matcher(elementIri);

    if (matcher.matches()) {
      var group = matcher.group(ONTOLOGY_IRI_GROUP_NAME);
      if (group != null) {
        return IRI.create(group);
      }
      return null;
    }

    return elementIri;
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

  private void checkAndCompleteMaturityLevel(OntologyModule module) {
    if (isMaturityLevelInSet(module.getMaturityLevel(), Set.of(PROVISIONAL, INFORMATIVE, RELEASE))) {
      return;
    }

    Set<MaturityLevel> levels = new HashSet<>();
    for (OntologyModule subModule : module.getSubModule()) {
      checkAndCompleteMaturityLevel(subModule);
      levels.add(subModule.getMaturityLevel());
    }

    module.setMaturityLevel(chooseMaturityLevel(levels));
  }

  private boolean isMaturityLevelInSet(MaturityLevel maturityLevel, Set<MaturityLevelDefinition> maturityLevels) {
    var maturityLevelOptional = MaturityLevelDefinition.getByIri(maturityLevel.getIri());
    if (maturityLevelOptional.isPresent()) {
      var maturityLevelDefinition = maturityLevelOptional.get();
      return maturityLevels.contains(maturityLevelDefinition);
    }

    return false;
  }

  private MaturityLevel chooseMaturityLevel(Set<MaturityLevel> levels) {
    int releaseCounter = 0;
    int provisionalCounter = 0;
    int informativeCounter = 0;
    int notSetCounter = 0;

    for (MaturityLevel level : levels) {
      if (level.equals(MaturityLevelFactory.get(MaturityLevelDefinition.MIXED))) {
        return level;
      } else if (level.equals(MaturityLevelFactory.get(MaturityLevelDefinition.RELEASE))) {
        releaseCounter++;
      } else if (level.equals(MaturityLevelFactory.get(MaturityLevelDefinition.INFORMATIVE))) {
        informativeCounter++;
      } else if (level.equals(MaturityLevelFactory.get(MaturityLevelDefinition.PROVISIONAL))) {
        provisionalCounter++;
      } else if (level.equals(MaturityLevelFactory.get(NOT_SET))) {
        notSetCounter++;
      }
    }

    LOGGER.trace("Version select, releaseCounter: {}, informativeCounter: {}, provisionalCounter: {}, size: {}",
        releaseCounter, informativeCounter, provisionalCounter, levels.size());

    if (levels.isEmpty()) {
      return MaturityLevelFactory.notSet();
    }
    if (releaseCounter == levels.size()) {
      return MaturityLevelFactory.get(MaturityLevelDefinition.RELEASE);
    } else if (informativeCounter == levels.size()) {
      return MaturityLevelFactory.get(MaturityLevelDefinition.INFORMATIVE);
    } else if (provisionalCounter == levels.size()) {
      return MaturityLevelFactory.get(MaturityLevelDefinition.PROVISIONAL);
    } else if (notSetCounter == levels.size()) {
      return MaturityLevelFactory.get(NOT_SET);
    } else {
      return MaturityLevelFactory.get(MaturityLevelDefinition.MIXED);
    }
  }

  private void addMissingModules(List<OntologyModule> modules) {
    Set<String> modulesIris = gatherModulesIris(modules);

    var newModules = ontologyManager.getOntologyWithImports()
        .filter(owlOntology -> {
          var ontologyIriOptional = owlOntology.getOntologyID().getOntologyIRI();
          if (ontologyIriOptional.isPresent()) {
            var ontologyIri = ontologyIriOptional.get();

            var ontologyLoadedDirectly = ontologyManager.getIriToPathMapping().get(ontologyIri) != null;

            if (ontologyLoadedDirectly) {
              return checkIfShouldAddModuleForOntology(modulesIris, ontologyIri);
            }
            return false;
          }
          return false;
        })
        .map(owlOntology -> {
          // We filtered out empty ontology IRI optional above
          var ontologyIri = owlOntology.getOntologyID().getOntologyIRI().get();

          OntologyModule module = new OntologyModule();
          module.setIri(ontologyIri.toString());
          module.setLabel(labelProvider.getLabelOrDefaultFragment(ontologyIri));
          module.setSubModule(new ArrayList<>());
          module.setMaturityLevel(getMaturityLevelForOntology(ontologyIri));
          if ("".equals(module.getMaturityLevel().getLabel())) {
            module.setMaturityLevel(MaturityLevelFactory.get(NOT_SET));
          }
          return module;
        })
        .filter(ontologyModule -> !ontologyModule.getLabel().isEmpty())
        .sorted()
        .collect(Collectors.toList());

    modules.addAll(newModules);
  }

  private boolean checkIfShouldAddModuleForOntology(Set<String> modulesIris, IRI ontologyIri) {
    if (ontologiesToIgnoreWhenGeneratingModules.contains(ontologyIri)) {
      return false;
    }

    var ontologyPath = ontologyManager.getIriToPathMapping().get(ontologyIri);
    if (ontologyPath != null) {
      var urlPatternMatch = URL_PATTERN.matcher(ontologyPath);
      if (urlPatternMatch.find()) {
        for (Pattern pattern : ontologyModuleIgnorePatterns) {
          var match = pattern.matcher(ontologyPath.toString());
          if (match.find()) {
            return false;
          }
        }
      } else {
        var ontologyPathWithoutFilePrefix = PathUtils.getPathWithoutFilePrefix(ontologyPath.toString());
        var ontologyFileName = ontologyPathWithoutFilePrefix.getFileName();
        for (Pattern pattern : ontologyModuleIgnorePatterns) {
          var match = pattern.matcher(ontologyFileName.toString());
          if (match.find()) {
            return false;
          }
        }
      }
    }

    return !modulesIris.contains(ontologyIri.toString());
  }

  private Set<String> gatherModulesIris(List<OntologyModule> modules) {
    Set<String> iris = new HashSet<>();
    for (OntologyModule module : modules) {
      iris.add(module.getIri());
      iris.addAll(gatherModulesIris(module.getSubModule()));
    }
    return iris;
  }

  private Map<IRI, OntologyModule> prepareModulesMap(List<OntologyModule> modules) {
    Map<IRI, OntologyModule> ontologyModules = new HashMap<>();

    for (OntologyModule module : modules) {
      ontologyModules.put(IRI.create(module.getIri()), module);
      addSubModules(module.getSubModule(), ontologyModules);
    }

    return ontologyModules;
  }

  private void addSubModules(List<OntologyModule> subModule, Map<IRI, OntologyModule> ontologyModules) {
    for (OntologyModule ontologyModule : subModule) {
      ontologyModules.put(IRI.create(ontologyModule.getIri()), ontologyModule);
      addSubModules(ontologyModule.getSubModule(), ontologyModules);
    }
  }
}