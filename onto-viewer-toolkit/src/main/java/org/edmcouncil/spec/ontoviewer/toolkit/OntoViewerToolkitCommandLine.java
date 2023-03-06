package org.edmcouncil.spec.ontoviewer.toolkit;

import static org.edmcouncil.spec.ontoviewer.toolkit.options.OptionDefinition.EXTRACT_DATA_COLUMN;
import static org.edmcouncil.spec.ontoviewer.toolkit.options.OptionDefinition.MATURITY_LEVEL;
import static org.edmcouncil.spec.ontoviewer.toolkit.options.OptionDefinition.ONTOLOGY_IRI;
import static org.edmcouncil.spec.ontoviewer.toolkit.options.OptionDefinition.ONTOLOGY_MAPPING;
import static org.edmcouncil.spec.ontoviewer.toolkit.options.OptionDefinition.ONTOLOGY_VERSION_IRI;
import static org.edmcouncil.spec.ontoviewer.toolkit.options.OptionDefinition.OUTPUT;
import static org.edmcouncil.spec.ontoviewer.toolkit.options.OptionDefinition.VERSION;

import com.google.common.base.Stopwatch;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.Pair;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.model.PairWithList;
import org.edmcouncil.spec.ontoviewer.configloader.configuration.service.ApplicationConfigurationService;
import org.edmcouncil.spec.ontoviewer.configloader.utils.files.FileSystemService;
import org.edmcouncil.spec.ontoviewer.core.exception.OntoViewerException;
import org.edmcouncil.spec.ontoviewer.core.mapping.OntologyCatalogParser;
import org.edmcouncil.spec.ontoviewer.core.mapping.model.Uri;
import org.edmcouncil.spec.ontoviewer.core.ontology.OntologyManager;
import org.edmcouncil.spec.ontoviewer.core.ontology.data.handler.ResourcesPopulate;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.CommandLineOntologyLoader;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.LoadedOntologyData;
import org.edmcouncil.spec.ontoviewer.core.ontology.loader.listener.MissingImport;
import org.edmcouncil.spec.ontoviewer.toolkit.config.ApplicationConfigProperties;
import org.edmcouncil.spec.ontoviewer.toolkit.exception.OntoViewerToolkitException;
import org.edmcouncil.spec.ontoviewer.toolkit.exception.OntoViewerToolkitRuntimeException;
import org.edmcouncil.spec.ontoviewer.toolkit.handlers.OntologyConsistencyChecker;
import org.edmcouncil.spec.ontoviewer.toolkit.handlers.OntologyImportsMerger;
import org.edmcouncil.spec.ontoviewer.toolkit.handlers.OntologyTableDataExtractor;
import org.edmcouncil.spec.ontoviewer.toolkit.io.CsvWriter;
import org.edmcouncil.spec.ontoviewer.toolkit.io.TextWriter;
import org.edmcouncil.spec.ontoviewer.toolkit.model.ConsistencyCheckResult;
import org.edmcouncil.spec.ontoviewer.toolkit.options.CommandLineOptions;
import org.edmcouncil.spec.ontoviewer.toolkit.options.CommandLineOptionsHandler;
import org.edmcouncil.spec.ontoviewer.toolkit.options.Goal;
import org.edmcouncil.spec.ontoviewer.toolkit.options.OptionDefinition;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.stereotype.Component;

