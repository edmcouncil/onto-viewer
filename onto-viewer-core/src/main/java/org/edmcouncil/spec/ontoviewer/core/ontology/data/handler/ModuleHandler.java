package org.edmcouncil.spec.ontoviewer.core.ontology.data.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.core.model.OwlType;
import org.edmcouncil.spec.ontoviewer.core.model.PropertyValue;
import org.edmcouncil.spec.ontoviewer.core.model.module.FiboModule;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlDetailsProperties;
import org.edmcouncil.spec.ontoviewer.core.model.property.OwlListElementIndividualProperty;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo.FiboMaturityLevel;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo.FiboMaturityLevelFactory;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo.FiboOntologyHandler;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.fibo.OntoFiboMaturityLevel;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.label.LabelProvider;
import org.edmcouncil.spec.ontoviewer.core.ontology.factory.ViewerIdentifierFactory;
import org.edmcouncil.spec.ontoviewer.core.utils.PathUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ModuleHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ModuleHandler.class);
  private static final String HAS_PART_IRI = "http://purl.org/dc/terms/hasPart";
  private static final String MODULE_IRI = "http://www.omg.org/techprocess/ab/SpecificationMetadata/Module";
  private static final String RELEASE_IRI =
      "https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/Release";
  private static final String INSTANCE_KEY = ViewerIdentifierFactory.createId(
      ViewerIdentifierFactory.Type.function,
      OwlType.INSTANCES.name().toLowerCase());
  private static final Pattern URL_PATTERN =
      Pattern.compile("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

  private final OntologyManager ontologyManager;
  private final IndividualDataHandler individualDataHandler;
  private final LabelProvider labelProvider;
  private final FiboOntologyHandler fiboOntologyHandler;
  private final Set<IRI> ontologiesToIgnoreWhenGeneratingModules;
  private final Set<Pattern> ontologyModuleIgnorePatterns;
  private final OWLAnnotationProperty hasPartAnnotation;

  private List<FiboModule> modules;

  public ModuleHandler(OntologyManager ontologyManager,
      IndividualDataHandler individualDataHandler,
      LabelProvider labelProvider,
      FiboOntologyHandler fiboOntologyHandler,
      ApplicationConfigurationService applicationConfigurationService) {
    this.ontologyManager = ontologyManager;
    this.individualDataHandler = individualDataHandler;
    this.labelProvider = labelProvider;
    this.fiboOntologyHandler = fiboOntologyHandler;

    this.ontologiesToIgnoreWhenGeneratingModules =
        applicationConfigurationService.getConfigurationData()
            .getOntologiesConfig()
            .getModuleToIgnore()
            .stream()
            .map(IRI::create)
            .collect(Collectors.toSet());

    this.ontologyModuleIgnorePatterns =
        applicationConfigurationService.getConfigurationData()
            .getOntologiesConfig()
            .getModuleIgnorePatterns()
            .stream()
            .map(Pattern::compile)
            .collect(Collectors.toSet());

    this.hasPartAnnotation = OWLManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(HAS_PART_IRI));
  }

  public List<FiboModule> getModules() {
    if (modules == null) {
      var ontology = ontologyManager.getOntology();

      modules = new ArrayList<>();

      IRI moduleIri = IRI.create(MODULE_IRI);
      Optional<OWLClass> moduleClassOptional = ontology
          .classesInSignature(Imports.INCLUDED)
          .filter(c -> c.getIRI().equals(moduleIri))
          .findFirst();

      if (moduleClassOptional.isPresent()) {
        OwlDetailsProperties<PropertyValue> moduleClass =
            individualDataHandler.handleClassIndividuals(ontology, moduleClassOptional.get());

        if (!moduleClass.getProperties().isEmpty()) {
          Set<String> modulesIris = moduleClass.getProperties().get(INSTANCE_KEY)
              .stream()
              .map(OwlListElementIndividualProperty.class::cast)
              .map(individualProperty -> individualProperty.getValue().getIri().toString())
              .collect(Collectors.toSet());

          List<String> rootModulesIris = getRootModulesIris(modulesIris);

          this.modules = prepareModuleObjects(rootModulesIris);
          this.modules.forEach(this::checkAndCompleteMaturityLevel);
        }
      }

      addMissingModules(modules);
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

  private List<FiboModule> getSubModules(String moduleIri) {
    Set<String> hasPartModules = getSubModules(IRI.create(moduleIri));

    return prepareModuleObjects(hasPartModules);
  }

  private List<FiboModule> prepareModuleObjects(Collection<String> modulesIris) {
    return modulesIris.stream()
        .map(moduleIri -> {
          FiboModule module = new FiboModule();
          module.setIri(moduleIri);
          module.setLabel(labelProvider.getLabelOrDefaultFragment(IRI.create(moduleIri)));
          module.setMaturityLevel(
              chooseTheRightVersion(
                  fiboOntologyHandler.getMaturityLevelForOntology(IRI.create(moduleIri))));
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

  private FiboMaturityLevel chooseTheRightVersion(OntoFiboMaturityLevel maturityLevelFromOntology) {
    if (maturityLevelFromOntology.getIri().equals(RELEASE_IRI)) {
      return FiboMaturityLevelFactory.prod;
    } else if (maturityLevelFromOntology.getIri().equals("")) {
      return FiboMaturityLevelFactory.emptyAppFiboMaturityLabel();
    } else {
      return FiboMaturityLevelFactory.dev;
    }
  }

  private void checkAndCompleteMaturityLevel(FiboModule module) {
    if (module.getMaturityLevel() != null && !module.getMaturityLevel().getLabel().equals("")) {
      return;
    }

    Set<FiboMaturityLevel> levels = new HashSet<>();
    for (FiboModule subModule : module.getSubModule()) {
      checkAndCompleteMaturityLevel(subModule);
      levels.add(subModule.getMaturityLevel());
    }

    module.setMaturityLevel(chooseTheRightVersion(levels));
  }

  private FiboMaturityLevel chooseTheRightVersion(Set<FiboMaturityLevel> levels) {
    int prodCount = 0;
    int devCount = 0;

    for (FiboMaturityLevel level : levels) {
      if (level.equals(FiboMaturityLevelFactory.prodDev)) {
        return level;
      } else if (level.equals(FiboMaturityLevelFactory.prod)) {
        prodCount++;
      } else if (level.equals(FiboMaturityLevelFactory.dev)) {
        devCount++;
      }
    }

    LOGGER.trace("Version select, prodCount: {}, devCount: {}, size: {}", prodCount, devCount, levels.size());

    if (prodCount == levels.size()) {
      return FiboMaturityLevelFactory.prod;
    } else if (devCount == levels.size()) {
      return FiboMaturityLevelFactory.dev;
    } else {
      return FiboMaturityLevelFactory.prodDev;
    }
  }

  private void addMissingModules(List<FiboModule> modules) {
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

          FiboModule module = new FiboModule();
          module.setIri(ontologyIri.toString());
          module.setLabel(labelProvider.getLabelOrDefaultFragment(ontologyIri));
          module.setSubModule(new ArrayList<>());
          module.setMaturityLevel(
              chooseTheRightVersion(
                  fiboOntologyHandler.getMaturityLevelForOntology(ontologyIri)));
          if ("".equals(module.getMaturityLevel().getLabel())) {
            module.setMaturityLevel(FiboMaturityLevelFactory.INFO);
          }
          return module;
        })
        .filter(fiboModule -> !fiboModule.getLabel().isEmpty())
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

  private Set<String> gatherModulesIris(List<FiboModule> modules) {
    Set<String> iris = new HashSet<>();
    for (FiboModule module : modules) {
      iris.add(module.getIri());
      iris.addAll(gatherModulesIris(module.getSubModule()));
    }
    return iris;
  }
}
