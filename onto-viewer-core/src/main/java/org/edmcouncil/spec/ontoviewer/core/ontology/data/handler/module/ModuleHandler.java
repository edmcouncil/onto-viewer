package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.individual.IndividualDataHelper;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevel;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.maturity.MaturityLevelFactory;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.edmcouncil.spec.ontoviewer.core.utils.PathUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
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
  private static final String INSTANCE_KEY = ViewerIdentifierFactory.createId(
      ViewerIdentifierFactory.Type.function,
      OwlType.INSTANCES.name().toLowerCase());
  private static final Pattern URL_PATTERN
      = Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
  private final MaturityLevelFactory maturityLevelFactory;
  private final ApplicationConfigurationService applicationConfigurationService;
  private final OntologyManager ontologyManager;
  private final IndividualDataHelper individualDataHelper;
  private final LabelProvider labelProvider;
  private final OWLAnnotationProperty hasPartAnnotation;
  // Cache for ontologies' maturity level
  private final Map<IRI, MaturityLevel> maturityLevelsCache = new ConcurrentHashMap<>();
  private  Set<IRI> ontologiesToIgnoreWhenGeneratingModules;
  private  Set<Pattern> ontologyModuleIgnorePatterns;
  private String moduleClassIri;
  private  boolean automaticCreationOfModules;
  private List<OntologyModule> modules;
  private Map<IRI, OntologyModule> modulesMap;

  public ModuleHandler(OntologyManager ontologyManager,
      IndividualDataHelper individualDataHelper,
      LabelProvider labelProvider,
      ApplicationConfigurationService applicationConfigurationService,
      MaturityLevelFactory maturityLevelFactory) {
    this.ontologyManager = ontologyManager;
    this.individualDataHelper = individualDataHelper;
    this.labelProvider = labelProvider;
    this.maturityLevelFactory = maturityLevelFactory;
    this.applicationConfigurationService = applicationConfigurationService;

    this.hasPartAnnotation = OWLManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(HAS_PART_IRI));
  }

  public List<OntologyModule> getModules() {
    if (modules == null) {
      var ontology = ontologyManager.getOntology();

      modules = new ArrayList<>();

      if (moduleClassIri != null) {
        IRI moduleIri = IRI.create(moduleClassIri);
        Optional<OWLClass> moduleClassOptional = ontology
            .classesInSignature(Imports.INCLUDED)
            .filter(c -> c.getIRI().equals(moduleIri))
            .findFirst();

        if (moduleClassOptional.isPresent()) {
          OwlDetailsProperties<PropertyValue> moduleClass
              = individualDataHelper.handleClassIndividuals(ontology, moduleClassOptional.get());

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

  public List<String> getRootModulesIris(Set<String> modulesIriSet, OWLOntology ontology) {
    Map<String, Integer> referenceCount = new LinkedHashMap<>();
    modulesIriSet.forEach(moduleIri -> {
      Set<String> hasPartModules = getHasPartElements(IRI.create(moduleIri), ontology);
      referenceCount.putIfAbsent(moduleIri, 0);
      hasPartModules.forEach(partModule -> {
        Integer c = referenceCount.getOrDefault(partModule, 0);
        c++;
        referenceCount.put(partModule, c);
      });
    });

    return referenceCount.entrySet()
        .stream()
        .filter(r -> r.getValue() == 0)
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
    return maturityLevelFactory.notSet();
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
    return maturityLevelFactory.notSet();
  }

  public void refreshModulesHandlerData() {
    this.modules = null;
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

    this.moduleClassIri = applicationConfigurationService
        .getConfigurationData()
        .getOntologiesConfig()
        .getModuleClassIri();
    // If modules is empty is auto generated again while get
    getModules();
  }

  private MaturityLevel getMaturityLevelForOntology(IRI ontologyIri) {
    if (!maturityLevelsCache.containsKey(ontologyIri)) {
      var ontologies = ontologyManager.getOntologyWithImports().collect(Collectors.toSet());

      for (OWLOntology ontology : ontologies) {
        var currentOntologyIri = ontology.getOntologyID().getOntologyIRI();
        if (currentOntologyIri.isPresent() && currentOntologyIri.get().equals(ontologyIri)) {
          var maturityLevelOptional = getMaturityLevelForParticularOntology(ontology);
          if (maturityLevelOptional.isPresent()) {
            maturityLevelsCache.put(ontologyIri, maturityLevelOptional.get());
            break;
          }
        }
      }
    }

    return maturityLevelsCache.computeIfAbsent(
        ontologyIri,
        iri -> maturityLevelFactory.notSet());
  }

  public IRI getOntologyIri(IRI elementIri) {
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

  private Optional<MaturityLevel> getMaturityLevelForParticularOntology(OWLOntology ontology) {
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

  private void checkAndCompleteMaturityLevel(OntologyModule module) {
    if (isMaturityLevelInSet(module.getMaturityLevel(), maturityLevelFactory.getMaturityLevels())) {
      return;
    }

    Set<MaturityLevel> levels = new HashSet<>();
    for (OntologyModule subModule : module.getSubModule()) {
      checkAndCompleteMaturityLevel(subModule);
      levels.add(subModule.getMaturityLevel());
    }

    module.setMaturityLevel(chooseMaturityLevel(levels));
  }

  private boolean isMaturityLevelInSet(MaturityLevel maturityLevel, List<MaturityLevel> maturityLevels) {
    var maturityLevelOptional = maturityLevelFactory.getByIri(maturityLevel.getIri());
    if (maturityLevelOptional.isPresent()) {
      var maturityLevelDefinition = maturityLevelOptional.get();
      return maturityLevels.contains(maturityLevelDefinition);
    }

    return false;
  }

  private MaturityLevel chooseMaturityLevel(Set<MaturityLevel> levels) {
    Set<MaturityLevel> listOfUsedMaturityLevels = new HashSet<>();
    for (MaturityLevel level : levels) {
      if (level.equals(maturityLevelFactory.mixed())) {
        return level;
      }
      listOfUsedMaturityLevels.add(level);
    }
    if (levels.isEmpty()) {
      return maturityLevelFactory.notSet();
    }
    if (listOfUsedMaturityLevels.size() > 1) {
      return maturityLevelFactory.mixed();
    } else {
      return listOfUsedMaturityLevels.stream().findFirst().get();
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
            module.setMaturityLevel(maturityLevelFactory.notSet());
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

  private Set<String> getHasPartElements(IRI iri, OWLOntology ontology) {
    OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
    Optional<OWLNamedIndividual> individual = ontology
        .individualsInSignature(Imports.INCLUDED)
        .filter(namedIndividual -> namedIndividual.getIRI().equals(iri))
        .findFirst();
    if (individual.isEmpty()) {
      return new HashSet<>(0);
    }
    Iterator<OWLAnnotation> iteratorAnnotation = EntitySearcher
        .getAnnotations(
            individual.get(),
            ontology.importsClosure(),
            dataFactory.getOWLAnnotationProperty(IRI.create(HAS_PART_IRI)))
        .iterator();

    Set<String> result = new LinkedHashSet<>();
    while (iteratorAnnotation.hasNext()) {
      OWLAnnotation annotation = iteratorAnnotation.next();
      String s = annotation.annotationValue().toString();
      result.add(s);
    }

    return result;
  }
}