@Component
public class OntoViewerToolkitCommandLine implements CommandLineRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(OntoViewerToolkitCommandLine.class);

  private final ApplicationConfigurationService applicationConfigurationService;
  private final OntologyManager ontologyManager;
  private final OntologyTableDataExtractor ontologyTableDataExtractor;
  private final OntologyConsistencyChecker ontologyConsistencyChecker;
  private final OntologyImportsMerger ontologyImportsMerger;
  private final StandardEnvironment environment;
  private final ApplicationConfigProperties applicationConfigProperties;
  private final FileSystemService fileSystemService;
  private final ResourcesPopulate resourcesPopulate;

  public OntoViewerToolkitCommandLine(
      ApplicationConfigurationService applicationConfigurationService,
      OntologyManager ontologyManager,
      OntologyTableDataExtractor ontologyTableDataExtractor,
      OntologyConsistencyChecker ontologyConsistencyChecker,
      OntologyImportsMerger ontologyImportsMerger,
      StandardEnvironment environment,
      ApplicationConfigProperties applicationConfigProperties,
      FileSystemService fileSystemService, ResourcesPopulate resourcesPopulate) {
    this.applicationConfigurationService = applicationConfigurationService;
    this.resourcesPopulate = resourcesPopulate;
    // We don't need the default paths configuration in OV Toolkit
    applicationConfigurationService.getConfigurationData().getOntologiesConfig().getPaths().clear();

    this.ontologyManager = ontologyManager;
    this.ontologyTableDataExtractor = ontologyTableDataExtractor;
    this.ontologyConsistencyChecker = ontologyConsistencyChecker;
    this.ontologyImportsMerger = ontologyImportsMerger;
    this.environment = environment;
    this.applicationConfigProperties = applicationConfigProperties;
    this.fileSystemService = fileSystemService;
  }

  @Override
  public void run(String... args) throws Exception {
    var stopwatch = Stopwatch.createStarted();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Current working directory: {}", System.getProperty("user.dir"));
      logSystemAndSpringProperties();
      LOGGER.debug("Raw command line arguments: {}", Arrays.toString(args));
    }
    var commandLineOptionsHandler = new CommandLineOptionsHandler();
    var commandLineOptions = commandLineOptionsHandler.parseArgs(args);

    var versionOption = commandLineOptions.getOption(VERSION);
    if (versionOption.isPresent()) {
      System.out.println(getApplicationInfo());
      System.exit(0);
    }

    populateConfiguration(commandLineOptions);

    var goal = resolveGoal();

    LOGGER.info("Running goal '{}'...", goal.getName());
    switch (goal) {
      case CONSISTENCY_CHECK: {
        var consistencyResult = false;
        LoadedOntologyData loadedOntologyData = null;
        try {
          loadedOntologyData = loadOntology(goal);
          consistencyResult = ontologyConsistencyChecker.checkOntologyConsistency();
        } catch (Exception ex) {
          LOGGER.error("Exception occurred while checking ontology consistency check: {}", ex.getMessage(), ex);
        }

        var optionOutputPath = commandLineOptions.getOption(OptionDefinition.OUTPUT)
            .orElseThrow(() ->
                new OntoViewerToolkitRuntimeException("There is no option for output path set."));
        var outputPath = Path.of(optionOutputPath);
        var loadingDetails = loadedOntologyData != null ? loadedOntologyData.getLoadingDetails() : null;
        var consistencyCheckResult = new ConsistencyCheckResult(consistencyResult, loadingDetails);
        if (!consistencyCheckResult.getLoadingDetails().getMissingImports().isEmpty()) {
          var missingOntologies = consistencyCheckResult.getLoadingDetails().getMissingImports()
              .stream()
              .map(MissingImport::getIri)
              .collect(Collectors.toList());
          LOGGER.info("Consistency check result {} with missing imports: {}",
              consistencyCheckResult.isConsistent(),
              missingOntologies);
        } else {
          LOGGER.info("Consistency check result: {}", consistencyCheckResult.isConsistent());
        }
        new TextWriter().write(outputPath, consistencyCheckResult);

        break;
      }
      case EXTRACT_DATA: {
        loadOntology(goal);

        addRequiredItemsToGroupsForExtractData();

        var ontologyTableData = ontologyTableDataExtractor.extractEntityData();

        var optionOutputPath = commandLineOptions.getOption(OptionDefinition.OUTPUT)
            .orElseThrow(() ->
                new OntoViewerToolkitRuntimeException("There is no option for output path set."));
        var outputPath = Path.of(optionOutputPath);
        new CsvWriter().write(outputPath, ontologyTableData);

        break;
      }
      case MERGE_IMPORTS:
        loadOntology(goal);

        var newOntologyIriOptional = commandLineOptions.getOption(ONTOLOGY_IRI);
        if (newOntologyIriOptional.isEmpty()) {
          throw new OntoViewerToolkitRuntimeException("'ontology-iri' for 'merge-imports' goal should be provided");
        }
        var newOntologyIri = newOntologyIriOptional.get();

        var newOntologyVersionIri = newOntologyIri;
        var newOntologyVersionIriOptional = commandLineOptions.getOption(ONTOLOGY_VERSION_IRI);
        if (newOntologyVersionIriOptional.isPresent()) {
          newOntologyVersionIri = newOntologyVersionIriOptional.get();
        }

        var mergedOntology = ontologyImportsMerger.mergeImportOntologies(
            newOntologyIri,
            newOntologyVersionIri);

        var owlOntologyManager = OWLManager.createOWLOntologyManager();
        var outputOption = commandLineOptions.getOption(OUTPUT);
        if (outputOption.isPresent()) {
          var output = outputOption.get();
          LOGGER.info("Saving merged ontology to '{}'...", output);
          owlOntologyManager.saveOntology(
              mergedOntology,
              new RDFXMLDocumentFormat(),
              IRI.create(new File(output)));
        } else {
          throw new OntoViewerToolkitRuntimeException("Error: 'output' argument is not present.");
        }

        break;
      default:
        var message = String.format("Goal '%s' not recognized. Should not happen.", goal.getName());
        LOGGER.error(message);
        System.exit(1);
    }

    stopwatch.stop();
    LOGGER.debug("Application finished task in {} seconds.", stopwatch.elapsed(TimeUnit.SECONDS));
  }

  private void populateConfiguration(CommandLineOptions commandLineOptions) {
    var configurationData = applicationConfigurationService.getConfigurationData();

    configurationData.getToolkitConfig().setRunningToolkit(true);

    var ontologyMappingOption = commandLineOptions.getOption(ONTOLOGY_MAPPING);
    if (ontologyMappingOption.isPresent()) {
      var ontologyMappingPath = ontologyMappingOption.get();
      if (!ontologyMappingPath.isBlank()) {
        try {
          var catalog = new OntologyCatalogParser().readOntologyMapping(ontologyMappingPath);
          var ontologyMappingParentPath = Path.of(ontologyMappingPath).toAbsolutePath().getParent();
          LOGGER.debug("For ontology mapping '{}' parent path is resolved to: {}",
              ontologyMappingPath,
              ontologyMappingParentPath);

          if (Files.notExists(ontologyMappingParentPath)) {
            LOGGER.warn("Ontology mapping parent path ('{}') doesn't exist.", ontologyMappingParentPath);
          }

          var mappings = new HashMap<String, String>();
          for (Uri mapping : catalog.getUri()) {
            var ontologyPath = Path.of(mapping.getUri());
            if (!ontologyPath.isAbsolute()) {
              ontologyPath = ontologyMappingParentPath.resolve(ontologyPath).normalize();
            }
            if (Files.exists(ontologyPath)) {
              mappings.put(mapping.getName(), ontologyPath.toString());
            } else {
              LOGGER.warn("File ('{}') that should map to an ontology ('{}') doesn't exist.",
                  ontologyPath, mapping.getName());
            }
          }

          configurationData.getOntologiesConfig().getOntologyMappings().putAll(mappings);
        } catch (OntoViewerException ex) {
          var message = String.format("Error while handling ontology mapping from path '%s'. "
              + "Details: %s", ontologyMappingPath, ex.getMessage());
          LOGGER.warn(message, ex);
        }
      }
    }

    var ontologyPathOptions = commandLineOptions.getOptions(OptionDefinition.DATA);
    if (ontologyPathOptions != null) {
      configurationData.getOntologiesConfig().getPaths().addAll(ontologyPathOptions);
    }
    LOGGER.debug("Using ontology paths: {}", configurationData.getOntologiesConfig().getPaths());

    var filterPattern = commandLineOptions.getOption(OptionDefinition.FILTER_PATTERN).orElse("");
    configurationData.getToolkitConfig().setFilterPattern(filterPattern);

    var matureLevelOptions = commandLineOptions.getOptions(MATURITY_LEVEL);
    if (matureLevelOptions != null && !matureLevelOptions.isEmpty()) {
      var matureLevels = matureLevelOptions.stream()
          .map(matureLevelOption -> {
            if (matureLevelOption.contains("=")) {
              var matureLevelPair = matureLevelOption.split("=");
              if (matureLevelPair.length == 2) {
                return new Pair(matureLevelPair[1], matureLevelPair[0]);
              }
            }
            return null;
          })
          .filter(Objects::nonNull)
          .collect(Collectors.toList());

      configurationData.getOntologiesConfig().setMaturityLevelDefinition(matureLevels);
    }

    var extractDataColumnsOptions = commandLineOptions.getOptions(EXTRACT_DATA_COLUMN);
    if (extractDataColumnsOptions != null && !extractDataColumnsOptions.isEmpty()) {
      var extractDataColumns = extractDataColumnsOptions.stream()
          .map(extractDataColumn -> {
            if (extractDataColumn.contains("=")) {
              var extractDataColumnPair = extractDataColumn.split("=");
              if (extractDataColumnPair.length == 2) {
                var iris = List.of(extractDataColumnPair[1].split(","));
                return new PairWithList(extractDataColumnPair[0], iris);
              }
            }
            return null;
          })
          .filter(Objects::nonNull)
          .collect(Collectors.toMap(PairWithList::getLabel, PairWithList::getIris));

      configurationData.getToolkitConfig().setExtractDataColumns(extractDataColumns);
    }

    var goal = commandLineOptions.getOption(OptionDefinition.GOAL).or(() -> {
      LOGGER.error("Unable to detect correct goal.");
      System.exit(1);
      return Optional.empty();
    });
    configurationData.getToolkitConfig().setGoal(goal.get());
  }

  private Goal resolveGoal() {
    var goal = applicationConfigurationService.getConfigurationData()
        .getToolkitConfig()
        .getGoal();
    return Goal.byName(goal);
  }

  private LoadedOntologyData loadOntology(Goal goal) throws OntoViewerToolkitException {
    try {
      var ontologyLoader = new CommandLineOntologyLoader(
          applicationConfigurationService.getConfigurationData(),
          fileSystemService);
      var loadedOntologyData = ontologyLoader.load();
      var loadedOntology = loadedOntologyData.getOntology();
      LOGGER.debug("Loaded ontology contains {} axioms.", loadedOntology.getAxiomCount(Imports.INCLUDED));

      ontologyManager.updateOntology(loadedOntology);
      ontologyManager.setLocationToIriMapping(loadedOntologyData.getPathsToIrisMapping());
      if (shouldPopulateOntologyResources(goal)) {
        resourcesPopulate.populateOntologyResources();
      }
      return loadedOntologyData;
    } catch (Exception ex) {
      var message = String.format(
          "Exception occurred while loading ontology. Details: %s",
          ex.getMessage());
      throw new OntoViewerToolkitException(message, ex);
    }
  }

  private boolean shouldPopulateOntologyResources(Goal goal) {
    return goal != Goal.MERGE_IMPORTS;
  }

  private void logSystemAndSpringProperties() {
    environment.getPropertySources().forEach(propertySource -> {
      if (propertySource instanceof MapPropertySource) {
        var mapPropertySource = (MapPropertySource) propertySource;
        var stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> entry : mapPropertySource.getSource().entrySet()) {
          stringBuilder.append("'").append(entry.getKey()).append("' = '").append(entry.getValue())
              .append("', ");
        }
        LOGGER.debug("Property source '{}' content: {}", propertySource.getName(), stringBuilder);
      }
    });
  }

  private String getApplicationInfo() {
    var applicationName = applicationConfigProperties.getApplicationName();
    var applicationVersion = applicationConfigProperties.getApplicationVersion();
    var commitId = applicationConfigProperties.getCommitId();
    return String.format("%s %s (%s)", applicationName, applicationVersion, commitId);
  }

  private void addRequiredItemsToGroupsForExtractData() {
    var glossaryGroup = applicationConfigurationService.getConfigurationData().getGroupsConfig()
        .getGroups()
        .get("Glossary");

    var extractDataColumns =
        applicationConfigurationService.getConfigurationData().getToolkitConfig().getExtractDataColumns();
    extractDataColumns.putIfAbsent("definition", List.of("http://www.w3.org/2004/02/skos/core#definition"));
    extractDataColumns.putIfAbsent("example", List.of("http://www.w3.org/2004/02/skos/core#example"));
    extractDataColumns.putIfAbsent("explanatoryNote", List.of(
        "https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/explanatoryNote"));
    extractDataColumns.putIfAbsent("synonym",
        List.of(
            "https://www.omg.org/spec/Commons/AnnotationVocabulary/synonym",
            "https://spec.edmcouncil.org/fibo/ontology/FND/Utilities/AnnotationVocabulary/synonym"));

    for (Entry<String, List<String>> extractDataColumn : extractDataColumns.entrySet()) {
      glossaryGroup.addAll(extractDataColumn.getValue());
    }
  }
}